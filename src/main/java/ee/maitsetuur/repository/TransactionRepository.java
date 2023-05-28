package ee.maitsetuur.repository;

import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Transaction findById(UUID id);
    List<Transaction> findTransactionsByCreatedAtAfterAndCreatedAtBefore(Instant startFrom, Instant endTo);

    List<Transaction> findTransactionsByCreatedAtAfterAndCreatedAtBeforeAndRestaurant(Instant startFrom, Instant endTo, Restaurant restaurant);

}
