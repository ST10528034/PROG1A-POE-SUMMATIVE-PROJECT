/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poe.part.pkg1;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents one QuickChat message and keeps track of every message
 * sent, disregarded or stored while the program is running.
 */
public final class Message {

    // ---------------------------------------------------------------
    // The five arrays required by the brief (all populated at runtime)
    // ---------------------------------------------------------------
    static ArrayList<String> sentMessages = new ArrayList<>();          // text of every sent message
    static ArrayList<String> disregardedMessages = new ArrayList<>();   // text of every disregarded message
    static ArrayList<String> storedMessages = new ArrayList<>();        // text of every stored message (also read from messages.json)
    static ArrayList<String> messageHashes = new ArrayList<>();         // hash of every message
    static ArrayList<String> messageIds = new ArrayList<>();            // ID of every message

    // Parallel arrays: index i describes the same message as messageIds.get(i)
    static ArrayList<String> messageSenders = new ArrayList<>();
    static ArrayList<String> messageRecipients = new ArrayList<>();
    static ArrayList<String> messageTexts = new ArrayList<>();
    static ArrayList<Integer> messageNumbers = new ArrayList<>();
    static ArrayList<String> messageFlags = new ArrayList<>();          // Sent, Stored or Disregarded

    // Every ID generated so far, so that IDs are unique
    static HashSet<String> usedIds = new HashSet<>();

    private static final String UNKNOWN_SENDER = "Unknown";

    // Number of the person using the app (shown as the sender of stored messages)
    static String senderNumber = UNKNOWN_SENDER;

    // File that stored messages are saved to and loaded from
    private static String jsonFile = "messages.json";

    private static final String SENT = "Sent";
    private static final String STORED = "Stored";
    private static final String DISREGARDED = "Disregarded";

    private static final Random RANDOM = new Random();

