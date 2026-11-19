/*
 * StoredMessagesTest test which uses the five test data messages from the question paper to test the arrays
 */
package poe.part.pkg1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class StoredMessagesTest {

    // Test data from the question paper
    private static final String RECIPIENT_1 = "+27834557896";
    private static final String MESSAGE_1 = "Did you get the cake?";

    private static final String RECIPIENT_2 = "+27838884567";
    private static final String MESSAGE_2 = "Where are you? You are late! I have asked you to be on time.";

    private static final String RECIPIENT_3 = "+27834484567";
    private static final String MESSAGE_3 = "Yohoooo, I am at your gate.";

    private static final String DEVELOPER_4 = "0838884567";   // also used as message 4's ID
    private static final String MESSAGE_4 = "It is dinner time!";

    private static final String RECIPIENT_5 = "+27838884567";
    private static final String MESSAGE_5 = "Ok, I am leaving without you.";

    // sentMessage choices: 1 = send, 2 = disregard, 3 = store
    private static final int SEND = 1;
    private static final int DISREGARD = 2;
    private static final int STORE = 3;

    // A temporary folder keeps the tests from overwriting the real messages.json
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    // Creates the five test messages, applies their flags and reads the JSON file back
    @Before
    public void loadTestData() {
        Message.resetAll();
        Message.setStorageFile(new File(tempFolder.getRoot(), "messages.json").getPath());

        message1 = new Message("0000000001", RECIPIENT_1, MESSAGE_1, 0);
        message2 = new Message("0000000002", RECIPIENT_2, MESSAGE_2, 1);
        message3 = new Message("0000000003", RECIPIENT_3, MESSAGE_3, 2);
        message4 = new Message(DEVELOPER_4, DEVELOPER_4, MESSAGE_4, 3);
        message5 = new Message("0000000005", RECIPIENT_5, MESSAGE_5, 4);

        message1.sentMessage(SEND);
        message2.sentMessage(STORE);
        message3.sentMessage(DISREGARD);
        message4.sentMessage(SEND);
        message5.sentMessage(STORE);

        Message.loadStoredMessages();
    }

    // ---- Arrays correctly populated ----

    @Test
    public void sentMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList("Did you get the cake?", "It is dinner time!"),
                Message.getSentMessages());
    }

    @Test
    public void disregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList(MESSAGE_3), Message.getDisregardedMessages());
    }

    @Test
    public void storedMessagesArrayReadFromJsonFile() {
        assertEquals(Arrays.asList(MESSAGE_2, MESSAGE_5), Message.getStoredMessages());
    }

    @Test
    public void messageIdAndHashArraysContainEveryMessage() {
        assertEquals(Arrays.asList("0000000001", "0000000002", "0000000003", DEVELOPER_4, "0000000005"),
                Message.getMessageIds());
        assertEquals(Arrays.asList(message1.getMessageHash(), message2.getMessageHash(),
                message3.getMessageHash(), message4.getMessageHash(), message5.getMessageHash()),
                Message.getMessageHashes());
    }

    // ---- Display the longest message ----

    @Test
    public void displayLongestMessage() {
        assertEquals(MESSAGE_2, Message.getLongestStoredMessage());
    }

    // ---- Search for messageID ----

    @Test
    public void searchForMessageId() {
        String[] found = Message.searchByMessageID(DEVELOPER_4);

        assertNotNull(found);
        assertEquals(DEVELOPER_4, found[0]);
        assertEquals("It is dinner time!", found[1]);
    }

    @Test
    public void searchForStoredMessageId() {
        assertEquals(MESSAGE_5, Message.searchByMessageID("0000000005")[1]);
    }

    @Test
    public void searchForUnknownMessageIdFindsNothing() {
        assertNull(Message.searchByMessageID("9999999999"));
    }

    // ---- Search all messages for a particular recipient ----

    @Test
    public void searchMessagesForRecipient() {
        assertEquals(Arrays.asList(MESSAGE_2, MESSAGE_5), Message.searchByRecipient("+27838884567"));
    }

    @Test
    public void searchMessagesForUnknownRecipientFindsNothing() {
        assertTrue(Message.searchByRecipient("+27000000000").isEmpty());
    }

    // ---- Delete a message using a message hash ----

    @Test
    public void deleteMessageUsingMessageHash() {
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" "
                + "successfully deleted.",
                Message.deleteByHash(message2.getMessageHash()));
        assertEquals(Arrays.asList(MESSAGE_5), Message.getStoredMessages());
    }

    @Test
    public void deletedMessageIsAlsoRemovedFromJsonFile() {
        Message.deleteByHash(message2.getMessageHash());

        Message.loadStoredMessages();   // read the rewritten file again

        assertEquals(Arrays.asList(MESSAGE_5), Message.getStoredMessages());
    }

    @Test
    public void deleteWithUnknownHashDeletesNothing() {
        assertEquals("No stored message found with that hash.", Message.deleteByHash("00:0:NOPE"));
        assertEquals(2, Message.getStoredMessages().size());
    }

    // ---- Display report ----

    @Test
    public void displayStoredReportShowsHashRecipientAndMessage() {
        String report = Message.getStoredReport();

        assertTrue(report.contains(message2.getMessageHash()));
        assertTrue(report.contains(RECIPIENT_2));
        assertTrue(report.contains(MESSAGE_2));
        assertTrue(report.contains(message5.getMessageHash()));
        assertTrue(report.contains(RECIPIENT_5));
        assertTrue(report.contains(MESSAGE_5));
    }

    @Test
    public void displaySentReportShowsHashRecipientAndMessage() {
        String report = Message.getSentReport();

        assertTrue(report.contains(message1.getMessageHash()));
        assertTrue(report.contains(RECIPIENT_1));
        assertTrue(report.contains(MESSAGE_1));
        assertTrue(report.contains(message4.getMessageHash()));
        assertTrue(report.contains(DEVELOPER_4));
        assertTrue(report.contains(MESSAGE_4));
    }

    // ---- Display the sender and recipient of all stored messages ----

    @Test
    public void displaySenderAndRecipientOfStoredMessages() {
        String result = Message.getSendersAndRecipients();

        assertTrue(result.contains("Sender: Unknown"));
        assertTrue(result.contains("Recipient: " + RECIPIENT_2));
        assertTrue(result.contains("Recipient: " + RECIPIENT_5));
    }
}