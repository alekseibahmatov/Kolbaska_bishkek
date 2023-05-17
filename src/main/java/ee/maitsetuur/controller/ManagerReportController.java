package ee.maitsetuur.controller;

import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.response.report.ReportResponse;
import ee.maitsetuur.service.ManagerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${api.basepath}/accountant")
@RequiredArgsConstructor
public class ManagerReportController {

    private final ManagerReportService service;

    @GetMapping("/report")
    private ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) throws RestaurantNotFoundException {
        return ResponseEntity.ok(service.getReports(from, to));
    }
}
