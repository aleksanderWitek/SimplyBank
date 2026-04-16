package com.alex.repository.mapper;

import com.alex.dto.ClientProfile;
import com.alex.exception.SQLRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientProfileRowMapperTest {

    @Mock
    private ResultSet rs;

    private final ClientProfileRowMapper mapper = new ClientProfileRowMapper();

    @Test
    void mapRow_allColumnsPresent_returnsClientProfile() throws SQLException {
        LocalDateTime clientCreate = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime clientModify = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime accountCreate = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getTimestamp("client_modify_date")).thenReturn(Timestamp.valueOf(clientModify));
        when(rs.getLong("client_id")).thenReturn(1L);
        when(rs.getString("first_name")).thenReturn("Alice");
        when(rs.getString("last_name")).thenReturn("Smith");
        when(rs.getString("city")).thenReturn("Warsaw");
        when(rs.getString("street")).thenReturn("Main");
        when(rs.getString("house_number")).thenReturn("10");
        when(rs.getString("identification_number")).thenReturn("ID123");
        when(rs.getTimestamp("client_create_date")).thenReturn(Timestamp.valueOf(clientCreate));
        when(rs.getLong("user_account_id")).thenReturn(2L);
        when(rs.getString("login")).thenReturn("alice");
        when(rs.getString("role")).thenReturn("CLIENT");
        when(rs.getTimestamp("account_create_date")).thenReturn(Timestamp.valueOf(accountCreate));

        ClientProfile profile = mapper.mapRow(rs, 1);

        assertThat(profile.getClientId()).isEqualTo(1L);
        assertThat(profile.getFirstName()).isEqualTo("Alice");
        assertThat(profile.getLastName()).isEqualTo("Smith");
        assertThat(profile.getCity()).isEqualTo("Warsaw");
        assertThat(profile.getStreet()).isEqualTo("Main");
        assertThat(profile.getHouseNumber()).isEqualTo("10");
        assertThat(profile.getIdentificationNumber()).isEqualTo("ID123");
        assertThat(profile.getClientCreateDate()).isEqualTo(clientCreate);
        assertThat(profile.getClientModifyDate()).isEqualTo(clientModify);
        assertThat(profile.getUserAccountId()).isEqualTo(2L);
        assertThat(profile.getLogin()).isEqualTo("alice");
        assertThat(profile.getRole()).isEqualTo("CLIENT");
        assertThat(profile.getAccountCreateDate()).isEqualTo(accountCreate);
    }

    @Test
    void mapRow_nullModifyDate_mapsNull() throws SQLException {
        LocalDateTime clientCreate = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime accountCreate = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getTimestamp("client_modify_date")).thenReturn(null);
        when(rs.getLong("client_id")).thenReturn(1L);
        when(rs.getString("first_name")).thenReturn("A");
        when(rs.getString("last_name")).thenReturn("B");
        when(rs.getString("city")).thenReturn("C");
        when(rs.getString("street")).thenReturn("S");
        when(rs.getString("house_number")).thenReturn("1");
        when(rs.getString("identification_number")).thenReturn("ID");
        when(rs.getTimestamp("client_create_date")).thenReturn(Timestamp.valueOf(clientCreate));
        when(rs.getLong("user_account_id")).thenReturn(2L);
        when(rs.getString("login")).thenReturn("a");
        when(rs.getString("role")).thenReturn("CLIENT");
        when(rs.getTimestamp("account_create_date")).thenReturn(Timestamp.valueOf(accountCreate));

        ClientProfile profile = mapper.mapRow(rs, 1);

        assertThat(profile.getClientModifyDate()).isNull();
    }

    @Test
    void mapRow_sqlException_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getTimestamp("client_modify_date")).thenThrow(new SQLException("broken"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception.")
                .hasMessageContaining("broken");
    }
}
