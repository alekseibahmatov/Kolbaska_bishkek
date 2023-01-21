package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RestaurantRepository restaurantRepository;
    public ResponseEntity<List<RestaurantTableResponse>> returnRestaurantList() {

        List<RestaurantTableResponse> response = restaurantRepository.findAll()
                .stream().map(restaurant -> new RestaurantTableResponse(restaurant.getName(), restaurant.getAverageBill()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
