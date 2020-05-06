package bio.terra.cloudres.util;

import com.google.cloud.http.BaseHttpServiceException;

/** Terra Cloud Resource Library exception.*/
public class CloudResourceException extends Exception {
    private final int code;
    private final boolean retryable;
    private final String reason;
    private final String location;
    private final String debugInfo;

    public CloudResourceException(String message) {
        this(message, null);
    }

    /** Constructor for Google API related exceptions. */
    public CloudResourceException(String message, BaseHttpServiceException ex) {
        super(message, ex);
        this.debugInfo = ex.getDebugInfo();
        this.retryable = ex.isRetryable();
        this.location = ex.getLocation();
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

    public String getLocation() {
        return location;
    }

    public String getDebugInfo() {
        return debugInfo;
    }
}
