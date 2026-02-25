package com.alex.repository.mapper;

import com.alex.dto.ClientProfile;
import com.alex.exception.SQLRuntimeException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ClientProfileRowMapper implements RowMapper<ClientProfile> {

    @Override
    public ClientProfile mapRow(ResultSet rs, int rowNum) {
        try {
            Timestamp clientModifyDate = rs.getTimestamp("client_modify_date");
            return new ClientProfile(
                    rs.getLong("client_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("city"),
                    rs.getString("street"),
                    rs.getString("house_number"),
                    rs.getString("identification_number"),
                    rs.getTimestamp("client_create_date").toLocalDateTime(),
                    clientModifyDate != null ? clientModifyDate.toLocalDateTime() : null,
                    rs.getLong("user_account_id"),
                    rs.getString("login"),
                    rs.getString("role"),
                    rs.getTimestamp("account_create_date").toLocalDateTime()
            );
        } catch (SQLException e) {
            throw new SQLRuntimeException("Database exception." + e.getMessage());
        }
    }
}
