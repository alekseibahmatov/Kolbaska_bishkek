package ee.maitsetuur.controller;

import ee.maitsetuur.exception.ReportNotFoundException;
import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.response.report.ReportResponse;
import ee.maitsetuur.response.report.ReportTransactionResponse;
import ee.maitsetuur.service.ManagerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.basepath}/accountant")
@RequiredArgsConstructor
public class ManagerReportController {

    private final ManagerReportService service;

    @GetMapping("/report")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) throws RestaurantNotFoundException {
        return ResponseEntity.ok(service.getReports(from, to));
    }

    @GetMapping("/report/{reportId}/transactions")
    public ResponseEntity<List<ReportTransactionResponse>> getTransactions(
            @RequestParam UUID reportId
    ) throws ReportNotFoundException, IllegalAccessException {
        return ResponseEntity.ok(service.getTransactions(reportId));
    }

    @GetMapping("/download/report/{reportId}")
    public ResponseEntity<Resource> downloadReport(@PathVariable UUID reportId) throws ReportNotFoundException, IllegalAccessException {

        byte[] bytes = service.downloadReport(reportId);

        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=report-%s.pdf".formatted(reportId))
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(bytes.length)
                .body(resource);
    }
}
