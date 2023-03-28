package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.repository.TransactionRepository;
import ee.kolbaska.kolbaska.response.AdminTransactionReportResponse;
import ee.kolbaska.kolbaska.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTransactionService {

    private final TransactionRepository transactionRepository;

    public AdminTransactionReportResponse getTransactions(String startFrom, String endTo) {
        Date parsedStartFrom, parsedEndTo;

        if (startFrom == null || endTo == null) {
            parsedEndTo = new Date();

            Calendar c = Calendar.getInstance();
            c.setTime(parsedEndTo);
            c.add(Calendar.YEAR, -1);

            parsedStartFrom = c.getTime();
        } else {
            parsedEndTo = Date.from(LocalDateTime.parse(String.format("%sT23:59:59", endTo)).atZone(ZoneId.systemDefault()).toInstant());
            parsedStartFrom = Date.from(LocalDate.parse(startFrom).atStartOfDay().toInstant(ZoneOffset.UTC));
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
                    .id(transaction.getId())
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
                .startFrom(parsedStartFrom)
                .endTo(parsedEndTo)
                .build();
    }
}
