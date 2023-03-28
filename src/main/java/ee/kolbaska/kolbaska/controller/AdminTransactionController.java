package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.response.AdminTransactionReportResponse;
import ee.kolbaska.kolbaska.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final AdminTransactionService service;

    @GetMapping("/transaction")
    public ResponseEntity<AdminTransactionReportResponse> getTransactions(
            @RequestParam(required = false) String startFrom,
            @RequestParam(required = false) String endTo
    ) {
        return ResponseEntity.ok(service.getTransactions(startFrom, endTo));
    }
}
