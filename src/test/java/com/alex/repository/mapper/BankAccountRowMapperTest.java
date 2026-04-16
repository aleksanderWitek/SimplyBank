package com.alex.repository.mapper;

import com.alex.BankAccountType;
import com.alex.Currency;
import com.alex.dto.BankAccount;
import com.alex.exception.SQLRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountRowMapperTest {

    @Mock
    private ResultSet rs;

    private final BankAccountRowMapper mapper = new BankAccountRowMapper();

    @Test
    void mapRow_allColumnsPresent_returnsBankAccount() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime modified = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime deleted = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getString("account_type")).thenReturn("checking");
        when(rs.getString("currency")).thenReturn("eur");
        when(rs.getTimestamp("modify_date")).thenReturn(Timestamp.valueOf(modified));
        when(rs.getTimestamp("delete_date")).thenReturn(Timestamp.valueOf(deleted));
        when(rs.getLong("id")).thenReturn(7L);
        when(rs.getString("number")).thenReturn("123456789012");
        when(rs.getBigDecimal("balance")).thenReturn(new BigDecimal("100.00"));
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        BankAccount account = mapper.mapRow(rs, 1);

        assertThat(account.getId()).isEqualTo(7L);
        assertThat(account.getNumber()).isEqualTo("123456789012");
        assertThat(account.getAccountType()).isEqualTo(BankAccountType.CHECKING);
        assertThat(account.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        assertThat(account.getCreateDate()).isEqualTo(created);
        assertThat(account.getModifyDate()).isEqualTo(modified);
        assertThat(account.getDeleteDate()).isEqualTo(deleted);
    }

    @Test
    void mapRow_nullTimestamps_mapsNulls() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(rs.getString("account_type")).thenReturn("SAVING");
        when(rs.getString("currency")).thenReturn("PLN");
        when(rs.getTimestamp("modify_date")).thenReturn(null);
        when(rs.getTimestamp("delete_date")).thenReturn(null);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("number")).thenReturn("999999999999");
        when(rs.getBigDecimal("balance")).thenReturn(new BigDecimal("0.00"));
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));

        BankAccount account = mapper.mapRow(rs, 1);

        assertThat(account.getModifyDate()).isNull();
        assertThat(account.getDeleteDate()).isNull();
        assertThat(account.getAccountType()).isEqualTo(BankAccountType.SAVING);
        assertThat(account.getCurrency()).isEqualTo(Currency.PLN);
    }

    @Test
    void mapRow_sqlException_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getString("account_type")).thenThrow(new SQLException("boom"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception.")
                .hasMessageContaining("boom");
    }
}
