/*
 * Message.java - QuickChat POE Part 3
 * Holds one QuickChat message and the arrays that track every message
 * (sent, disregarded and stored) while the program is running.
 * This class does no screen output; QuickChat shows everything with JOptionPane.
 */
package poe.part.pkg1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents one QuickChat message and keeps track of every message
 * sent, disregarded or stored while the program is running.
 */
public final class Message {

    // Results that QuickChat compares against
    static final String RECIPIENT_OK = "Cell phone number successfully captured.";
    static final String MESSAGE_OK = "Message sent";

    // ---------------------------------------------------------------
    // Arrays (every list with the same prefix uses one index per message)
    // ---------------------------------------------------------------

    // Message ID and Message Hash arrays: every message handled (sent, disregarded or stored)
    private static final ArrayList<String> allMessageIds = new ArrayList<>();
    private static final ArrayList<String> allMessageHashes = new ArrayList<>();

    // Sent Message array (parallel arrays)
    private static final ArrayList<String> sentIds = new ArrayList<>();
    private static final ArrayList<String> sentHashes = new ArrayList<>();
    private static final ArrayList<String> sentRecipients = new ArrayList<>();
    private static final ArrayList<String> sentMessages = new ArrayList<>();

    // Disregarded Messages array
    private static final ArrayList<String> disregardedMessages = new ArrayList<>();

    // Stored Messages array (parallel arrays, filled from the JSON file)
    private static final ArrayList<String> storedIds = new ArrayList<>();
    private static final ArrayList<String> storedHashes = new ArrayList<>();
    private static final ArrayList<String> storedSenders = new ArrayList<>();
    private static final ArrayList<String> storedRecipients = new ArrayList<>();
    private static final ArrayList<String> storedMessages = new ArrayList<>();
    private static final ArrayList<Integer> storedNumbers = new ArrayList<>();

    // Every ID generated so far, so that IDs are unique
    private static final HashSet<String> usedIds = new HashSet<>();

    private static final Random RANDOM = new Random();
    private static String storageFile = "messages.json";
    private static int numMessageSent = 0;

