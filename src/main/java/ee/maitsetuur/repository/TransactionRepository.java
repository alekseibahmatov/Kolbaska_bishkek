package ee.maitsetuur.repository;

import ee.maitsetuur.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findTransactionsByCreatedAtAfterAndCreatedAtBefore(Instant startFrom, Instant endTo);
}
