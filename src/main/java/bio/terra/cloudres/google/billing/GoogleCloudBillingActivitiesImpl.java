package bio.terra.cloudres.google.billing;

import com.uber.cadence.workflow.Workflow;

import java.io.IOException;

public class GoogleCloudBillingActivitiesImpl implements GoogleCloudBillingActivities {
    private final GoogleCloudBilling googleCloudBilling;

    public GoogleCloudBillingActivitiesImpl(GoogleCloudBilling googleCloudBilling) {
        this.googleCloudBilling = googleCloudBilling;
    }

    @Override
    public void setBilling(String projectId, String billingAccount) {
        try {
            this.googleCloudBilling.setBillingRaw(projectId, billingAccount);
        } catch (IOException e) {
            throw Workflow.wrap(e);
        }
    }
}
