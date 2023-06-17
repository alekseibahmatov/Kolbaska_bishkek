package ee.maitsetuur.exception;

public class RestaurantNotFoundException extends Exception {
    public RestaurantNotFoundException(String message) {
        super(message,null,false,false);
    }
}
