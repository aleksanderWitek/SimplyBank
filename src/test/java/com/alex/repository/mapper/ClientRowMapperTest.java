package com.alex.repository.mapper;

import com.alex.dto.Client;
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
class ClientRowMapperTest {

    @Mock
    private ResultSet rs;

    private final ClientRowMapper mapper = new ClientRowMapper();

    @Test
    void mapRow_allColumnsPresent_returnsClient() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime modified = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime deleted = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getTimestamp("modify_date")).thenReturn(Timestamp.valueOf(modified));
        when(rs.getTimestamp("delete_date")).thenReturn(Timestamp.valueOf(deleted));
        when(rs.getLong("id")).thenReturn(5L);
        when(rs.getString("first_name")).thenReturn("Alice");
        when(rs.getString("last_name")).thenReturn("Smith");
        when(rs.getString("city")).thenReturn("Warsaw");
        when(rs.getString("street")).thenReturn("Main");
        when(rs.getString("house_number")).thenReturn("10");
        when(rs.getString("identification_number")).thenReturn("ID123");
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        Client client = mapper.mapRow(rs, 1);

        assertThat(client.getId()).isEqualTo(5L);
        assertThat(client.getFirstName()).isEqualTo("Alice");
        assertThat(client.getLastName()).isEqualTo("Smith");
        assertThat(client.getCity()).isEqualTo("Warsaw");
        assertThat(client.getStreet()).isEqualTo("Main");
        assertThat(client.getHouseNumber()).isEqualTo("10");
        assertThat(client.getIdentificationNumber()).isEqualTo("ID123");
        assertThat(client.getCreateDate()).isEqualTo(created);
        assertThat(client.getModifyDate()).isEqualTo(modified);
        assertThat(client.getDeleteDate()).isEqualTo(deleted);
    }

    @Test
    void mapRow_nullTimestamps_mapsNulls() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(rs.getTimestamp("modify_date")).thenReturn(null);
        when(rs.getTimestamp("delete_date")).thenReturn(null);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("first_name")).thenReturn("A");
        when(rs.getString("last_name")).thenReturn("B");
        when(rs.getString("city")).thenReturn("C");
        when(rs.getString("street")).thenReturn("S");
        when(rs.getString("house_number")).thenReturn("1");
        when(rs.getString("identification_number")).thenReturn("ID");
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        Client client = mapper.mapRow(rs, 1);

        assertThat(client.getModifyDate()).isNull();
        assertThat(client.getDeleteDate()).isNull();
    }

    @Test
    void mapRow_sqlException_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getTimestamp("modify_date")).thenThrow(new SQLException("db down"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception.")
                .hasMessageContaining("db down");
    }
}
