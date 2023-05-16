package ee.maitsetuur.exception;

public class CertificateIsDisabledException extends Exception {
    public CertificateIsDisabledException(String message) {
        super(message);
    }
}
