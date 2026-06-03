package com.alex.repository;

import com.alex.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Direct coverage for {@link TransactionRepository#sumDepositsForAccountsBetween} — in particular the
 * empty/null guard, which the deposit endpoint never reaches (a depositing client always owns at least
 * the target account, so the id set is never empty).
 */
class TransactionRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ITransactionRepository transactionRepository;

    private final LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    private final LocalDateTime startOfNextDay = startOfDay.plusDays(1);

    @Test
    void sumDeposits_emptyAccountSet_returnsZeroWithoutQuerying() {
        assertThat(transactionRepository.sumDepositsForAccountsBetween(Set.of(), startOfDay, startOfNextDay))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumDeposits_nullAccountSet_returnsZeroWithoutQuerying() {
        assertThat(transactionRepository.sumDepositsForAccountsBetween(null, startOfDay, startOfNextDay))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sumDeposits_withTodaysDeposits_returnsSumForTheGivenAccounts() {
        insertBankAccount(100L, "100000000001", "CHECKING", "EUR", new BigDecimal("0.00"));
        insertDeposit(100L, new BigDecimal("40.00"));
        insertDeposit(100L, new BigDecimal("60.00"));

        assertThat(transactionRepository.sumDepositsForAccountsBetween(Set.of(100L), startOfDay, startOfNextDay))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    private void insertDeposit(Long toAccountId, BigDecimal amount) {
        jdbcTemplate.update(
                "INSERT INTO transaction (transaction_type, currency, amount, bank_account_id_from, bank_account_id_to, description, create_date)"
                        + " VALUES ('DEPOSIT', 'EUR', ?, NULL, ?, 'today', ?)",
                amount, toAccountId, Timestamp.valueOf(LocalDateTime.now()));
    }
}
