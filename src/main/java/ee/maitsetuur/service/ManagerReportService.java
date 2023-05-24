package ee.maitsetuur.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ee.maitsetuur.config.UserConfiguration;
import ee.maitsetuur.dto.TransactionsDTO;
import ee.maitsetuur.exception.ReportNotFoundException;
import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.model.report.Report;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.ReportRepository;
import ee.maitsetuur.response.report.ReportResponse;
import ee.maitsetuur.response.report.ReportTransactionResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ManagerReportService {

    private final UserConfiguration user;

    private final ReportRepository reportRepository;

    private static final int PAGE_CONTAINS_TRANSACTIONS = 23;

    public List<ReportResponse> getReports(LocalDate from, LocalDate to) throws RestaurantNotFoundException {
        User manager = user.getRequestUser();

        Restaurant restaurant = Optional.ofNullable(manager.getManagedRestaurant()).orElseThrow(() -> new RestaurantNotFoundException("This user does not belongs to any restaurant"));

        Instant parsedStartFrom, parsedEndTo;

        parsedStartFrom = Objects.requireNonNullElseGet(from, () -> LocalDate.of(2022, 1, 1)).atStartOfDay(ZoneId.systemDefault()).toInstant();
        parsedEndTo = Objects.requireNonNullElseGet(to, LocalDate::now).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        List<ReportResponse> response = new ArrayList<>();

        List<Report> reports = reportRepository.findByCreatedAtAfterAndCreatedAtBeforeAndRestaurant(parsedStartFrom, parsedEndTo, restaurant);

        for (Report report : reports) {

            ReportResponse reportResponse = ReportResponse.builder()
                    .id(report.getId())
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

            reportResponse.setReportTransactionResponse(transactionResponses);

            response.add(reportResponse);
        }

        return response;
    }

    public byte[] downloadReport(UUID reportId) throws ReportNotFoundException, IllegalAccessException {
        User worker = user.getRequestUser();

        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException("The report was not found"));

        if (worker.getManagedRestaurant() != report.getRestaurant() && !user.getRoleNames(worker).contains("ROLE_ADMIN")) throw new IllegalAccessException("Access denied");

        HashMap<String, Object> data = new HashMap<>();

        data.put("id", "Report #%s".formatted(report.getId().toString().split("-")[0]));
        data.put("transactions_amount", report.getTransactionsAmount());
        data.put("turnover", report.getTurnover());
        data.put("maitsetuur_share", report.getMaitsetuurShare());
        data.put("report_from", report.getReportFrom().toString());
        data.put("report_to", report.getReportTo().toString());
        data.put("status", report.getStatus());

        Set<Transaction> transactions = report.getTransactions();

        ZoneId zoneId = ZoneId.systemDefault();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        double total = 0.0;

        List<List<TransactionsDTO>> pages = new ArrayList<>();

        List<TransactionsDTO> page = new ArrayList<>();

        int onPageTransaction = 0, totalIterates = 0;

        for (Transaction t : transactions) {
            ZonedDateTime zonedDateTime = t.getCreatedAt().atZone(zoneId);
            TransactionsDTO newTransactionDTO = TransactionsDTO.builder()
                    .uuid(t.getId().toString())
                    .activationDate(dateFormatter.format(zonedDateTime))
                    .activationTime(timeFormatter.format(zonedDateTime))
                    .spent("%.2f".formatted(t.getValue()))
                    .waiterFullName(t.getWaiter().getFullName())
                    .build();

            page.add(newTransactionDTO);

            onPageTransaction++;
            totalIterates++;

            total += t.getValue();

            if (onPageTransaction == PAGE_CONTAINS_TRANSACTIONS) {
                pages.add(page);
                page = new ArrayList<>();
                onPageTransaction = 0;
            }

            if (totalIterates == transactions.size()) pages.add(page);
        }

        data.put("transactions", pages);
        data.put("sub_total", "%.2f".formatted(total / 1.2));
        data.put("vat", "%.2f".formatted(total - (total / 1.2)));
        data.put("total", "%.2f".formatted(total));

        OkHttpClient client = new OkHttpClient();

        Gson gson = new Gson();
        String jsonData = gson.toJson(data);

        RequestBody body = RequestBody.create(
                jsonData,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("http://localhost:3030/reports")
                .post(body)
                .build();

        byte[] pdfBytes = null;

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            assert response.body() != null;

            // parse response body as json
            JsonObject json = new Gson().fromJson(response.body().string(), JsonObject.class);

            // get base64 pdf string
            String base64Pdf = json.get("pdf").getAsString();

            // decode base64 string to byte array
            pdfBytes = Base64.getDecoder().decode(base64Pdf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pdfBytes;
    }
}
