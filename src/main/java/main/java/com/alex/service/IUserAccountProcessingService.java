package main.java.com.alex.service;

public interface IUserAccountProcessingService {
    String generateLogin(String firstName, String lastName);
    String generatePassword();
}
