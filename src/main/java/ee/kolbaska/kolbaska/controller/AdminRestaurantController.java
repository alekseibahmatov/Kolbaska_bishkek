package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.request.RestaurantUpdateRequest;
import ee.kolbaska.kolbaska.response.RestaurantDisableResponse;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.response.RestaurantUpdateResponse;
import ee.kolbaska.kolbaska.service.AdminRestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminRestaurantController {

    private final AdminRestaurantService service;

    @PostMapping(value = "/restaurant", consumes = "multipart/form-data")
    public ResponseEntity<RestaurantTableResponse> createRestaurant(
            @Valid @ModelAttribute RestaurantRequest request
    ) throws Exception {
        return ResponseEntity.ok(service.createRestaurant(request));
    }

    @GetMapping("/category")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(service.getCategories());
    }

    @GetMapping("/restaurant")
    public ResponseEntity<List<RestaurantTableResponse>> returnRestaurantList() {
        return ResponseEntity.ok(service.returnRestaurantList());
    }

    @GetMapping("/restaurant/{code}")
    public ResponseEntity<RestaurantResponse> returnRestaurant(
            @Valid @PathVariable String code
    ) throws Exception {
        return ResponseEntity.ok(service.returnRestaurant(code));
    }

    @PutMapping(value = "/restaurant", consumes = "multipart/form-data")
    public ResponseEntity<RestaurantUpdateResponse> updateRestaurant(
            @Valid @ModelAttribute RestaurantUpdateRequest request
    ) throws Exception {
        return ResponseEntity.ok(service.updateRestaurant(request));
    }

    @DeleteMapping("/restaurant/{code}")
    public ResponseEntity<RestaurantDisableResponse> disableRestaurant(
            @Valid @PathVariable String code
    ) throws RestaurantNotFoundException {
        return ResponseEntity.ok(service.disableRestaurant(code));
    }
}
