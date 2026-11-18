/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */

package poe.part.pkg1;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MessageTest {

    private static final String TEXT =
            "Hi Mike, can you join us for dinner tonight?";
    private static final String NUMBER = "+27718693002";

    // Test data from the brief (message 4 is entered by the developer)
    private static final String[] TEST_IDS = {
        "1000000001", "2000000002", "3000000003", "0838884567", "5000000005"
    };
    private static final String[] TEST_RECIPIENTS = {
        "+27834557896", "+27838884567", "+27834484567", "0838884567", "+27838884567"
    };
    private static final String[] TEST_MESSAGES = {
        "Did you get the cake?",
        "Where are you? You are late! I have asked you to be on time.",
        "Yohoooo, I am at your gate.",
        "It is dinner time!",
        "Ok, I am leaving without you."
    };
    // 1 = Sent, 2 = Disregard, 3 = Stored
    private static final int[] TEST_FLAGS = {1, 3, 2, 1, 3};

    // Temporary folder so the tests never overwrite the real messages.json
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File jsonFile;

    @Before
    public void setUp() {
        Message.resetAll();
        jsonFile = new File(folder.getRoot(), "messages.json");
        Message.setJsonFile(jsonFile.getPath());
    }

    // Creates the first "count" test messages and sends, disregards or stores each one
    private void loadTestData(int count) {
        for (int i = 0; i < count; i++) {
            new Message(TEST_IDS[i], TEST_RECIPIENTS[i], TEST_MESSAGES[i], i)
                    .sentMessage(TEST_FLAGS[i]);
        }
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        loadTestData(4);

        assertEquals(
                Arrays.asList("Did you get the cake?", "It is dinner time!"),
                Message.sentMessages
        );
    }

    @Test
    public void testDisregardedAndStoredArraysPopulated() {
        loadTestData(5);

        assertEquals(Arrays.asList(TEST_MESSAGES[2]), Message.disregardedMessages);
        assertEquals(Arrays.asList(TEST_MESSAGES[1], TEST_MESSAGES[4]), Message.storedMessages);
    }

    @Test
    public void testMessageIdAndHashArraysPopulated() {
        loadTestData(5);

        assertEquals(Arrays.asList(TEST_IDS), Message.messageIds);
        assertEquals(5, Message.messageHashes.size());
        assertEquals("20:1:WHERETIME", Message.messageHashes.get(1));
    }

    @Test
    public void testDisplayLongestMessage() {
        loadTestData(4);

        assertEquals(
                "Where are you? You are late! I have asked you to be on time.",
                Message.getLongestStoredMessage()
        );
    }

    @Test
    public void testSearchForMessageId() {
        loadTestData(4);

        String[] found = Message.searchByMessageId("0838884567");

        assertEquals("It is dinner time!", found[1]);
        assertEquals("0838884567", found[0]);
    }

    @Test
    public void testSearchForMessageIdNotFound() {
        loadTestData(4);

        assertNull(Message.searchByMessageId("9999999999"));
    }

    @Test
    public void testSearchMessagesForRecipient() {
        loadTestData(5);

        assertEquals(
                Arrays.asList(
                        "Where are you? You are late! I have asked you to be on time.",
                        "Ok, I am leaving without you."
                ),
                Message.searchByRecipient("+27838884567")
        );
    }

    @Test
    public void testDeleteMessageUsingHash() {
        loadTestData(5);

        assertEquals(
                "Message: \"Where are you? You are late! I have asked you to be on time.\""
                + " successfully deleted.",
                Message.deleteByHash("20:1:WHERETIME")
        );
        assertEquals(Arrays.asList(TEST_MESSAGES[4]), Message.storedMessages);
        assertFalse(Message.messageHashes.contains("20:1:WHERETIME"));
    }

    @Test
    public void testDeleteMessageHashNotFound() {
        loadTestData(5);

        assertEquals(
                "No stored message found with that hash.",
                Message.deleteByHash("00:0:NOTHING")
        );
        assertEquals(2, Message.storedMessages.size());
    }

    @Test
    public void testDisplayReport() {
        loadTestData(4);

        String report = Message.printMessages();

        assertFalse(report.contains("20:1:WHERETIME"));   // stored, not sent
        assertTrue(report.contains("10:0:DIDCAKE"));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains("08:3:ITTIME"));
        assertTrue(report.contains("0838884567"));
        assertTrue(report.contains("It is dinner time!"));
    }

    @Test
    public void testDisplayStoredReport() {
        loadTestData(5);

        String report = Message.displayStoredReport();

        assertTrue(report.contains("20:1:WHERETIME"));
        assertTrue(report.contains("+27838884567"));
        assertTrue(report.contains(TEST_MESSAGES[1]));
        assertTrue(report.contains(TEST_MESSAGES[4]));
        assertFalse(report.contains(TEST_MESSAGES[0]));
    }

    @Test
    public void testDisplaySendersAndRecipients() {
        Message.setSenderNumber("+27718693002");
        loadTestData(5);

        assertEquals(
                "Sender: +27718693002 | Recipient: +27838884567\n"
                + "Sender: +27718693002 | Recipient: +27838884567\n",
                Message.displaySendersAndRecipients()
        );
    }

    @Test
    public void testStoredMessagesReadFromJsonFile() throws IOException {
        Message.setSenderNumber("+27718693002");
        loadTestData(5);
        Message.resetAll();   // empty the arrays; messages.json still holds the stored messages

        Message.loadStoredMessages();

        assertEquals(Arrays.asList(TEST_MESSAGES[1], TEST_MESSAGES[4]), Message.storedMessages);
        assertEquals("20:1:WHERETIME", Message.messageHashes.get(0));
        assertEquals("+27718693002", Message.messageSenders.get(0));
        assertTrue(Files.exists(jsonFile.toPath()));
    }

    @Test
    public void testRecipientCorrectlyFormatted() {
        Message m = new Message("0012345678", NUMBER, TEXT, 0);

        assertEquals(
                "Cell phone number successfully captured.",
                m.checkRecipientCell()
        );
    }

    @Test
    public void testRecipientIncorrectlyFormatted() {
        Message m = new Message(
                "0012345678", "071869", TEXT, 0
        );

        assertEquals(
                "Cell phone number is incorrectly formatted or does not "
                + "contain an international code. Please correct the number "
                + "and try again.",
                m.checkRecipientCell()
        );
    }

    @Test
    public void testRecipientLocalNumberAccepted() {
        Message m = new Message("0012345678", "0817421062", TEXT, 0);

        assertTrue(Message.checkLocalRecipient("0817421062"));
        assertEquals(
                "Cell phone number successfully captured.",
                m.checkRecipientCell()
        );
    }

    @Test
    public void testRecipientInternationalNumberAccepted() {
        assertTrue(Message.checkInternationalRecipient("+27817421062"));
        assertFalse(Message.checkInternationalRecipient("0817421062"));
        assertFalse(Message.checkLocalRecipient("+27817421062"));
    }

    @Test
    public void testMessageHashCorrect() {
        Message m = new Message("0012345678", NUMBER, TEXT, 0);

        assertEquals("00:0:HITONIGHT", m.createMessageHash());
    }

    @Test
    public void testMessageHashesInLoop() {
        String[] texts = {
            "Hi, thanks",
            "Hi Mike, can you join us for dinner tonight?",
            "Where are you? You are late! I have asked you to be on time.",
            "Ok, I am leaving without you."
        };

        String[] expected = {
            "00:0:HITHANKS",
            "00:1:HITONIGHT",
            "00:2:WHERETIME",
            "00:3:OKYOU"
        };

        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(
                    "0012345678", NUMBER, texts[i], i
            );

            assertEquals(expected[i], m.createMessageHash());
        }
    }

    @Test
    public void testMessageIDCreated() {
        Message m = new Message(NUMBER, TEXT, 0);

        assertNotNull(m.getMessageID());
        assertEquals(10, m.getMessageID().length());
        assertTrue(m.checkMessageID());

        System.out.println(
                "Message ID generated: " + m.getMessageID()
        );
    }

    @Test
    public void testMessageIDTooLong() {
        Message m = new Message("12345678901", NUMBER, TEXT, 0);

        assertFalse(m.checkMessageID());
    }

    @Test
    public void testMessageLengthTooLong() {
        String longText = "a".repeat(251);

        assertEquals(
                "Please enter a message of less than 250 characters.",
                Message.checkMessageLength(longText)
        );
    }

    @Test
    public void testMessageLengthOk() {
        assertEquals(
                "Message sent",
                Message.checkMessageLength(TEXT)
        );
    }

    @Test
    public void testSendMessage() {
        Message m = new Message("0012345678", NUMBER, TEXT, 0);

        assertEquals(
                "Message successfully sent.",
                m.sentMessage(1)
        );

        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testDisregardMessage() {
        Message m = new Message("0012345678", NUMBER, TEXT, 0);

        assertEquals(
                "Press 0 to delete the message.",
                m.sentMessage(2)
        );

        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void testStoreMessage() {
        Message m = new Message("0012345678", NUMBER, TEXT, 0);

        assertEquals(
                "Message successfully stored.",
                m.sentMessage(3)
        );

        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void testReturnTotalMessages() {
        new Message("0012345678", NUMBER, TEXT, 0)
                .sentMessage(1);

        new Message("0112345678", NUMBER, "Hi, thanks", 1)
                .sentMessage(1);

        assertEquals(2, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages() {
        Message m = new Message("0012345678", NUMBER, TEXT, 0);
        m.sentMessage(1);

        String printed = Message.printMessages();

        assertTrue(printed.contains("0012345678"));
        assertTrue(printed.contains("00:0:HITONIGHT"));
        assertTrue(printed.contains(NUMBER));
        assertTrue(printed.contains(TEXT));
    }
}