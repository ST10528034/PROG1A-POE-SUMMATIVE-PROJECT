/*
 * LoginTest.java - unit tests for registration and login (Part 1)
 */
package poe.part.pkg1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoginTest {

    private final Login login = new Login("Kyle", "Smith");

    @Test
    public void usernameCorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    public void usernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!!"));
    }

    @Test
    public void passwordMeetsComplexity() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    public void passwordDoesNotMeetComplexity() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    @Test
    public void cellNumberCorrectlyFormatted() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    public void cellNumberIncorrectlyFormatted() {
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    @Test
    public void localNumberWithoutInternationalCodeAccepted() {
        assertTrue(login.checkLocalNumber("0812345678"));
        assertTrue(login.checkCellPhoneNumber("0812345678"));
    }

    @Test
    public void localNumberWrongLengthRejected() {
        assertFalse(login.checkLocalNumber("081749106"));
        assertFalse(login.checkLocalNumber("08174219622"));
        assertFalse(login.checkCellPhoneNumber("08174A1962"));
    }

    @Test
    public void internationalNumberAccepted() {
        assertTrue(login.checkInternationalNumber("+27812345678"));
        assertTrue(login.checkCellPhoneNumber("+27812345678"));
    }

    @Test
    public void internationalNumberWithoutPlusRejected() {
        assertFalse(login.checkInternationalNumber("27817421062"));
        assertFalse(login.checkCellPhoneNumber("27817421062"));
    }

    @Test
    public void registerAcceptsInternationalNumber() {
        assertTrue(login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976")
                .endsWith("registered successfully."));
    }

    @Test
    public void registerAcceptsLocalNumber() {
        assertTrue(login.registerUser("kyl_1", "Ch&&sec@ke99!", "0838968976")
                .endsWith("registered successfully."));
    }

    @Test
    public void loginSuccessful() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void loginFailed() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.loginUser("kyl_1", "wrong"));
    }

    @Test
    public void welcomeMessageShownAfterSuccessfulLogin() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals("Welcome Kyle, Smith it is great to see you again.",
                login.returnLoginStatus("kyl_1", "Ch&&sec@ke99!"));
    }
}