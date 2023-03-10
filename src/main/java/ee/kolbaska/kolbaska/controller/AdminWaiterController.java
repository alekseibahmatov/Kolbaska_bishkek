package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.request.AdminCustomerUpdateRequest;
import ee.kolbaska.kolbaska.response.CustomerInformationResponse;
import ee.kolbaska.kolbaska.response.CustomerUpdateResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.AdminWaiterService;
import jakarta.validation.constraints.NotNull;
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
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getWaiter(id));
    }

    @PutMapping("/waiter")
    public ResponseEntity<CustomerUpdateResponse> updateWaiter(
            @NotNull @RequestBody AdminCustomerUpdateRequest request
    ) throws RoleNotFoundException {
        return ResponseEntity.ok(service.updateWaiter(request));
    }
}
