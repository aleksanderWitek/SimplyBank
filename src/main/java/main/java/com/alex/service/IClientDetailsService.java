package main.java.com.alex.service;

import main.java.com.alex.dto.Client;
import main.java.com.alex.dto.ClientDetails;

public interface IClientDetailsService {

    Client save(ClientDetails clientDetails);
}
