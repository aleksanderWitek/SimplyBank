package com.alex.repository.mapper;

import com.alex.dto.EmployeeProfile;
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
class EmployeeProfileRowMapperTest {

    @Mock
    private ResultSet rs;

    private final EmployeeProfileRowMapper mapper = new EmployeeProfileRowMapper();

    @Test
    void mapRow_allColumnsPresent_returnsEmployeeProfile() throws SQLException {
        LocalDateTime empCreate = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime empModify = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime accountCreate = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getTimestamp("employee_modify_date")).thenReturn(Timestamp.valueOf(empModify));
        when(rs.getLong("employee_id")).thenReturn(1L);
        when(rs.getString("first_name")).thenReturn("Bob");
        when(rs.getString("last_name")).thenReturn("Jones");
        when(rs.getTimestamp("employee_create_date")).thenReturn(Timestamp.valueOf(empCreate));
        when(rs.getLong("user_account_id")).thenReturn(5L);
        when(rs.getString("login")).thenReturn("bob.jones");
        when(rs.getString("role")).thenReturn("EMPLOYEE");
        when(rs.getTimestamp("account_create_date")).thenReturn(Timestamp.valueOf(accountCreate));

        EmployeeProfile profile = mapper.mapRow(rs, 1);

        assertThat(profile.getEmployeeId()).isEqualTo(1L);
        assertThat(profile.getFirstName()).isEqualTo("Bob");
        assertThat(profile.getLastName()).isEqualTo("Jones");
        assertThat(profile.getEmployeeCreateDate()).isEqualTo(empCreate);
        assertThat(profile.getEmployeeModifyDate()).isEqualTo(empModify);
        assertThat(profile.getUserAccountId()).isEqualTo(5L);
        assertThat(profile.getLogin()).isEqualTo("bob.jones");
        assertThat(profile.getRole()).isEqualTo("EMPLOYEE");
        assertThat(profile.getAccountCreateDate()).isEqualTo(accountCreate);
    }

    @Test
    void mapRow_nullModifyDate_mapsNull() throws SQLException {
        LocalDateTime empCreate = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime accountCreate = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getTimestamp("employee_modify_date")).thenReturn(null);
        when(rs.getLong("employee_id")).thenReturn(1L);
        when(rs.getString("first_name")).thenReturn("A");
        when(rs.getString("last_name")).thenReturn("B");
        when(rs.getTimestamp("employee_create_date")).thenReturn(Timestamp.valueOf(empCreate));
        when(rs.getLong("user_account_id")).thenReturn(5L);
        when(rs.getString("login")).thenReturn("a");
        when(rs.getString("role")).thenReturn("EMPLOYEE");
        when(rs.getTimestamp("account_create_date")).thenReturn(Timestamp.valueOf(accountCreate));

        EmployeeProfile profile = mapper.mapRow(rs, 1);

        assertThat(profile.getEmployeeModifyDate()).isNull();
    }

    @Test
    void mapRow_sqlException_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getTimestamp("employee_modify_date")).thenThrow(new SQLException("boom"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception.")
                .hasMessageContaining("boom");
    }
}
