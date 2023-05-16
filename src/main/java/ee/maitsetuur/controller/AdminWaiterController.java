package ee.maitsetuur.controller;

import ee.maitsetuur.request.AdminCustomerUpdateRequest;
import ee.maitsetuur.response.CustomerInformationResponse;
import ee.maitsetuur.response.CustomerUpdateResponse;
import ee.maitsetuur.response.WaiterResponse;
import ee.maitsetuur.service.AdminWaiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.util.List;

@RestController
@RequestMapping("${api.basepath}/admin/restaurant")
@RequiredArgsConstructor
public class AdminWaiterController {

    private final AdminWaiterService service;

    @GetMapping("/waiter")
    public ResponseEntity<List<WaiterResponse>> getWaiters() {
        return ResponseEntity.ok(service.getWaiters());
    }

    @GetMapping("/waiter/{id}")
    public ResponseEntity<CustomerInformationResponse> getWaiter(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getWaiter(id));
    }

    @PutMapping("/waiter")
    public ResponseEntity<CustomerUpdateResponse> updateWaiter(
            @Valid @RequestBody AdminCustomerUpdateRequest request
    ) throws RoleNotFoundException {
        return ResponseEntity.ok(service.updateWaiter(request));
    }
}
