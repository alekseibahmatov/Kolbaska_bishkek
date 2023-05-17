package ee.maitsetuur.scheduledTasks;

import ee.maitsetuur.model.payment.Status;
import ee.maitsetuur.model.report.Report;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.repository.ReportRepository;
import ee.maitsetuur.repository.RestaurantRepository;
import ee.maitsetuur.repository.TransactionRepository;
import ee.maitsetuur.service.AdminRestaurantService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportCreator {

    private final RestaurantRepository restaurantRepository;

    private final TransactionRepository transactionRepository;

    private final ReportRepository reportRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRestaurantService.class);

    @Scheduled(cron = "0 0 0 ? * 4")
    @Transactional
    public void generateReport() {
        LOGGER.info("Starting to generate report");

        List<Restaurant> restaurants = restaurantRepository.findAll();

        Instant start = LocalDate.now().minusDays(7).atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant();
        Instant end = LocalDate.now().minusDays(1).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        for (Restaurant r : restaurants) {
            List<Transaction> transactions = transactionRepository.findTransactionsByCreatedAtAfterAndCreatedAtBeforeAndRestaurant(start, end, r);

            Double maitsetuurShare = transactions.stream().mapToDouble(t -> t.getValue() * ((double) r.getMaitsetuurShare() /100)).sum();
            Double turnover = transactions.stream().mapToDouble(Transaction::getValue).sum();

            Report newReport = Report.builder()
                    .reportTo(LocalDate.now().minusDays(1))
                    .reportFrom(LocalDate.now().minusDays(7))
                    .maitsetuurShare(maitsetuurShare)
                    .turnover(turnover)
                    .transactionsAmount(transactions.size())
                    .restaurant(r)
                    .status(Status.UNPAID)
                    .build();

            newReport = reportRepository.save(newReport);

            for(Transaction t : transactions) {
                Transaction newTransaction = transactionRepository.findById(t.getId());
                newTransaction.setReport(newReport);
                transactionRepository.save(newTransaction);
            }
        }
    }

}
