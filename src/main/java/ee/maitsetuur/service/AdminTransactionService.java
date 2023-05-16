package ee.maitsetuur.service;

import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.repository.TransactionRepository;
import ee.maitsetuur.response.AdminTransactionReportResponse;
import ee.maitsetuur.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTransactionService {

    private final TransactionRepository transactionRepository;

    public AdminTransactionReportResponse getTransactions(LocalDate startFrom, LocalDate endTo) {
        Instant parsedStartFrom, parsedEndTo;

        if (startFrom == null || endTo == null) {
            parsedEndTo = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            parsedStartFrom = LocalDate.now().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            parsedEndTo = endTo.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            parsedStartFrom = startFrom.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        List<Transaction> transactions = transactionRepository.findTransactionsByCreatedAtAfterAndCreatedAtBefore(parsedStartFrom, parsedEndTo);

        List<TransactionResponse> transactionResponses = new ArrayList<>();

        double overallProfitBrutto = 0.0;
        double overallProfitNetto = 0.0;
        double overallTurnover = 0.0;

        for (Transaction transaction : transactions) {

            double ourProfitBrutto = transaction.getValue() * (transaction.getRestaurant().getMaitsetuurShare() / 100.0);

            overallProfitBrutto += ourProfitBrutto;
            overallProfitNetto += ourProfitBrutto / 1.2;
            overallTurnover += transaction.getValue();

            TransactionResponse transactionResponse = TransactionResponse.builder()
                    .id(transaction.getId().toString())
                    .waiterEmail(transaction.getWaiter().getEmail())
                    .waiterId(transaction.getWaiter().getId())
                    .restaurantName(transaction.getRestaurant().getName())
                    .restaurantCode(transaction.getRestaurant().getRestaurantCode())
                    .value(String.format("%.2f", transaction.getValue()))
                    .profit(String.format("%.2f", ourProfitBrutto))
                    .createdAt(transaction.getCreatedAt())
                    .build();

            transactionResponses.add(transactionResponse);
        }

        return AdminTransactionReportResponse.builder()
                .transactionResponses(transactionResponses)
                .overallTurnoverBrutto(String.format("%.2f", overallTurnover))
                .overallIncomeBrutto(String.format("%.2f",overallProfitBrutto))
                .overallIncomeNetto(String.format("%.2f", overallProfitNetto))
                .startFrom(LocalDate.ofInstant(parsedStartFrom, ZoneId.systemDefault()))
                .endTo(LocalDate.ofInstant(parsedEndTo, ZoneId.systemDefault()))
                .build();
    }
}
