package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.request.ManagerCustomerUpdateRequest;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.CustomerInformationResponse;
import ee.kolbaska.kolbaska.response.CustomerUpdateResponse;
import ee.kolbaska.kolbaska.response.WaiterDeletedResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.ManagerRestaurantService;
import jakarta.validation.constraints.NotNull;
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
            @NotNull @RequestBody WaiterRequest request
            ) throws Exception {
        return ResponseEntity.ok(service.createWaiter(request));
    }

    @GetMapping("/restaurant/waiter")
    public ResponseEntity<List<WaiterResponse>> getWaiters() throws RestaurantNotFoundException {
        return ResponseEntity.ok(service.getWaiters());
    }

    @GetMapping("/restaurant/waiter/{id}")
    public ResponseEntity<CustomerInformationResponse> getWaiter(
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getWaiter(id));
    }

    @PutMapping("/restaurant/waiter")
    public ResponseEntity<CustomerUpdateResponse> updateWaiter(
            @NotNull @RequestBody ManagerCustomerUpdateRequest request
            ) throws UsernameNotFoundException {
        return ResponseEntity.ok(service.updateWaiter(request));
    }

    @DeleteMapping("/restaurant/waiter/{id}")
    public ResponseEntity<WaiterDeletedResponse> deleteWaiter(
            @NotNull @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.deleteWaiter(id));
    }
}
