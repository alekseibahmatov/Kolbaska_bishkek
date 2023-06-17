package ee.maitsetuur.exception;

public class CertificateNotFoundException extends Exception {
    public CertificateNotFoundException(String message) {
        super(message,null,false,false);

    }
}
