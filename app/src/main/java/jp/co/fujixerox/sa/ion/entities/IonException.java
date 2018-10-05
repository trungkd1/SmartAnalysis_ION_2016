package jp.co.fujixerox.sa.ion.entities;

/**
 * Custom Exception
 */
public class IonException extends Exception {
    private static final long serialVersionUID = 1L;
    private int errorCode;


    public IonException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public IonException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