    /*
     * Attribution for reading the JSON file: the JSON layout follows https://www.json.org/json-en.html ;
     * the file is read with java.nio.file.Files.readString
     * (https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html) and each
     * object and field is found with java.util.regex
     * (https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html).
     */
    // One JSON object {...}; quoted strings are skipped so braces inside a message do not break it
    private static final Pattern JSON_OBJECT =
            Pattern.compile("\\{(?:[^{}\"]|\"(?:[^\"\\\\]|\\\\.)*\")*\\}");
    // One "key": "text" or "key": number pair inside an object
    private static final Pattern JSON_FIELD =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|(-?\\d+))");

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String hash;
    private String sender = "Unknown";

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

    // Clears all arrays and counters (used between unit tests)
    public static void resetAll() {
        allMessageIds.clear();
        allMessageHashes.clear();
        sentIds.clear();
        sentHashes.clear();
        sentRecipients.clear();
        sentMessages.clear();
        disregardedMessages.clear();
        clearStoredArrays();
        usedIds.clear();
        numMessageSent = 0;
    }

    // Chooses the JSON file used for stored messages (unit tests use a temporary file)
    public static void setStorageFile(String path) {
        storageFile = path;
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

    // ---------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------

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
            return RECIPIENT_OK;
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
    }

    // Message must not exceed 250 characters
    public static String checkMessageLength(String messageText) {
        if (messageText != null && messageText.length() <= 250) {
            return MESSAGE_OK;
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

    // ---------------------------------------------------------------
    // Sending, disregarding and storing
    // ---------------------------------------------------------------

    // 1 = send, 2 = disregard, 3 = store
    public String sentMessage(int choice) {
        switch (choice) {
            case 1 -> {
                recordMessage();
                sentIds.add(messageID);
                sentHashes.add(hash);
                sentRecipients.add(recipient);
                sentMessages.add(text);
                numMessageSent++;
                return "Message successfully sent.";
            }
            case 2 -> {
                recordMessage();
                disregardedMessages.add(text);
                return "Press 0 to delete the message.";
            }
            case 3 -> {
                recordMessage();
                storeMessage();
                return "Message successfully stored.";
            }
            default -> {
                return "Invalid option.";
            }
        }
    }

    // Adds this message's ID and hash to the Message ID and Message Hash arrays
    private void recordMessage() {
        allMessageIds.add(messageID);
        allMessageHashes.add(hash);
    }

    // Adds the message to the stored arrays and saves them to the JSON file
    private void storeMessage() {
        storedIds.add(messageID);
        storedHashes.add(hash);
        storedSenders.add(sender);
        storedRecipients.add(recipient);
        storedMessages.add(text);
        storedNumbers.add(messageNumber);
        writeStoredFile();
    }

    public static int returnTotalMessages() {
        return numMessageSent;
    }

    // ---------------------------------------------------------------
    // JSON file: writing and reading
    // ---------------------------------------------------------------

    // Writes every stored message to the JSON file (a JSON array of objects)
    private static void writeStoredFile() {
        List<String> entries = new ArrayList<>();
        for (int i = 0; i < storedIds.size(); i++) {
            entries.add("  {\n"
                    + "    \"messageID\": \"" + escape(storedIds.get(i)) + "\",\n"
                    + "    \"messageNumber\": " + storedNumbers.get(i) + ",\n"
                    + "    \"sender\": \"" + escape(storedSenders.get(i)) + "\",\n"
                    + "    \"recipient\": \"" + escape(storedRecipients.get(i)) + "\",\n"
                    + "    \"message\": \"" + escape(storedMessages.get(i)) + "\",\n"
                    + "    \"messageHash\": \"" + escape(storedHashes.get(i)) + "\"\n"
                    + "  }");
        }
        try {
            Files.writeString(Paths.get(storageFile),
                    "[\n" + String.join(",\n", entries) + "\n]", StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Could not write " + storageFile + ": " + e.getMessage());
        }
    }

    // Reads the JSON file into the Stored Messages arrays (replaces what was in them)
    public static void loadStoredMessages() {
        clearStoredArrays();
        Path file = Paths.get(storageFile);
        if (!Files.exists(file)) {
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Matcher objects = JSON_OBJECT.matcher(json);
            while (objects.find()) {
                Map<String, String> fields = readFields(objects.group());
                storedIds.add(fields.getOrDefault("messageID", ""));
                storedHashes.add(fields.getOrDefault("messageHash", ""));
                storedSenders.add(fields.getOrDefault("sender", "Unknown"));
                storedRecipients.add(fields.getOrDefault("recipient", ""));
                storedMessages.add(fields.getOrDefault("message", ""));
                storedNumbers.add(Integer.parseInt(fields.getOrDefault("messageNumber", "0")));
                addToAllArraysIfMissing(storedIds.get(storedIds.size() - 1),
                        storedHashes.get(storedHashes.size() - 1));
            }
        } catch (IOException e) {
            System.err.println("Could not read " + storageFile + ": " + e.getMessage());
        }
    }

    // Keeps the Message ID and Message Hash arrays complete for messages stored in an earlier run
    private static void addToAllArraysIfMissing(String id, String messageHash) {
        if (!allMessageIds.contains(id)) {
            allMessageIds.add(id);
            allMessageHashes.add(messageHash);
        }
    }

    // Collects the "key": value pairs of one JSON object
    private static Map<String, String> readFields(String jsonObject) {
        Map<String, String> fields = new HashMap<>();
        Matcher pair = JSON_FIELD.matcher(jsonObject);
        while (pair.find()) {
            String value = pair.group(2) != null ? unescape(pair.group(2)) : pair.group(3);
            fields.put(pair.group(1), value);
        }
        return fields;
    }

    private static void clearStoredArrays() {
        storedIds.clear();
        storedHashes.clear();
        storedSenders.clear();
        storedRecipients.clear();
        storedMessages.clear();
        storedNumbers.clear();
    }

    // Escapes characters that are not allowed raw inside a JSON string
    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Reverses escape() when reading text back from the JSON file
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
                    default -> result.append(next);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // ---------------------------------------------------------------
    // Stored Messages menu features (2a - 2f)
    // ---------------------------------------------------------------

    // 2a: sender and recipient of every stored message
    public static String getSendersAndRecipients() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            result.append("Message ").append(i + 1)
                    .append("\nSender: ").append(storedSenders.get(i))
                    .append("\nRecipient: ").append(storedRecipients.get(i))
                    .append("\n\n");
        }
        return result.toString();
    }

    // 2b: position of the longest stored message (-1 if nothing is stored)
    private static int indexOfLongestStored() {
        int longestIndex = -1;
        for (int i = 0; i < storedMessages.size(); i++) {
            if (longestIndex < 0 || storedMessages.get(i).length() > storedMessages.get(longestIndex).length()) {
                longestIndex = i;
            }
        }
        return longestIndex;
    }

    // 2b: the longest stored message (empty text if nothing is stored)
    public static String getLongestStoredMessage() {
        int index = indexOfLongestStored();
        return index < 0 ? "" : storedMessages.get(index);
    }

    // 2b: full details of the longest stored message (empty text if nothing is stored)
    public static String getLongestStoredDetails() {
        int index = indexOfLongestStored();
        return index < 0 ? "" : formatEntry(index + 1, storedIds.get(index), storedHashes.get(index),
                storedSenders.get(index), storedRecipients.get(index), storedMessages.get(index));
    }

    // 2c: searches sent then stored messages; returns {recipient, message} or null if not found
    public static String[] searchByMessageID(String messageId) {
        int index = sentIds.indexOf(messageId);
        if (index >= 0) {
            return new String[]{sentRecipients.get(index), sentMessages.get(index)};
        }
        index = storedIds.indexOf(messageId);
        if (index >= 0) {
            return new String[]{storedRecipients.get(index), storedMessages.get(index)};
        }
        return null;
    }

    // 2d: every sent or stored message for one recipient
    public static List<String> searchByRecipient(String recipientNumber) {
        List<String> found = new ArrayList<>();
        collectMessages(sentRecipients, sentMessages, recipientNumber, found);
        collectMessages(storedRecipients, storedMessages, recipientNumber, found);
        return found;
    }

    // Adds every message whose recipient matches to the found list
    private static void collectMessages(List<String> recipients, List<String> texts,
            String recipientNumber, List<String> found) {
        for (int i = 0; i < recipients.size(); i++) {
            if (recipients.get(i).equals(recipientNumber)) {
                found.add(texts.get(i));
            }
        }
    }

    // 2e: deletes a stored message using its hash and updates the JSON file
    public static String deleteByHash(String messageHash) {
        int index = storedHashes.indexOf(messageHash);
        if (index < 0) {
            return "No stored message found with that hash.";
        }
        String deletedText = storedMessages.get(index);

        allMessageIds.remove(storedIds.get(index));
        allMessageHashes.remove(messageHash);
        storedIds.remove(index);
        storedHashes.remove(index);
        storedSenders.remove(index);
        storedRecipients.remove(index);
        storedMessages.remove(index);
        storedNumbers.remove(index);
        writeStoredFile();

        return "Message: \"" + deletedText + "\" successfully deleted.";
    }

    // 2f: full details of every stored message
    public static String getStoredReport() {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            report.append(formatEntry(i + 1, storedIds.get(i), storedHashes.get(i),
                    storedSenders.get(i), storedRecipients.get(i), storedMessages.get(i)));
        }
        return report.toString();
    }

    // Report of every message sent while the program is running
    public static String getSentReport() {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            report.append(formatEntry(i + 1, sentIds.get(i), sentHashes.get(i),
                    null, sentRecipients.get(i), sentMessages.get(i)));
        }
        return report.toString();
    }

    // One report entry; the sender line is left out when sender is null
    private static String formatEntry(int number, String id, String messageHash,
            String senderNumber, String recipientNumber, String messageText) {
        return "Message " + number
                + "\nMessage ID: " + id
                + "\nMessage Hash: " + messageHash
                + (senderNumber == null ? "" : "\nSender: " + senderNumber)
                + "\nRecipient: " + recipientNumber
                + "\nMessage: " + messageText
                + "\n\n";
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    public static List<String> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }

    public static List<String> getDisregardedMessages() {
        return Collections.unmodifiableList(disregardedMessages);
    }

    public static List<String> getStoredMessages() {
        return Collections.unmodifiableList(storedMessages);
    }

    public static List<String> getMessageHashes() {
        return Collections.unmodifiableList(allMessageHashes);
    }

    public static List<String> getMessageIds() {
        return Collections.unmodifiableList(allMessageIds);
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return hash;
    }

    // Sets who sent the message (defaults to "Unknown")
    public void setSender(String sender) {
        this.sender = sender;
    }
}