package scr;
import java.io.Console;
import java.util.List;
import java.util.Scanner;

public class usernames {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static String user () {
        Console console = System.console();
        if (console != null) {
            return console.readLine("Enter username: ").trim();
        }
        System.out.print("Enter username: ");
        return SCANNER.nextLine().trim();
    }

    public static String password(){
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword("Enter master password: ");
            return new String(passwordArray);
        }
        System.out.print("Enter master password: ");
        return SCANNER.nextLine();
    }

    private static int readPasswordLength() {
        while (true) {
            System.out.print("Password length (>=4): ");
            String line = SCANNER.nextLine().trim();
            try {
                int length = Integer.parseInt(line);
                if (length >= 4) {
                    return length;
                }
                System.out.println("Length must be 4 or more.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static void showSavedPasswords(int userId) {
        List<String> saved = databaseConnects.getGeneratedPasswordsByUserId(userId);
        if (saved.isEmpty()) {
            System.out.println("No saved generated passwords yet.");
            return;
        }

        System.out.println("Saved generated passwords:");
        for (int i = 0; i < saved.size(); i++) {
            System.out.println((i + 1) + ". " + saved.get(i));
        }
    }

    public static void main(String[] args) {
        databaseConnects.initialise();

        System.out.println("=== Password Generator Login ===");
        String username = user();
        String password = password();

        Integer existingUserId = databaseConnects.findUserIdByUsername(username);
        int userId;

        if (existingUserId != null) {
            Integer loggedInUserId = databaseConnects.login(username, password);
            if (loggedInUserId == null) {
                System.out.println("Login failed: wrong password.");
                return;
            }
            userId = loggedInUserId;
            System.out.println("Login successful. Welcome back, " + username + "!");
            showSavedPasswords(userId);
        } else {
            Integer createdUserId = databaseConnects.registerUser(username, password);
            if (createdUserId == null) {
                System.out.println("Could not create account. Please try again.");
                return;
            }
            userId = createdUserId;
            System.out.println("New user created. Welcome, " + username + "!");
        }

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Generate and save a new password");
            System.out.println("2. Show my saved generated passwords");
            System.out.println("3. Exit");
            System.out.print("Option: ");

            String option = SCANNER.nextLine().trim();
            if ("1".equals(option)) {
                int length = readPasswordLength();
                String generated = generator.generators(length);
                if (generated.startsWith("invalid length")) {
                    System.out.println(generated);
                    continue;
                }
                databaseConnects.saveGeneratedPassword(userId, generated);
                System.out.println("Generated password: " + generated);
                System.out.println("Saved to your account.");
            } else if ("2".equals(option)) {
                showSavedPasswords(userId);
            } else if ("3".equals(option)) {
                System.out.println("Goodbye.");
                break;
            } else {
                System.out.println("Invalid option. Choose 1, 2, or 3.");
            }
        }


    }
}
