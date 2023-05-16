package ee.maitsetuur.controller;

import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.request.ManagerCustomerUpdateRequest;
import ee.maitsetuur.request.WaiterRequest;
import ee.maitsetuur.response.CustomerInformationResponse;
import ee.maitsetuur.response.CustomerUpdateResponse;
import ee.maitsetuur.response.WaiterDeletedResponse;
import ee.maitsetuur.response.WaiterResponse;
import ee.maitsetuur.service.ManagerRestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.basepath}/manager")
@RequiredArgsConstructor
public class ManagerRestaurantController {

    private final ManagerRestaurantService service;

    @PostMapping("/restaurant/waiter")
    public ResponseEntity<WaiterResponse> createWaiter(
            @Valid @RequestBody WaiterRequest request
            ) throws Exception {
        return ResponseEntity.ok(service.createWaiter(request));
    }

    @GetMapping("/restaurant/waiter")
    public ResponseEntity<List<WaiterResponse>> getWaiters() throws RestaurantNotFoundException {
        return ResponseEntity.ok(service.getWaiters());
    }

    @GetMapping("/restaurant/waiter/{id}")
    public ResponseEntity<CustomerInformationResponse> getWaiter(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getWaiter(id));
    }

    @PutMapping("/restaurant/waiter")
    public ResponseEntity<CustomerUpdateResponse> updateWaiter(
            @Valid @RequestBody ManagerCustomerUpdateRequest request
            ) throws UsernameNotFoundException {
        return ResponseEntity.ok(service.updateWaiter(request));
    }

    @DeleteMapping("/restaurant/waiter/{id}")
    public ResponseEntity<WaiterDeletedResponse> deleteWaiter(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.deleteWaiter(id));
    }
}
