package main.java.com.alex.service;

import main.java.com.alex.exception.IllegalArgumentRuntimeException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class UserAccountProcessingService implements IUserAccountProcessingService{

    @Override
    public String generateLogin(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentRuntimeException("firstName and lastName must be provided");
        }
        String first = firstName.trim();
        String last = lastName.trim();
        if (first.isEmpty() || last.isEmpty()) {
            throw new IllegalArgumentRuntimeException("firstName and lastName must be non-blank");
        }

        while(first.length() < 3) {
            first += first;
        }
        while(last.length() < 3) {
            last += last;
        }

        StringBuilder login = new StringBuilder();
        login.append(first, 0, 3);
        login.append(last, 0, 3);
        login.append(UUID.randomUUID().toString().replace("-", ""), 0, 8);

        return login.toString().toLowerCase();
    }

    @Override
    public String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        addToPasswordCapitalLetters(password, random, 4);
        addToPasswordSmallLetters(password, random, 4);
        addToPasswordNumbers(password, random, 2);
        addToPasswordSpecialSymbols(password, random, 2);

        return shufflePassword(password.toString(), random);
    }

    private String shufflePassword(String password, SecureRandom random) {
        char[] result = password.toCharArray();
        for (int i = result.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = result[i];
            result[i] = result[j];
            result[j] = temp;
        }
        return new String(result);
    }

    private void addToPasswordSpecialSymbols(StringBuilder password, SecureRandom random, int count) {
        String special = "!@#$%^&*";
        for (int i = 0; i < count; i++) {
            password.append(special.charAt(random.nextInt(special.length())));
        }
    }

    private void addToPasswordNumbers(StringBuilder password, SecureRandom random, int count) {
        for (int i = 0; i < count; i++) {
            password.append(random.nextInt(10));
        }
    }

    private void addToPasswordSmallLetters(StringBuilder password, SecureRandom random, int count) {
        for (int i = 0; i < count; i++) {
            password.append((char) (random.nextInt(26) + 'a'));
        }
    }

    private void addToPasswordCapitalLetters(StringBuilder password, SecureRandom random, int count) {
        for (int i = 0; i < count; i++) {
            password.append((char) (random.nextInt(26) + 'A'));
        }
    }
}
