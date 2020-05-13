package bio.terra.cloudres.util;

import com.google.cloud.resourcemanager.ResourceManagerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static bio.terra.cloudres.util.CloudResourceException.ERROR_MESSAGE_CONNECTOR;
import static org.junit.Assert.*;

@Tag("unit")
public class CloudResourceExceptionTest {

    private static final String CRL_ERROR_MESSAGE = "error1";
    private static final String ORIGINAl_ERROR_MESSAGE = "error2";
    private static final String ERROR_MESSAGE = CRL_ERROR_MESSAGE + ERROR_MESSAGE_CONNECTOR + ORIGINAl_ERROR_MESSAGE;

    @Test
    public void testCloudResourceException() {
        CloudResourceException exception = new CloudResourceException(CRL_ERROR_MESSAGE, new ResourceManagerException(500, ORIGINAl_ERROR_MESSAGE));
        assertEquals(500, exception.getCode());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
        assertNull(exception.getReason());
        assertTrue(exception.isRetryable());

        exception = new CloudResourceException(CRL_ERROR_MESSAGE, new ResourceManagerException(403, ORIGINAl_ERROR_MESSAGE));
        assertEquals(403, exception.getCode());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
        assertNull(exception.getReason());
        assertFalse(exception.isRetryable());

        IOException cause = new SocketTimeoutException();
        exception = new CloudResourceException(CRL_ERROR_MESSAGE, new ResourceManagerException(404, ORIGINAl_ERROR_MESSAGE, cause));
        assertEquals(404, exception.getCode());
        assertEquals(ERROR_MESSAGE, exception.getMessage());
        assertNull(exception.getReason());
        assertFalse(exception.isRetryable());
        assertSame(cause, exception.getCause().getCause());
    }
}
