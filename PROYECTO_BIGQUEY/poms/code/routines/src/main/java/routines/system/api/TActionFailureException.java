package routines.system.api;

public class TActionFailureException extends Exception {

    private static final long serialVersionUID = 1L;

    private String errorType;

    public TActionFailureException(String errorType, String errorMessage) {
        super(errorMessage);
        this.errorType = errorType;
    }

    public String getErrorType() {
        return this.errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
}
