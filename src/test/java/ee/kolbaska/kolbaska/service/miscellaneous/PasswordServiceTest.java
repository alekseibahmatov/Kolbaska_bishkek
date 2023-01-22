package ee.kolbaska.kolbaska.service.miscellaneous;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordServiceTest {

    private PasswordService passwordService = new PasswordService();

    @Test
    void testGeneratePassword() {
        int length = 8;
        String password = passwordService.generatePassword(length);
        assert password.length() == length;
        assert password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#%^&*_=+-/])[a-zA-Z\\d!@#%^&*_=+-/]*$");
    }

    @Test
    void testGeneratePassword_LengthLessThan8() {
        int length = 7;
        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePassword(length));
    }

    @Test
    void testGeneratePassword_LengthGreaterThan12() {
        int length = 13;
        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePassword(length));
    }
}