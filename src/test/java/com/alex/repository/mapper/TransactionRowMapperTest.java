package com.alex.repository.mapper;

import com.alex.BankAccountType;
import com.alex.Currency;
import com.alex.TransactionType;
import com.alex.dto.Transaction;
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
class TransactionRowMapperTest {

    @Mock
    private ResultSet rs;

    private final TransactionRowMapper mapper = new TransactionRowMapper();

    @Test
    void mapRow_transferWithBothAccounts_returnsPopulatedTransaction() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime bafCreate = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime batCreate = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getString("transaction_type")).thenReturn("TRANSFER");
        when(rs.getString("currency")).thenReturn("EUR");
        when(rs.getLong("baf_id")).thenReturn(1L);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getString("baf_account_type")).thenReturn("CHECKING");
        when(rs.getString("baf_currency")).thenReturn("EUR");
        when(rs.getTimestamp("baf_create_date")).thenReturn(Timestamp.valueOf(bafCreate));
        when(rs.getTimestamp("baf_modify_date")).thenReturn(null);
        when(rs.getTimestamp("baf_delete_date")).thenReturn(null);
        when(rs.getString("baf_number")).thenReturn("111111111111");
        when(rs.getBigDecimal("baf_balance")).thenReturn(new BigDecimal("500.00"));
        when(rs.getLong("bat_id")).thenReturn(2L);
        when(rs.getString("bat_account_type")).thenReturn("SAVING");
        when(rs.getString("bat_currency")).thenReturn("EUR");
        when(rs.getTimestamp("bat_create_date")).thenReturn(Timestamp.valueOf(batCreate));
        when(rs.getTimestamp("bat_modify_date")).thenReturn(null);
        when(rs.getTimestamp("bat_delete_date")).thenReturn(null);
        when(rs.getString("bat_number")).thenReturn("222222222222");
        when(rs.getBigDecimal("bat_balance")).thenReturn(new BigDecimal("50.00"));
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));
        when(rs.getLong("id")).thenReturn(99L);
        when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("25.00"));
        when(rs.getString("description")).thenReturn("Rent");

        Transaction tx = mapper.mapRow(rs, 1);

        assertThat(tx.getId()).isEqualTo(99L);
        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(tx.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(tx.getAmount()).isEqualByComparingTo("25.00");
        assertThat(tx.getDescription()).isEqualTo("Rent");
        assertThat(tx.getCreateDate()).isEqualTo(created);
        assertThat(tx.getBankAccountFrom()).isNotNull();
        assertThat(tx.getBankAccountFrom().getId()).isEqualTo(1L);
        assertThat(tx.getBankAccountFrom().getAccountType()).isEqualTo(BankAccountType.CHECKING);
        assertThat(tx.getBankAccountTo()).isNotNull();
        assertThat(tx.getBankAccountTo().getId()).isEqualTo(2L);
        assertThat(tx.getBankAccountTo().getAccountType()).isEqualTo(BankAccountType.SAVING);
    }

    @Test
    void mapRow_depositWithNullFromAccount_returnsNullFromAccount() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime batCreate = LocalDateTime.of(2026, 1, 3, 10, 0);

        when(rs.getString("transaction_type")).thenReturn("DEPOSIT");
        when(rs.getString("currency")).thenReturn("PLN");
        when(rs.getLong("baf_id")).thenReturn(0L);
        when(rs.getLong("bat_id")).thenReturn(2L);
        when(rs.getString("bat_account_type")).thenReturn("CHECKING");
        when(rs.getString("bat_currency")).thenReturn("PLN");
        when(rs.getTimestamp("bat_create_date")).thenReturn(Timestamp.valueOf(batCreate));
        when(rs.getTimestamp("bat_modify_date")).thenReturn(null);
        when(rs.getTimestamp("bat_delete_date")).thenReturn(null);
        when(rs.getString("bat_number")).thenReturn("222222222222");
        when(rs.getBigDecimal("bat_balance")).thenReturn(new BigDecimal("50.00"));
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));
        when(rs.getLong("id")).thenReturn(11L);
        when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("10.00"));
        when(rs.getString("description")).thenReturn(null);

        Transaction tx = mapper.mapRow(rs, 1);

        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.getBankAccountFrom()).isNull();
        assertThat(tx.getBankAccountTo()).isNotNull();
        assertThat(tx.getBankAccountTo().getId()).isEqualTo(2L);
    }

    @Test
    void mapRow_withdrawalWithNullToAccount_returnsNullToAccount() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime bafCreate = LocalDateTime.of(2026, 1, 2, 10, 0);

        when(rs.getString("transaction_type")).thenReturn("WITHDRAWAL");
        when(rs.getString("currency")).thenReturn("USD");
        when(rs.getLong("baf_id")).thenReturn(1L);
        when(rs.wasNull()).thenReturn(false).thenReturn(true);
        when(rs.getString("baf_account_type")).thenReturn("CHECKING");
        when(rs.getString("baf_currency")).thenReturn("USD");
        when(rs.getTimestamp("baf_create_date")).thenReturn(Timestamp.valueOf(bafCreate));
        when(rs.getTimestamp("baf_modify_date")).thenReturn(null);
        when(rs.getTimestamp("baf_delete_date")).thenReturn(null);
        when(rs.getString("baf_number")).thenReturn("111111111111");
        when(rs.getBigDecimal("baf_balance")).thenReturn(new BigDecimal("500.00"));
        when(rs.getLong("bat_id")).thenReturn(0L);
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));
        when(rs.getLong("id")).thenReturn(22L);
        when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("5.00"));
        when(rs.getString("description")).thenReturn("ATM");

        Transaction tx = mapper.mapRow(rs, 1);

        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(tx.getBankAccountFrom()).isNotNull();
        assertThat(tx.getBankAccountFrom().getId()).isEqualTo(1L);
        assertThat(tx.getBankAccountTo()).isNull();
    }

    @Test
    void mapRow_accountTimestampsPresent_mapsLocalDateTime() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime bafCreate = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime bafModify = LocalDateTime.of(2026, 1, 3, 10, 0);
        LocalDateTime bafDelete = LocalDateTime.of(2026, 1, 4, 10, 0);
        LocalDateTime batCreate = LocalDateTime.of(2026, 1, 5, 10, 0);
        LocalDateTime batModify = LocalDateTime.of(2026, 1, 6, 10, 0);
        LocalDateTime batDelete = LocalDateTime.of(2026, 1, 7, 10, 0);

        when(rs.getString("transaction_type")).thenReturn("TRANSFER");
        when(rs.getString("currency")).thenReturn("EUR");
        when(rs.getLong("baf_id")).thenReturn(1L);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getString("baf_account_type")).thenReturn("CHECKING");
        when(rs.getString("baf_currency")).thenReturn("EUR");
        when(rs.getTimestamp("baf_create_date")).thenReturn(Timestamp.valueOf(bafCreate));
        when(rs.getTimestamp("baf_modify_date")).thenReturn(Timestamp.valueOf(bafModify));
        when(rs.getTimestamp("baf_delete_date")).thenReturn(Timestamp.valueOf(bafDelete));
        when(rs.getString("baf_number")).thenReturn("111");
        when(rs.getBigDecimal("baf_balance")).thenReturn(new BigDecimal("10.00"));
        when(rs.getLong("bat_id")).thenReturn(2L);
        when(rs.getString("bat_account_type")).thenReturn("CHECKING");
        when(rs.getString("bat_currency")).thenReturn("EUR");
        when(rs.getTimestamp("bat_create_date")).thenReturn(Timestamp.valueOf(batCreate));
        when(rs.getTimestamp("bat_modify_date")).thenReturn(Timestamp.valueOf(batModify));
        when(rs.getTimestamp("bat_delete_date")).thenReturn(Timestamp.valueOf(batDelete));
        when(rs.getString("bat_number")).thenReturn("222");
        when(rs.getBigDecimal("bat_balance")).thenReturn(new BigDecimal("5.00"));
        when(rs.getTimestamp("create_date")).thenReturn(Timestamp.valueOf(created));
        when(rs.getLong("id")).thenReturn(99L);
        when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("1.00"));
        when(rs.getString("description")).thenReturn("x");

        Transaction tx = mapper.mapRow(rs, 1);

        assertThat(tx.getBankAccountFrom().getModifyDate()).isEqualTo(bafModify);
        assertThat(tx.getBankAccountFrom().getDeleteDate()).isEqualTo(bafDelete);
        assertThat(tx.getBankAccountTo().getModifyDate()).isEqualTo(batModify);
        assertThat(tx.getBankAccountTo().getDeleteDate()).isEqualTo(batDelete);
    }

    @Test
    void mapRow_sqlExceptionOnOuter_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getString("transaction_type")).thenThrow(new SQLException("outer"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception:")
                .hasMessageContaining("outer");
    }

    @Test
    void mapRow_sqlExceptionOnFromAccount_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getString("transaction_type")).thenReturn("TRANSFER");
        when(rs.getString("currency")).thenReturn("EUR");
        when(rs.getLong("baf_id")).thenThrow(new SQLException("inner-from"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception:")
                .hasMessageContaining("inner-from");
    }

    @Test
    void mapRow_sqlExceptionOnToAccount_wrapsInSQLRuntimeException() throws SQLException {
        when(rs.getString("transaction_type")).thenReturn("TRANSFER");
        when(rs.getString("currency")).thenReturn("EUR");
        when(rs.getLong("baf_id")).thenReturn(0L);
        when(rs.getLong("bat_id")).thenThrow(new SQLException("inner-to"));

        assertThatThrownBy(() -> mapper.mapRow(rs, 1))
                .isInstanceOf(SQLRuntimeException.class)
                .hasMessageContaining("Database exception:")
                .hasMessageContaining("inner-to");
    }
}
