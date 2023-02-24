package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
