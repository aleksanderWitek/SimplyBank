package main.java.com.alex.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import main.java.com.alex.dto.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountRepository implements IAccountRepository {
    
    private final EntityManager entityManager;

    public AccountRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Account save(Account account) {
        if(account.getId() == null) {
            entityManager.persist(account);
            entityManager.flush();
            return account;
        } else {
            return entityManager.merge(account);
        }
    }

    @Override
    public Optional<Account> findById(Long id) {
        Account account = entityManager.find(Account.class, id);
        return Optional.ofNullable(account);
    }

    @Override
    public List<Account> findAll() {
        TypedQuery<Account> query = entityManager.createQuery(
                "SELECT account FROM Account account", Account.class);
        return query.getResultList();
    }

    @Override
    public void removeById(Account account) {
        entityManager.remove(account);
    }
}
