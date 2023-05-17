package ee.maitsetuur.scheduledTasks;

import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.repository.RestaurantRepository;
import ee.maitsetuur.service.AdminRestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class ReportCreator {

    private RestaurantRepository restaurantRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRestaurantService.class);

    @Scheduled(cron = "0 0 * * *")
    public void generateReport() {
        LOGGER.info("Starting to generate report");

        Integer dayOfMonth = LocalDate.now().getDayOfMonth();

        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant r : restaurants) {
            List<Integer> reportDays = Arrays.stream(r.getReportDays().split(",")).toList().stream().map(Integer::valueOf).toList();

            if (!reportDays.contains(dayOfMonth)) continue;


        }
    }

}
