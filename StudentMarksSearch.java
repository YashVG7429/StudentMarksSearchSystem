import java.io.*;
import java.util.Scanner;

// Stores the details of one student.
class Student {
    private String id;
    private String name;
    private int marks;

    public Student(String id, String name, int marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMarks() {
        return marks;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + marks;
    }
}

// Manages student records and all operations on them.
class StudentManager {
    private Student[] students = new Student[100];
    private int count = 0;
    private int nextIdNumber = 101;
    private static final String FILE_NAME = "marks.txt";

    // Loads records from marks.txt when the program starts.
    public void loadFromFile() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("No existing data file found. Starting with an empty list.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null && count < students.length) {
                if (line.trim().isEmpty()) {
                    continue; // Skip blank lines safely
                }

                String[] parts = line.split(",", 3);

                if (parts.length != 3) {
                    System.out.println("Skipping an invalid record in marks.txt: " + line);
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    int marks = Integer.parseInt(parts[2].trim());

                    if (id.isEmpty() || name.isEmpty() || marks < 0 || marks > 100) {
                        System.out.println("Skipping an invalid record in marks.txt: " + line);
                        continue;
                    }

                    students[count] = new Student(id, name, marks);
                    count++;

                    // Find the highest existing ID number.
                    if (id.matches("S\\d+")) {
                        int number = Integer.parseInt(id.substring(1));
                        if (number >= nextIdNumber) {
                            nextIdNumber = number + 1;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Skipping a record with invalid marks: " + line);
                }
            }

            System.out.println(count + " student record(s) loaded.");
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }

        if (count == students.length) {
            System.out.println("The maximum capacity of 100 records has been reached.");
        }
    }

    // Saves all current records to marks.txt.
    public void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < count; i++) {
                // The file format is ID,Name,Marks.
                bw.write(students[i].getId() + ","
                        + students[i].getName() + ","
                        + students[i].getMarks());
                bw.newLine();
            }
            System.out.println("Data saved successfully to " + FILE_NAME + ".");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // Adds a student if the details are valid.
    public boolean addStudent(String name, int marks) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Invalid name. Name cannot be empty.");
            return false;
        }

        // Prevent commas so marks.txt CSV parsing does not break on reload
        if (name.contains(",")) {
            System.out.println("Invalid name. Name cannot contain commas (,).");
            return false;
        }

        if (marks < 0 || marks > 100) {
            System.out.println("Invalid marks. Marks must be between 0 and 100.");
            return false;
        }

        if (count == students.length) {
            System.out.println("Cannot add student. The array is full.");
            return false;
        }

        String id = "S" + nextIdNumber;
        students[count] = new Student(id, name.trim(), marks);
        count++;
        nextIdNumber++;

        System.out.println("Student added. ID: " + students[count - 1]);
        return true;
    }

    // Displays every stored student.
    public void displayAll() {
        if (count == 0) {
            System.out.println("No student records available.");
            return;
        }

        System.out.println("\nStudent Records:");
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + ". " + students[i]);
        }
    }

    // Manually sorts records by marks using Bubble Sort.
    // It is stable because equal marks are not swapped.
    public void sortByMarks() {
        if (count == 0) {
            System.out.println("No student records to sort.");
            return;
        }

        for (int i = 0; i < count - 1; i++) {
            boolean swapped = false;

            for (int j = 0; j < count - 1 - i; j++) {
                if (students[j].getMarks() > students[j + 1].getMarks()) {
                    Student temp = students[j];
                    students[j] = students[j + 1];
                    students[j + 1] = temp;
                    swapped = true;
                }
            }

            // Stop early if this pass made no swaps.
            if (!swapped) {
                break;
            }
        }

        System.out.println("Students sorted by marks (low to high).");
        displayAll();
    }

    // Checks whether the records are in ascending order of marks.
    public boolean isSortedByMarks() {
        for (int i = 0; i < count - 1; i++) {
            if (students[i].getMarks() > students[i + 1].getMarks()) {
                return false;
            }
        }
        return true;
    }

    // Returns the first index containing the requested marks, or -1.
    // Binary Search is used only when the array is sorted.
    public int binarySearchByMarks(int marks) {
        if (!isSortedByMarks()) {
            System.out.println("The list is not sorted by marks. Please use option 3 first.");
            return -1;
        }

        int low = 0;
        int high = count - 1;
        int result = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (students[mid].getMarks() == marks) {
                result = mid;
                high = mid - 1; // Continue left to find the first matching index.
            } else if (students[mid].getMarks() < marks) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return result;
    }

    public void searchByMarks(int marks) {
        if (count == 0) {
            System.out.println("No student records available to search.");
            return;
        }

        int index = binarySearchByMarks(marks);

        // -1 can mean either "not found" or "not sorted".
        if (index == -1) {
            if (isSortedByMarks()) {
                System.out.println("No student found with marks " + marks + ".");
            }
            return;
        }

        System.out.println("Found at index " + index + ": " + students[index]);
    }
}

// Public class name matches the file name: StudentMarksSearch.java
public class StudentMarksSearch {
    private static final Scanner sc = new Scanner(System.in);

    // Reads an integer safely without crashing on non-numeric input.
    private static int readInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) {
                return 5; // Default to Exit if input stream closes unexpectedly
            }
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n========== STUDENT MARKS SEARCH SYSTEM ==========");
        System.out.println("1. Add Student");
        System.out.println("2. Display All");
        System.out.println("3. Sort by Marks (low to high)");
        System.out.println("4. Binary Search by Marks");
        System.out.println("5. Exit");
        System.out.println("=================================================");
    }

    public static void main(String[] args) {
        StudentManager manager = new StudentManager();

        // Load saved records before showing the menu.
        manager.loadFromFile();

        boolean running = true;

        while (running) {
            showMenu();
            int choice = readInteger("Enter your choice: ");

            switch (choice) {
                case 1:
                    System.out.print("Enter student name: ");
                    String name = sc.hasNextLine() ? sc.nextLine() : "";

                    int marks = readInteger("Enter marks (0-100): ");
                    manager.addStudent(name, marks);
                    break;

                case 2:
                    manager.displayAll();
                    break;

                case 3:
                    manager.sortByMarks();
                    break;

                case 4:
                    int searchMarks = readInteger("Enter marks to search: ");

                    if (searchMarks < 0 || searchMarks > 100) {
                        System.out.println("Invalid marks. Marks must be between 0 and 100.");
                    } else {
                        manager.searchByMarks(searchMarks);
                    }
                    break;

                case 5:
                    manager.saveToFile();
                    System.out.println("Exiting program. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please select an option from 1 to 5.");
            }
        }

        sc.close();
    }
}
