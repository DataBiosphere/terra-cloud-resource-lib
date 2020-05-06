package bio.terra.cloudres.util;

import com.google.cloud.http.BaseHttpServiceException;

/** Terra Cloud Resource Library exception.*/
public class CloudResourceException extends Exception {
    // VisiableForTesting
    static final String ERROR_MESSAGE_CONNECTOR = " the error from the original cloud provider: ";

    private final int code;
    private final boolean retryable;
    private final String reason;

    public CloudResourceException(String message) {
        this(message, null);
    }

    /** Constructor for Google API related exceptions. */
    public CloudResourceException(String message, BaseHttpServiceException ex) {
        super(message + ERROR_MESSAGE_CONNECTOR + ex.getMessage(), ex);
        this.retryable = ex.isRetryable();
        this.reason = ex.getReason();
        this.code = ex.getCode();
    }

    public int getCode() {
        return code;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public String getReason() {
        return reason;
    }
}
