package ee.kolbaska.kolbaska.service.miscellaneous;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatServiceTest {
    private FormatService formatService = new FormatService();

    @Test
    void testFormatE164() {
        // Test valid phone number
        String phoneNumber = "+1234567890";
        String expected = "+1234567890";
        String result = formatService.formatE164(phoneNumber);
        assertEquals(expected, result);

        // Test invalid phone number format
        phoneNumber = "1234567890";
        try {
            formatService.formatE164(phoneNumber);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid phone number format", e.getMessage());
        }
    }


    @Test
    void testFormatE164_NullPhoneNumber() {
        String phoneNumber = null;
        assertThrows(NullPointerException.class, () -> formatService.formatE164(phoneNumber));
    }

    @Test
    void testFormatE164_InvalidFormat() {
        String phoneNumber = "invalid_format";
        assertThrows(IllegalArgumentException.class, () -> formatService.formatE164(phoneNumber));
    }

    @Test
    void testFormatE164_LengthGreaterThan15() {
        String phoneNumber = "+1234567890123456";
        assertThrows(IllegalArgumentException.class, () -> formatService.formatE164(phoneNumber));
    }
}