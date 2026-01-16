package main.java.com.alex.rowMapper;

import main.java.com.alex.dto.Client;
import main.java.com.alex.exception.SQLRuntimeException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientRowMapper implements RowMapper<Client> {
    @Override
    public Client mapRow(ResultSet rs, int rowNum) {
        try {
            return new Client(
                    rs.getLong("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("city"),
                    rs.getString("street"),
                    rs.getString("house_number"),
                    rs.getString("identification_number")
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
