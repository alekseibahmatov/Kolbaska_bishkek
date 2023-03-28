package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findTransactionsByCreatedAtAfterAndCreatedAtBefore(Date startFrom, Date endTo);
}
