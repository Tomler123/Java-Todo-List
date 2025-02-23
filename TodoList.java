import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;
import java.sql.ResultSet;
import java.util.Scanner;

class Task {

  private String taskName;
  private String dueDate;
  private int isCompleted;
  
  public static Task validate(String taskName, String dueDate, boolean isCompleted) {
    if (taskName.isEmpty()) {
      System.out.println("Error: Task name should not be empty.");
      return null;
    }
    if (!isValidDate(dueDate)) {
      System.out.println("Error: Invalid date format: YYYY-MM-DD.");
      return null;
    }
    return new Task(taskName, dueDate, isCompleted);
  }            

  private Task(String taskName, String dueDate, boolean isCompleted) {
    this.taskName = taskName;
    this.dueDate = dueDate;
    this.isCompleted = isCompleted ? 1 : 0;
  }

  private static boolean isValidDate(String date) {
    String regex = "\\d{4}-\\d{2}-\\d{2}";
    return Pattern.matches(regex, date);
  }

  public String getTaskName(){return taskName;}
  public String getDueDate(){return dueDate;}
  public int getIsCompleted(){return isCompleted;}

  public void setName(String taskName){this.taskName = taskName;}
  public void setDueDate(String dueDate){this.dueDate = dueDate;}
  public void setIsCompelted(boolean isCompleted){this.isCompleted = isCompleted ? 1 : 0;}

}

public class TodoList {

  
  private static final String DB_URL = "jdbc:sqlite:todo.db";

  public static void initializeDatabase() {
    String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
		+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "task_name TEXT NOT NULL,"
		+ "due_date TEXT,"
		+ "is_complete INTEGER DEFAULT 0"
		+ ");";

    try (Connection conn = DriverManager.getConnection(DB_URL);
      Statement stmt = conn.createStatement()) {
      stmt.execute(createTableSQL);
      System.out.println("Database Initialised Successfully!");
    } catch (SQLException e) {
      System.out.println("Database Initialisation failed " + e.getMessage());
    }
  }

  public static void addTask(String taskName, String dueDate, boolean isCompleted) {
     Task t = Task.validate(taskName, dueDate, isCompleted);
    
     if (t != null) {
       String insertSQL = "INSERT INTO tasks (task_name, due_date, is_complete) VALUES (?,?,?)";

       try (Connection conn = DriverManager.getConnection(DB_URL);
 	   PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

	 pstmt.setString(1, t.getTaskName());
	 pstmt.setString(2, t.getDueDate());
	 pstmt.setInt(3, t.getIsCompleted());


	 int rowsAffected = pstmt.executeUpdate();

	 if(rowsAffected > 0)
	   System.out.println("Task added successfully!");
       } catch (SQLException e) {
	System.out.println("Error adding task: " + e.getMessage());
       }

     } else {
       System.out.println("Error adding task.");
       return;

     }
  }
   
