package bio.terra.cloudres.google.billing;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.billing.v1.CloudBillingClient;
import com.google.cloud.billing.v1.CloudBillingSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CloudBillingClientCow implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(CloudBillingClientCow.class);

    private final OperationAnnotator operationAnnotator;
    private final CloudBillingClient billing;

    private final ClientConfig clientConfig;

    public CloudBillingClientCow(ClientConfig clientConfig) throws IOException {
        this(clientConfig, CloudBillingSettings.newBuilder().build());
    }

    public CloudBillingClientCow(ClientConfig clientConfig, CloudBillingSettings settings) throws IOException {
        this.clientConfig = clientConfig;
        this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
        this.billing = CloudBillingClient.create(settings);
    }

    @Override
    public void close() {
        billing.close();
    }
}
