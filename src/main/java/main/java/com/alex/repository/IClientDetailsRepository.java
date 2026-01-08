package main.java.com.alex.repository;

import main.java.com.alex.dto.ClientDetails;

public interface IClientDetailsRepository {

    ClientDetails save(ClientDetails clientDetails);
}
