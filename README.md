FORTUNATE KUTLWANO MALOPE - ST10528034
# QuickChat - PROG5121 POE Part 3 SUMMATIVE PROJECT

A Java messaging app. Users register and log in, then send, disregard or store messages.
All input and output uses `JOptionPane` dialogs.

## Features
- Register and log in (username, password and cell number rules).
- Send messages: each message gets a 10-digit ID and a hash. The user can send, disregard or store it.
- Arrays: Sent, Disregarded, Stored, Message Hash and Message ID, all filled from user input.
- Stored messages are saved to `messages.json` and read back into the Stored Messages array.
- Main menu option 4, Stored Messages:
  1. Display the sender and recipient of all stored messages
  2. Display the longest stored message
  3. Search for a message ID
  4. Search for all messages stored for a recipient
  5. Delete a message using the message hash
  6. Display a report of all stored messages

## Running the app
Open the project in NetBeans (JDK 17) and run `QuickChat`.

## Unit tests
`LoginTest`, `MessageTest` and `StoredMessagesTest` (JUnit 4). GitHub Actions
(`.github/workflows/TestJava.yml`) compiles the project and runs all three on every push.

## References
- JSON.org (n.d.) *Introducing JSON*. Available at: https://www.json.org/json-en.html (Accessed: 19 November 2026).
- Oracle (n.d.) *Files (Java SE 17 API)*. Available at: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html (Accessed: 19 November 2026).
- Oracle (n.d.) *Pattern (Java SE 17 API)*. Available at: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html (Accessed: 19 November 2026).
