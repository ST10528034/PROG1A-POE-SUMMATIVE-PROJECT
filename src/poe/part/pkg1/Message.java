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