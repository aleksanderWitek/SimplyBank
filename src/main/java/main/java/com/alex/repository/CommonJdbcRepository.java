package main.java.com.alex.repository;

import main.java.com.alex.exception.DataAccessRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CommonJdbcRepository implements ICommonJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(CommonJdbcRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public CommonJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Long getLastInsertedId() {
        String query;
        query = "SELECT last_insert_id()";
        log.trace(query);
        try {
            return jdbcTemplate.queryForObject(query, Long.class);
        } catch (DataAccessException e) {
            throw new DataAccessRuntimeException("Can't access database " + e.getMessage());
        }
    }
}
