package ee.maitsetuur.exception;

public class CertificateIsOutDatedException extends Exception {
    public CertificateIsOutDatedException(String message) {
        super(message,null,false,false);

    }
}
