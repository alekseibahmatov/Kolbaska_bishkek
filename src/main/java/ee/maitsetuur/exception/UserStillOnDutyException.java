package ee.maitsetuur.exception;

public class UserStillOnDutyException extends Exception {
    public UserStillOnDutyException(String message) {
        super(message,null,false,false);
    }
}
