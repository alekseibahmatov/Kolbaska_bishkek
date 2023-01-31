package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.ManagerRestaurantService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basepath}/manager")
@RequiredArgsConstructor
public class ManagerRestaurantController {

    private final ManagerRestaurantService service;

    @PostMapping("/restaurant/waiter")
    public ResponseEntity<WaiterResponse> createWaiter(
            @NotNull @RequestBody WaiterRequest request
            ) throws Exception {
        return service.createWaiter(request);
    }
}
