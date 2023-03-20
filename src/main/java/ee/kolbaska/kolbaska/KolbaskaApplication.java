package ee.kolbaska.kolbaska;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KolbaskaApplication {

    private static final Logger logger = LoggerFactory.getLogger(KolbaskaApplication.class);

    public static void main(String[] args) {
        logger.info("Backend started");
        SpringApplication.run(KolbaskaApplication.class, args);
    }

}
