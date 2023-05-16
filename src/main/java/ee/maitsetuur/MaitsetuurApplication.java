package ee.maitsetuur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MaitsetuurApplication {

    private static final Logger logger = LoggerFactory.getLogger(MaitsetuurApplication.class);

    public static void main(String[] args) {
        logger.info("Backend started");
        SpringApplication.run(MaitsetuurApplication.class, args);
    }

}