  public static void getAllTasks() {

      String selectSQL = "SELECT * FROM tasks;";

      try (Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL)) {
	 
        System.out.println("=== Todo List ===");
	while (rs.next()) {
	  int id = rs.getInt("id");
	  String taskName = rs.getString("task_name");
	  String dueDate = rs.getString("due_date");
	  boolean isCompleted = rs.getInt("is_complete") == 1;

	  System.out.printf("ID: %d | Task: %s | Due: %s | Completed: %s\n",
			  id, taskName, dueDate, isCompleted ? "Yes" : "No");
	}
      } catch (SQLException e){
        System.out.println("Error retrieving tasks: " + e.getMessage());
      }
  }
  public static void getTaskById(int taskId){
    	String selectSQL = "SELECT * FROM tasks WHERE id = ?;";

	try (Connection conn = DriverManager.getConnection(DB_URL);
	    PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
	  
	    pstmt.setInt(1, taskId);
	    ResultSet rs = pstmt.executeQuery();

	    if (rs.next()) {
	        String taskName = rs.getString("task_name");
		String dueDate = rs.getString("due_date");
		boolean isCompleted = rs.getInt("is_complete") == 1;

		System.out.printf("\n=== ==== ===\nTask ID: %d\nTask: %s\nDue: %s\nCompleted: %s\n=== ==== ===\n",
				taskId, taskName, dueDate, isCompleted ? "Yes" : "No");
	    } else {
	        System.out.println("Error: Task with ID = " + taskId + " does not exist.");
	    }
	} catch (SQLException e) {
	    System.out.println("Error retrieving task: " + e.getMessage());
	}
    }
    
    public static void updateTask(int taskId, String newTaskName, String newDueDate, boolean newIsCompleted) {
      String updateSQL = "UPDATE tasks SET task_name = ?, due_date = ?, is_complete = ? WHERE id = ?;";

      try (Connection conn = DriverManager.getConnection(DB_URL);
          PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

      	pstmt.setString(1, newTaskName);
	pstmt.setString(2, newDueDate);
	pstmt.setInt(3, newIsCompleted ? 1 : 0);
	pstmt.setInt(4, taskId);

	int affectedRows = pstmt.executeUpdate();
	if (affectedRows > 0) {
	  System.out.println("Task with ID " + taskId + " updated successfully!");
	  getTaskById(taskId);
	} else {
	  System.out.println("Task with ID " + taskId + " bit found.");
	}
      } catch (SQLException e) {
      	System.out.println("Error updating task: " + e.getMessage());
      }
    }
    
    public static void deleteTask(int taskId) {
      String deleteSQL = "DELETE FROM tasks WHERE id = ?;";

      try (Connection conn = DriverManager.getConnection(DB_URL);
          PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
	
	pstmt.setInt(1, taskId);

	int affectedRows = pstmt.executeUpdate();

	if (affectedRows > 0) {
	  System.out.println("Task with ID: " + taskId + " deleted successfully!");
	} else {
	  System.out.println("Task ID: " + taskId + " not found.");
	}
      
      } catch (SQLException e) {
        System.out.println("Error deleting task: " + e.getMessage());
      }
    }

    public static void changeStatus(int taskId) {
      String selectSQL = "SELECT is_complete FROM tasks WHERE id = ?;";
      String updateSQL = "UPDATE tasks SET is_complete = ? WHERE id = ?";

      boolean isComplete;

      try (Connection conn = DriverManager.getConnection(DB_URL);
	  PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
	  PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {

	selectStmt.setInt(1, taskId);
	ResultSet rs = selectStmt.executeQuery();

	if (rs.next()) {
	  boolean currentStatus = rs.getInt("is_complete") == 1;
	  
	  updateStmt.setInt(1, currentStatus ? 0 : 1);
	  updateStmt.setInt(2, taskId);

	  int affectedRows = updateStmt.executeUpdate();
	  if (affectedRows > 0) {
	    System.out.println("Task ID: " + taskId + " status updated successfully!");

	  } else {
	    System.out.println("Task with ID: " + taskId + " not found.");
	    getTaskById(taskId);
	  }
	}
        
      } catch (SQLException e) {
        System.out.println("Error while updating status: " + e.getMessage());
      }
    }

    public static void printMenu() {
      System.out.println("\n===== TODO LIST MENU =====");
      System.out.println("1. Add Task");
      System.out.println("2. View All Tasks");
      System.out.println("3. View Task by ID");
      System.out.println("4. Update Task");
      System.out.println("5. Delete Task");
      System.out.println("6. Change Task Status");
      System.out.println("7. Exit");
      System.out.print("Enter your choice: ");
    }

    
    public static void main(String[] args) {
      initializeDatabase();
      
      System.out.println();
      System.out.println();
      
      Scanner scanner = new Scanner(System.in);
      boolean running = true;

      while (running) {
        printMenu();
	
	int choice = scanner.nextInt();
	scanner.nextLine();

	switch (choice) {
	  case 1:
	    System.out.print("Enter Task Name: ");
	    String taskName = scanner.nextLine();
	    System.out.print("Enter due date (YYYY-MM-DD): ");
	    String dueDate = scanner.nextLine();
	    addTask(taskName, dueDate, false);
	    break;
	  
	  case 2:
	    getAllTasks();
	    break;
	  
	  case 3:
	    System.out.print("Enter task ID: ");
	    getTaskById(scanner.nextInt());
	    break;
	  
	  case 4:
	    System.out.print("Enter task ID to update: ");
            int updateId = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter new task name: ");
            String newTaskName = scanner.nextLine();
            System.out.print("Enter new due date (YYYY-MM-DD): ");
            String newDueDate = scanner.nextLine();
            System.out.print("Is the task completed? (true/false): ");
            boolean newStatus = scanner.nextBoolean();
            updateTask(updateId, newTaskName, newDueDate, newStatus);
            break;

	  case 5:
	    System.out.print("Enter task ID to delete: ");
	    deleteTask(scanner.nextInt());
	    break;
	
	  case 6:
	    System.out.print("Enter task ID to change status: ");
	    changeStatus(scanner.nextInt());
	    break;
	  
	  case 7:
	    running = false;
	    break;
	  
	  default:
	    System.out.println("Invalid choice. Please try again. (1-7)");
	}
      }

      scanner.close();
      System.out.println("End of the loop.\nGood bye and good luck completing all tasks!");
  }
}

