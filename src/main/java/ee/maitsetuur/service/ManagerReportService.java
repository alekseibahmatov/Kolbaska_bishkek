package ee.maitsetuur.service;

import ee.maitsetuur.config.UserConfiguration;
import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.model.report.Report;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.ReportRepository;
import ee.maitsetuur.response.report.ReportResponse;
import ee.maitsetuur.response.report.ReportTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ManagerReportService {

    private final UserConfiguration user;

    private final ReportRepository reportRepository;

    public List<ReportResponse> getReports(LocalDate from, LocalDate to) throws RestaurantNotFoundException {
        User manager = user.getRequestUser();

        Restaurant restaurant = Optional.ofNullable(manager.getManagedRestaurant()).orElseThrow(() -> new RestaurantNotFoundException("This user does not belongs to any restaurant"));

        Instant parsedStartFrom, parsedEndTo;

        if (from == null || to == null) {
            parsedEndTo = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            parsedStartFrom = LocalDate.of(2022, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            parsedEndTo = from.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            parsedStartFrom = to.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        List<ReportResponse> response = new ArrayList<>();

        List<Report> reports = reportRepository.findByCreatedAtAfterAndCreatedAtBeforeAndRestaurant(parsedStartFrom, parsedEndTo, restaurant);

        for (Report report : reports) {

            ReportResponse reportResponse = ReportResponse.builder()
                    .reportFrom(report.getReportFrom())
                    .reportTo(report.getReportTo())
                    .turnover(report.getTurnover())
                    .maitsetuurShare(report.getMaitsetuurShare())
                    .status(report.getStatus())
                    .transactionsAmount(report.getTransactionsAmount())
                    .build();

            Set<Transaction> transactions = report.getTransactions();

            Set<ReportTransactionResponse> transactionResponses = new HashSet<>();

            for (Transaction t : transactions) {
                ReportTransactionResponse transactionResponse = ReportTransactionResponse.builder()
                        .activationDate(LocalDate.ofInstant(t.getCreatedAt(), ZoneId.systemDefault()))
                        .activationTime(LocalTime.ofInstant(t.getCreatedAt(), ZoneId.systemDefault()))
                        .amountSpent(t.getValue())
                        .uuid(t.getId().toString())
                        .waiterFullName(t.getWaiter().getFullName())
                        .build();

                transactionResponses.add(transactionResponse);
            }

            reportResponse.setReportTransactionRespons(transactionResponses);

            response.add(reportResponse);
        }

        return response;
    }
}
