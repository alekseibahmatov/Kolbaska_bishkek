package ee.maitsetuur.controller;

import ee.maitsetuur.response.AdminTransactionReportResponse;
import ee.maitsetuur.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final AdminTransactionService service;

    @GetMapping("/transaction")
    public ResponseEntity<AdminTransactionReportResponse> getTransactions(
            @RequestParam(required = false) LocalDate startFrom,
            @RequestParam(required = false) LocalDate endTo
    ) {
        return ResponseEntity.ok(service.getTransactions(startFrom, endTo));
    }
}
