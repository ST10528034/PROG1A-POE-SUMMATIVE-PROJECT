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