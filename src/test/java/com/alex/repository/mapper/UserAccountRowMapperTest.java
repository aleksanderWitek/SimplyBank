package com.alex.repository.mapper;

import com.alex.UserAccountRole;
import com.alex.dto.UserAccount;
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
class UserAccountRowMapperTest {

    @Mock
    private ResultSet rs;

    private final UserAccountRowMapper mapper = new UserAccountRowMapper();

    @Test
    void mapRow_allColumnsPresent_returnsUserAccount() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime modified = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime deleted = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getString("role")).thenReturn("client");
        when(rs.getTimestamp("modify_date")).thenReturn(Timestamp.valueOf(modified));
        when(rs.getTimestamp("delete_date")).thenReturn(Timestamp.valueOf(deleted));
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("login")).thenReturn("alice.smith");
        when(rs.getString("password")).thenReturn("encoded");
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        UserAccount account = mapper.mapRow(rs, 1);

        assertThat(account.getId()).isEqualTo(1L);
        assertThat(account.getLogin()).isEqualTo("alice.smith");
        assertThat(account.getPassword()).isEqualTo("encoded");
        assertThat(account.getRole()).isEqualTo(UserAccountRole.CLIENT);
        assertThat(account.getCreateDate()).isEqualTo(created);
        assertThat(account.getModifyDate()).isEqualTo(modified);
        assertThat(account.getDeleteDate()).isEqualTo(deleted);
    }

    @Test
    void mapRow_nullTimestamps_mapsNulls() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(rs.getString("role")).thenReturn("ADMIN");
        when(rs.getTimestamp("modify_date")).thenReturn(null);
        when(rs.getTimestamp("delete_date")).thenReturn(null);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("login")).thenReturn("admin");
        when(rs.getString("password")).thenReturn("pwd");
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        UserAccount account = mapper.mapRow(rs, 1);

        assertThat(account.getModifyDate()).isNull();
        assertThat(account.getDeleteDate()).isNull();
        assertThat(account.getRole()).isEqualTo(UserAccountRole.ADMIN);
    }

    @Test
    void mapRow_sqlException_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getString("role")).thenThrow(new SQLException("oops"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception.")
                .hasMessageContaining("oops");
    }
}
