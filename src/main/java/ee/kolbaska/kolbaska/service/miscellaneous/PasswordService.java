package ee.kolbaska.kolbaska.service.miscellaneous;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMERIC_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#%^&*_=+-/";

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 12;

    private static final Random RANDOM = new SecureRandom();

    public String generatePassword(int length) {
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("Password length must be between 8 and 12 characters");
        }

        StringBuilder password = new StringBuilder();
        password.append(getRandomChar(LOWERCASE_CHARS));
        password.append(getRandomChar(UPPERCASE_CHARS));
        password.append(getRandomChar(NUMERIC_CHARS));
        password.append(getRandomChar(SPECIAL_CHARS));

        for (int i = 0; i < length - 4; i++) {
            String charSet = getCharSet(i);
            password.append(getRandomChar(charSet));
        }

        return shuffleString(password.toString());
    }

    private static String getCharSet(int index) {
        switch (index % 4) {
            case 0:
                return LOWERCASE_CHARS;
            case 1:
                return UPPERCASE_CHARS;
            case 2:
                return NUMERIC_CHARS;
            case 3:
                return SPECIAL_CHARS;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static char getRandomChar(String charSet) {
        int index = RANDOM.nextInt(charSet.length());
        return charSet.charAt(index);
    }

    private static String shuffleString(String string) {
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int randomIndex = RANDOM.nextInt(chars.length);
            char temp = chars[i];
            chars[i] = chars[randomIndex];
            chars[randomIndex] = temp;
        }
        return new String(chars);
    }
}
