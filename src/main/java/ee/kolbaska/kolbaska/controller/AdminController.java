package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.service.AdminService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @GetMapping("/restaurant")
    public ResponseEntity<List<RestaurantTableResponse>> returnRestaurantList() {
        return ResponseEntity.ok(service.returnRestaurantList());
    }

    @GetMapping("/restaurant/{code}")
    public ResponseEntity<RestaurantResponse> returnRestaurant(
            @NotNull @PathVariable String code
    ) throws Exception {
        return ResponseEntity.ok(service.returnRestaurant(code));
    }
}
