package com.alex.repository.mapper;

import com.alex.dto.Employee;
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
class EmployeeRowMapperTest {

    @Mock
    private ResultSet rs;

    private final EmployeeRowMapper mapper = new EmployeeRowMapper();

    @Test
    void mapRow_allColumnsPresent_returnsEmployee() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime modified = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime deleted = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getTimestamp("modify_date")).thenReturn(Timestamp.valueOf(modified));
        when(rs.getTimestamp("delete_date")).thenReturn(Timestamp.valueOf(deleted));
        when(rs.getLong("id")).thenReturn(3L);
        when(rs.getString("first_name")).thenReturn("Bob");
        when(rs.getString("last_name")).thenReturn("Jones");
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        Employee employee = mapper.mapRow(rs, 1);

        assertThat(employee.getId()).isEqualTo(3L);
        assertThat(employee.getFirstName()).isEqualTo("Bob");
        assertThat(employee.getLastName()).isEqualTo("Jones");
        assertThat(employee.getCreateDate()).isEqualTo(created);
        assertThat(employee.getModifyDate()).isEqualTo(modified);
        assertThat(employee.getDeleteDate()).isEqualTo(deleted);
    }

    @Test
    void mapRow_nullTimestamps_mapsNulls() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(rs.getTimestamp("modify_date")).thenReturn(null);
        when(rs.getTimestamp("delete_date")).thenReturn(null);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("first_name")).thenReturn("A");
        when(rs.getString("last_name")).thenReturn("B");
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        Employee employee = mapper.mapRow(rs, 1);

        assertThat(employee.getModifyDate()).isNull();
        assertThat(employee.getDeleteDate()).isNull();
    }

    @Test
    void mapRow_sqlException_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getTimestamp("modify_date")).thenThrow(new SQLException("broken"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception.")
                .hasMessageContaining("broken");
    }
}
