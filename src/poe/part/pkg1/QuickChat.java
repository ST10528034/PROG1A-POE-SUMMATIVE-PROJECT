/*
 * QuickChat.java - QuickChat POE Part 3
 * Main program: register, log in, send messages and work with stored messages.
 * Every input and output uses JOptionPane dialogs.
 */
package poe.part.pkg1;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * QuickChat: register, log in, then use a numeric menu to send messages
 * and to view the stored messages.
 */
public class QuickChat {

    private static final String TITLE = "QuickChat";

    private static final String MAIN_MENU = """
            ===== Menu =====
            1) Send Messages
            2) Show recently sent messages
            3) Quit
            4) Stored Messages

            Choose an option:""";

    private static final String STORED_MENU = """
            ===== Stored Messages =====
            1) Display the sender and recipient of all stored messages.
            2) Display the longest stored message.
            3) Search for a message ID.
            4) Search for all messages stored for a recipient.
            5) Delete a message using the message hash.
            6) Display a report of all stored messages.
            0) Back to the main menu.

            Choose an option:""";

    public static void main(String[] args) {
        Login login = registerAndLogin();

        showMessage("WELCOME TO QUICKCHAT!");
        Message.loadStoredMessages();

        int totalAllowed = readInt("How many messages do you wish to enter?", 1);
        int entered = 0;   // number of messages handled so far (also the next message number)
        int option = 0;

        while (option != 3) {
            option = readInt(MAIN_MENU, 0);

            switch (option) {
                case 1 -> entered = sendMessages(entered, totalAllowed, login);
                case 2 -> showSentMessages();
                case 3 -> showMessage("Goodbye. Total messages sent: " + Message.returnTotalMessages());
                case 4 -> storedMessagesMenu();
                default -> showMessage("Invalid option. Please choose 1, 2, 3 or 4.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Registration and login
    // ---------------------------------------------------------------
    private static Login registerAndLogin() {
        String firstName = askOrQuit("Enter your first name:").trim();
        String lastName = askOrQuit("Enter your last name:").trim();

        Login login = new Login(firstName, lastName);
        boolean registered = false;

        while (!registered) {
            String username = askOrQuit("Enter username (underscore, max 5 characters):").trim();
            String password = askOrQuit("Enter password (8+ chars, capital, number, special):");
            String cell = askOrQuit("Enter cell number (e.g. +27718693002 / 0712345678):").trim();

            String result = login.registerUser(username, password, cell);
            showMessage(result);
            registered = result.endsWith("registered successfully.");
        }

        boolean loggedIn = false;
        while (!loggedIn) {
            String enteredUser = askOrQuit("Login - enter username:").trim();
            String enteredPass = askOrQuit("Login - enter password:");

            showMessage(login.returnLoginStatus(enteredUser, enteredPass));
            loggedIn = login.loginUser(enteredUser, enteredPass);
        }
        return login;
    }

    // ---------------------------------------------------------------
    // Sending messages: runs until the user's chosen number is reached
    // ---------------------------------------------------------------
    private static int sendMessages(int entered, int totalAllowed, Login login) {
        if (entered >= totalAllowed) {
            showMessage("You have already entered all " + totalAllowed + " messages.");
            return entered;
        }

        // The loop counter i is the message number (starts at 0)
        for (int i = entered; i < totalAllowed; i++) {
            String recipient = askRecipient(i + 1, totalAllowed);
            String text = askMessageText();

            Message message = new Message(recipient, text, i);
            message.setSender(login.getCellNumber());
            showMessage("Message ID generated: " + message.getMessageID()
                    + "\nMessage Hash: " + message.getMessageHash());

            int choice = askSendChoice();
            String result = message.sentMessage(choice);

            switch (choice) {
                case 1 -> showReport(result + "\n\n" + Message.getSentReport());
                case 2 -> {
                    // The result is the prompt "Press 0 to delete the message."
                    if (readInt(result, 0) == 0) {
                        showMessage("Message deleted.");
                    }
                }
                default -> showMessage(result);
            }
            entered = i + 1;
        }

        showMessage("Total messages sent: " + Message.returnTotalMessages());
        return entered;
    }

    private static String askRecipient(int number, int total) {
        while (true) {
            String recipient = askOrQuit("MESSAGE " + number + " of " + total
                    + "\nEnter recipient cell number (e.g. +27718693002):").trim();
            String result = Message.checkRecipientCell(recipient);
            if (result.equals(Message.RECIPIENT_OK)) {
                showMessage(result);
                return recipient;
            }
            showMessage(result);
        }
    }

    private static String askMessageText() {
        while (true) {
            String text = askOrQuit("Enter your message:");
            if (text.trim().isEmpty()) {
                showMessage("Message cannot be empty.");
                continue;
            }
            String result = Message.checkMessageLength(text);
            if (result.equals(Message.MESSAGE_OK)) {
                showMessage(result);
                return text;
            }
            showMessage(result);
        }
    }

    private static int askSendChoice() {
        int choice = readInt("What would you like to do with this message?\n"
                + "1) Send Message\n2) Disregard Message\n3) Store Message to send later\n\n"
                + "Enter your choice:", 1);
        while (choice > 3) {
            showMessage("Invalid option.");
            choice = readInt("Enter your choice (1, 2 or 3):", 1);
        }
        return choice;
    }

    // ---------------------------------------------------------------
    // Show recently sent messages (main menu option 2)
    // ---------------------------------------------------------------
    private static void showSentMessages() {
        String report = Message.getSentReport();
        if (report.isEmpty()) {
            showMessage("No messages have been sent yet.");
        } else {
            showReport(report);
        }
    }

    // ---------------------------------------------------------------
    // Stored Messages (main menu option 4)
    // ---------------------------------------------------------------
    private static void storedMessagesMenu() {
        Message.loadStoredMessages();   // read the JSON file into the Stored Messages arrays

        while (true) {
            String choice = ask(STORED_MENU);
            if (choice == null) {
                return;   // dialog closed: back to the main menu
            }
            switch (choice.trim()) {
                case "1" -> showStoredOrEmpty(Message.getSendersAndRecipients());
                case "2" -> showLongestMessage();
                case "3" -> searchByMessageId();
                case "4" -> searchByRecipient();
                case "5" -> deleteByHash();
                case "6" -> showStoredOrEmpty(Message.getStoredReport());
                case "0" -> {
                    return;
                }
                default -> showMessage("Invalid option. Please choose 0 to 6.");
            }
        }
    }

    // Shows stored-message text, or a notice when nothing is stored
    private static void showStoredOrEmpty(String text) {
        if (text.isEmpty()) {
            showMessage("There are no stored messages.");
        } else {
            showReport(text);
        }
    }

    private static void showLongestMessage() {
        String details = Message.getLongestStoredDetails();
        showMessage(details.isEmpty() ? "There are no stored messages."
                : "Longest stored message:\n\n" + details);
    }

    private static void searchByMessageId() {
        String id = ask("Enter the message ID to search for:");
        if (id == null) {
            return;
        }
        String[] found = Message.searchByMessageID(id.trim());
        showMessage(found == null ? "No message found with that ID."
                : "Recipient: " + found[0] + "\nMessage: " + found[1]);
    }

    private static void searchByRecipient() {
        String recipient = ask("Enter the recipient cell number to search for:");
        if (recipient == null) {
            return;
        }
        List<String> found = Message.searchByRecipient(recipient.trim());
        if (found.isEmpty()) {
            showMessage("No messages found for that recipient.");
            return;
        }
        StringBuilder result = new StringBuilder("Messages for " + recipient.trim() + ":\n");
        for (int i = 0; i < found.size(); i++) {
            result.append("\n").append(i + 1).append(") ").append(found.get(i));
        }
        showReport(result.toString());
    }

    private static void deleteByHash() {
        String hash = ask("Enter the message hash of the message to delete:");
        if (hash != null) {
            showMessage(Message.deleteByHash(hash.trim()));
        }
    }

    // ---------------------------------------------------------------
    // JOptionPane helpers
    // ---------------------------------------------------------------

    // Asks a question; returns null if the user presses Cancel or closes the dialog
    private static String ask(String prompt) {
        return JOptionPane.showInputDialog(null, prompt, TITLE, JOptionPane.QUESTION_MESSAGE);
    }

    // Asks a question and ends the program if the user presses Cancel or closes the dialog
    private static String askOrQuit(String prompt) {
        String answer = ask(prompt);
        if (answer == null) {
            System.exit(0);
        }
        return answer;
    }

    // Reads a whole number without crashing on letters
    private static int readInt(String prompt, int minimum) {
        while (true) {
            String answer = askOrQuit(prompt);
            try {
                int value = Integer.parseInt(answer.trim());
                if (value >= minimum) {
                    return value;
                }
                showMessage("Please enter a number of " + minimum + " or more.");
            } catch (NumberFormatException e) {
                showMessage("Please enter a whole number.");
            }
        }
    }

    private static void showMessage(String text) {
        JOptionPane.showMessageDialog(null, text, TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    // Long text (reports) goes in a scrolling box so it fits on the screen
    private static void showReport(String text) {
        JTextArea area = new JTextArea(text, 15, 45);
        area.setEditable(false);
        JOptionPane.showMessageDialog(null, new JScrollPane(area), TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
}