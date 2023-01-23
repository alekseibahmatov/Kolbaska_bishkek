package ee.kolbaska.kolbaska.service.miscellaneous;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FormatService {
    private static final String PHONE_REGEX = "^\\+(?:[0-9] ?){6,14}[0-9]$";
    private static final Pattern PATTERN = Pattern.compile(PHONE_REGEX);

    public String formatE164(String phoneNumber) {
        if (phoneNumber == null) {
            throw new NullPointerException("Phone number not found");
        }

        Matcher matcher = PATTERN.matcher(phoneNumber);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        String formattedPhoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
        if (formattedPhoneNumber.length() > 15) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        if (!formattedPhoneNumber.startsWith("+")) {
            formattedPhoneNumber = "+" + formattedPhoneNumber;
        }

        return formattedPhoneNumber;
    }
}
