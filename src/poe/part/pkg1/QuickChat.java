/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */



package poe.part.pkg1;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * QuickChat: register, log in, then send messages from a numeric menu.
 */
public class QuickChat {

    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        Login login = registerAndLogin();
        Message.setSenderNumber(login.getCellNumber());
        Message.loadStoredMessages();   // fills the stored arrays from messages.json

        System.out.println("\n======================");
        System.out.println("WELCOME TO QUICKCHAT!");
        System.out.println("======================");

        int totalAllowed = readInt("How many messages do you wish to enter? ", 1);
        int entered = 0;   // number of messages handled so far (also the next message number)
        int option = 0;

        while (option != 3) {
            System.out.println("\n===== Menu =====");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            option = readInt("Choose an option: ", 0);

            switch (option) {
                case 1 -> entered = sendMessages(entered, totalAllowed);
                case 2 -> System.out.println(Message.printMessages());
                case 3 -> System.out.println("Goodbye. Total messages sent: "
                            + Message.returnTotalMessages());
                case 4 -> storedMessagesMenu();
                default -> System.out.println("Invalid option. Please choose 1, 2, 3 or 4.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Stored Messages menu: works with the arrays in the Message class
    // ---------------------------------------------------------------
    private static void storedMessagesMenu() {
        int choice = -1;
        while (choice != 0) {
            System.out.println("\n===== Stored Messages =====");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for messages stored for a recipient");
            System.out.println("5) Delete a message using its hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("0) Back to main menu");
            choice = readInt("Choose an option: ", 0);

            switch (choice) {
                case 1 -> System.out.println(Message.displaySendersAndRecipients());
                case 2 -> System.out.println("Longest stored message: " + Message.getLongestStoredMessage());
                case 3 -> searchById();
                case 4 -> searchByRecipient();
                case 5 -> deleteByHash();
                case 6 -> System.out.println(Message.displayStoredReport());
                case 0 -> System.out.println("Returning to main menu.");
                default -> System.out.println("Invalid option. Please choose 0 to 6.");
            }
        }
    }

    private static void searchById() {
        System.out.print("Enter the message ID: ");
        String[] found = Message.searchByMessageId(INPUT.nextLine().trim());
        if (found == null) {
            System.out.println("No message found with that ID.");
        } else {
            System.out.println("Recipient: " + found[0]);
            System.out.println("Message: " + found[1]);
        }
    }

    private static void searchByRecipient() {
        System.out.print("Enter the recipient cell number: ");
        ArrayList<String> found = Message.searchByRecipient(INPUT.nextLine().trim());
        if (found.isEmpty()) {
            System.out.println("No messages found for that recipient.");
        }
        for (String text : found) {
            System.out.println("Message: " + text);
        }
    }

    private static void deleteByHash() {
        System.out.print("Enter the message hash: ");
        System.out.println(Message.deleteByHash(INPUT.nextLine().trim()));
    }

    // ---------------------------------------------------------------
    // Registration and login
    // ---------------------------------------------------------------
    private static Login registerAndLogin() {
        System.out.println("=== Register ===");
        System.out.print("Enter your first name: ");
        String firstName = INPUT.nextLine().trim();
        System.out.print("Enter your last name: ");
        String lastName = INPUT.nextLine().trim();

        Login login = new Login(firstName, lastName);
        boolean registered = false;
        String username = "";
        String password = "";

        while (!registered) {
            System.out.print("Enter username (underscore, max 5 characters): ");
            username = INPUT.nextLine().trim();
            System.out.print("Enter password (8+ chars, capital, number, special): ");
            password = INPUT.nextLine();
            System.out.print("Enter cell number (e.g. +27718693002 / 0712345678): ");
            String cell = INPUT.nextLine().trim();

            String result = login.registerUser(username, password, cell);
            System.out.println(result);
            registered = result.endsWith("registered successfully.");
        }

        System.out.println("\n=== Login ===");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter username: ");
            String enteredUser = INPUT.nextLine().trim();
            System.out.print("Enter password: ");
            String enteredPass = INPUT.nextLine();

            System.out.println(login.returnLoginStatus(enteredUser, enteredPass));
            loggedIn = login.loginUser(enteredUser, enteredPass);
        }
        return login;
    }

    // ---------------------------------------------------------------
    // Sending messages: runs until the user's chosen number is reached
    // ---------------------------------------------------------------
    private static int sendMessages(int entered, int totalAllowed) {
        if (entered >= totalAllowed) {
            System.out.println("You have already entered all " + totalAllowed + " messages.");
            return entered;
        }

        // The loop counter i is the message number (starts at 0)
        for (int i = entered; i < totalAllowed; i++) {
            System.out.println("\n==============================");
            System.out.println("MESSAGE " + (i + 1) + " of " + totalAllowed);
            System.out.println("==============================");

            String recipient = askRecipient();
            String text = askMessageText();

            Message message = new Message(recipient, text, i);
            System.out.println("Message ID generated: " + message.getMessageID());
            System.out.println("Message Hash: " + message.getMessageHash());

            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int choice = readInt("Enter your choice: ", 1);
            while (choice < 1 || choice > 3) {
                System.out.println("Invalid option.");
                choice = readInt("Enter your choice: ", 1);
            }

            System.out.println(message.SentMessage(choice));

            if (choice == 1) {
                System.out.println("\n" + Message.printMessages());
            } else if (choice == 2) {
                int deleteChoice = readInt("", 0);
                if (deleteChoice == 0) {
                    System.out.println("Message deleted.");
                }
            }
            entered = i + 1;
        }

        System.out.println("Total messages sent: " + Message.returnTotalMessages());
        return entered;
    }

    private static String askRecipient() {
        while (true) {
            System.out.print("Enter recipient cell number (e.g. +27718693002): ");
            String recipient = INPUT.nextLine().trim();
            String result = Message.checkRecipientCell(recipient);
            System.out.println(result);
            if (result.equals("Cell phone number successfully captured.")) {
                return recipient;
            }
        }
    }

    private static String askMessageText() {
        while (true) {
            System.out.print("Enter your message: ");
            String text = INPUT.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("Message cannot be empty.");
                continue;
            }
            String result = Message.checkMessageLength(text);
            System.out.println(result);
            if (result.equals("Message sent")) {
                return text;
            }
        }
    }

    // Reads a whole number without crashing on letters (minimum is only used for the prompt loop)
    private static int readInt(String prompt, int minimum) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            String line = INPUT.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= minimum) {
                    return value;
                }
                System.out.println("Please enter a number of " + minimum + " or more.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}