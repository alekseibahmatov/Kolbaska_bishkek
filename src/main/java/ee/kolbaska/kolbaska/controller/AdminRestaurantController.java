package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.service.AdminRestaurantService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminRestaurantController {

    private final AdminRestaurantService service;

    @PostMapping("/restaurant")
    public ResponseEntity<RestaurantTableResponse> createRestaurant(
            @NotNull @RequestBody RestaurantRequest request
    ) throws Exception {
        return service.createRestaurant(request);
    }

    @GetMapping("/category")
    public ResponseEntity<List<String>> getCategories() {
        return service.getCategories();
    }
}
