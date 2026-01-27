package main.java.com.alex.service;

import main.java.com.alex.dto.Transaction;
import main.java.com.alex.repository.ITransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService implements ITransactionService{

    private final ITransactionRepository transactionRepository;

    public TransactionService(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    @Override
    public Transaction save(Transaction transaction) {
        Long id = transactionRepository.save(transaction);
        return new Transaction(id, transaction.getTransactionType(), transaction.getCurrency(), transaction.getAmount(),
                transaction.getBankAccountFrom(), transaction.getBankAccountTo(), transaction.getDescription(),
                transaction.getCreateDate());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }
}