    // Matches one "key": "text value" or "key": number pair in messages.json
    private static final Pattern JSON_FIELD =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|(-?\\d+))");

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String hash;

    // Creates a message with an auto-generated ten-digit ID and hash
    public Message(String recipient, String text, int messageNumber) {
        this(generateMessageID(), recipient, text, messageNumber);
    }

    // Creates a message with a known ID (used for testing)
    public Message(String messageID, String recipient, String text, int messageNumber) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
        this.hash = createMessageHash();
    }

    // Clears all static data (used between unit tests)
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIds.clear();
        messageSenders.clear();
        messageRecipients.clear();
        messageTexts.clear();
        messageNumbers.clear();
        messageFlags.clear();
        usedIds.clear();
        senderNumber = UNKNOWN_SENDER;
    }

    public static void setSenderNumber(String number) {
        senderNumber = number;
    }

    // Lets the unit tests save to a temporary file instead of the real messages.json
    public static void setJsonFile(String fileName) {
        jsonFile = fileName;
    }

    // Random, unused ten-digit ID (may start with zeros, e.g. 0012345678)
    public static String generateMessageID() {
        String id;
        do {
            long number = (long) (RANDOM.nextDouble() * 10_000_000_000L);
            id = String.format("%010d", number);
        } while (usedIds.contains(id));
        usedIds.add(id);
        return id;
    }

    // Message ID must be no more than ten characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Checks the recipient stored in this message
    public String checkRecipientCell() {
        return checkRecipientCell(recipient);
    }

    // Local number without an international code: 0 followed by nine digits, e.g. 0817421062
    public static boolean checkLocalRecipient(String cellNumber) {
        return cellNumber != null
                && cellNumber.matches("^0[0-9]{9}$");
    }

    // International number: + and a 1-3 digit country code, then up to ten digits, e.g. +27718693002
    public static boolean checkInternationalRecipient(String cellNumber) {
        return cellNumber != null
                && cellNumber.matches("^\\+[0-9]{1,3}[0-9]{1,10}$");
    }

    // Accepts either a local number or an international number
    public static String checkRecipientCell(String cellNumber) {
        if (checkLocalRecipient(cellNumber) || checkInternationalRecipient(cellNumber)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
    }

    // Message must not exceed 250 characters
    public static String checkMessageLength(String messageText) {
        if (messageText != null && messageText.length() <= 250) {
            return "Message sent";
        }
        return "Please enter a message of less than 250 characters.";
    }

    // Format: first two ID digits : message number : FIRSTWORD + LASTWORD (all caps)
    public String createMessageHash() {
        String firstTwoDigits = messageID.substring(0, 2);

        String[] words = text.trim().split("\\s+");
        String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");

        return (firstTwoDigits + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // Same as sentMessage (name used in the brief)
    public String SentMessage(int choice) {
        return sentMessage(choice);
    }

    // 1 = send, 2 = disregard, 3 = store
    public String sentMessage(int choice) {
        switch (choice) {
            case 1 -> {
                addEntry(messageID, hash, senderNumber, recipient, text, messageNumber, SENT, sentMessages);
                return "Message successfully sent.";
            }
            case 2 -> {
                addEntry(messageID, hash, senderNumber, recipient, text, messageNumber, DISREGARDED, disregardedMessages);
                return "Press 0 to delete the message.";
            }
            case 3 -> {
                addEntry(messageID, hash, senderNumber, recipient, text, messageNumber, STORED, storedMessages);
                saveStoredMessages();
                return "Message successfully stored.";
            }
            default -> {
                return "Invalid option.";
            }
        }
    }

    // Adds one message to the ID/hash arrays, its parallel arrays and the array for its flag
    private static void addEntry(String id, String hash, String sender, String recipient,
            String text, int number, String flag, ArrayList<String> flagArray) {
        messageIds.add(id);
        messageHashes.add(hash);
        messageSenders.add(sender);
        messageRecipients.add(recipient);
        messageTexts.add(text);
        messageNumbers.add(number);
        messageFlags.add(flag);
        flagArray.add(text);
    }

    // Removes the message at position index from the ID/hash arrays and the parallel arrays
    private static void removeEntry(int index) {
        messageIds.remove(index);
        messageHashes.remove(index);
        messageSenders.remove(index);
        messageRecipients.remove(index);
        messageTexts.remove(index);
        messageNumbers.remove(index);
        messageFlags.remove(index);
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    // ---------------------------------------------------------------
    // Reports
    // ---------------------------------------------------------------

    // Full details of every message sent while the program is running
    public static String printMessages() {
        return buildReport(SENT);
    }

    // Full details of every stored message (menu option f)
    public static String displayStoredReport() {
        return buildReport(STORED);
    }

    // Builds a report of all messages that have the given flag
    private static String buildReport(String flag) {
        StringBuilder report = new StringBuilder();
        int count = 0;
        for (int i = 0; i < messageIds.size(); i++) {
            if (messageFlags.get(i).equals(flag)) {
                count++;
                report.append("Message ").append(count)
                        .append("\nMessage ID: ").append(messageIds.get(i))
                        .append("\nMessage Hash: ").append(messageHashes.get(i))
                        .append("\nSender: ").append(messageSenders.get(i))
                        .append("\nRecipient: ").append(messageRecipients.get(i))
                        .append("\nMessage: ").append(messageTexts.get(i))
                        .append("\nStatus: ").append(flag)
                        .append("\n\n");
            }
        }
        if (count == 0) {
            return "No " + flag.toLowerCase() + " messages.";
        }
        return report.toString();
    }

    // ---------------------------------------------------------------
    // Stored Messages menu features
    // ---------------------------------------------------------------

    // a. Sender and recipient of every stored message
    public static String displaySendersAndRecipients() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < messageIds.size(); i++) {
            if (messageFlags.get(i).equals(STORED)) {
                result.append("Sender: ").append(messageSenders.get(i))
                        .append(" | Recipient: ").append(messageRecipients.get(i))
                        .append("\n");
            }
        }
        return result.length() == 0 ? "No stored messages." : result.toString();
    }

    // b. The longest stored message
    public static String getLongestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        String longest = storedMessages.get(0);
        for (String stored : storedMessages) {
            if (stored.length() > longest.length()) {
                longest = stored;
            }
        }
        return longest;
    }

    // c. Finds a message by its ID; returns {recipient, message} or null if the ID is unknown
    public static String[] searchByMessageId(String id) {
        int index = messageIds.indexOf(id);
        if (index < 0) {
            return null;
        }
        return new String[]{messageRecipients.get(index), messageTexts.get(index)};
    }

    // d. Every message sent or stored for the recipient (disregarded messages are skipped)
    public static ArrayList<String> searchByRecipient(String recipientNumber) {
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < messageIds.size(); i++) {
            if (messageRecipients.get(i).equals(recipientNumber)
                    && !messageFlags.get(i).equals(DISREGARDED)) {
                found.add(messageTexts.get(i));
            }
        }
        return found;
    }

    // e. Deletes a stored message using its hash and updates messages.json
    public static String deleteByHash(String hashToDelete) {
        for (int i = 0; i < messageHashes.size(); i++) {
            if (messageHashes.get(i).equals(hashToDelete) && messageFlags.get(i).equals(STORED)) {
                String deletedText = messageTexts.get(i);
                storedMessages.remove(positionAmongStored(i));
                removeEntry(i);
                saveStoredMessages();
                return "Message: \"" + deletedText + "\" successfully deleted.";
            }
        }
        return "No stored message found with that hash.";
    }

    // Position of the message at index within the storedMessages array
    private static int positionAmongStored(int index) {
        int position = 0;
        for (int i = 0; i < index; i++) {
            if (messageFlags.get(i).equals(STORED)) {
                position++;
            }
        }
        return position;
    }

    // ---------------------------------------------------------------
    // Reading and writing messages.json
    // ---------------------------------------------------------------

    /*
     * Saves every stored message to the JSON file (a JSON array of objects).
     * Attribution: JSON structure follows the format described at https://www.json.org ;
     * file writing uses FileWriter inside try-with-resources as documented at
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/FileWriter.html
     */
    public static void saveStoredMessages() {
        ArrayList<String> entries = new ArrayList<>();
        for (int i = 0; i < messageIds.size(); i++) {
            if (messageFlags.get(i).equals(STORED)) {
                entries.add("  {\n"
                        + "    \"messageID\": \"" + escape(messageIds.get(i)) + "\",\n"
                        + "    \"messageNumber\": " + messageNumbers.get(i) + ",\n"
                        + "    \"sender\": \"" + escape(messageSenders.get(i)) + "\",\n"
                        + "    \"recipient\": \"" + escape(messageRecipients.get(i)) + "\",\n"
                        + "    \"message\": \"" + escape(messageTexts.get(i)) + "\",\n"
                        + "    \"messageHash\": \"" + escape(messageHashes.get(i)) + "\"\n"
                        + "  }");
            }
        }
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write("[\n" + String.join(",\n", entries) + "\n]");
        } catch (IOException e) {
            System.out.println("Could not write " + jsonFile + ": " + e.getMessage());
        }
    }

    /*
     * Reads messages.json into the arrays (called once when the app starts).
     * Attribution: reading a whole file with Files.readString as documented at
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html ;
     * the values are pulled out with a java.util.regex Pattern/Matcher as documented at
     * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
     */
    public static void loadStoredMessages() {
        Path path = Paths.get(jsonFile);
        if (!Files.exists(path)) {
            return;
        }
        try {
            Matcher matcher = JSON_FIELD.matcher(Files.readString(path));
            String[] fields = new String[6];   // id, number, sender, recipient, message, hash
            boolean started = false;

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2) != null ? unescape(matcher.group(2)) : matcher.group(3);

                // "messageID" is the first key of every object, so it starts a new message
                if (key.equals("messageID")) {
                    if (started) {
                        addLoadedMessage(fields);
                    }
                    fields = new String[6];
                    started = true;
                }
                switch (key) {
                    case "messageID" -> fields[0] = value;
                    case "messageNumber" -> fields[1] = value;
                    case "sender" -> fields[2] = value;
                    case "recipient" -> fields[3] = value;
                    case "message" -> fields[4] = value;
                    case "messageHash" -> fields[5] = value;
                    default -> { }
                }
            }
            if (started) {
                addLoadedMessage(fields);
            }
        } catch (IOException e) {
            System.out.println("Could not read " + jsonFile + ": " + e.getMessage());
        }
    }

    // Adds one message read from the JSON file to the stored arrays
    private static void addLoadedMessage(String[] fields) {
        String sender = fields[2] != null ? fields[2] : UNKNOWN_SENDER;
        int number = fields[1] != null ? Integer.parseInt(fields[1]) : 0;
        addEntry(fields[0], fields[5], sender, fields[3], fields[4], number, STORED, storedMessages);
        usedIds.add(fields[0]);
    }

    // Escapes characters that are not allowed raw inside a JSON string
    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Reverses escape() for a value read from the JSON file
    private static String unescape(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    default -> result.append(next);   // \" and \\
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return hash;
    }

    public int getMessageNumber() {
        return messageNumber;
    }
}