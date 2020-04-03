package bio.terra.cloudres.google.serviceusage;

import com.google.api.services.serviceusage.v1beta1.model.Operation;
import com.uber.cadence.workflow.Workflow;

import java.io.IOException;
import java.util.List;

public class GoogleServiceUsageActivitiesImpl implements GoogleServiceUsageActivities {
    private final GoogleServiceUsage googleServiceUsage;

    public GoogleServiceUsageActivitiesImpl(GoogleServiceUsage googleServiceUsage) {
        this.googleServiceUsage = googleServiceUsage;
    }

    @Override
    public String batchEnable(String projectId, List<String> services) {
        try {
            return this.googleServiceUsage.batchEnableRaw(projectId, services).getName();
        } catch (IOException e) {
            throw Workflow.wrap(e);
        }
    }

    @Override
    public Operation getOperation(String operationName) {
        try {
            return this.googleServiceUsage.getOperationRaw(operationName);
        } catch (IOException ioe) {
            throw Workflow.wrap(ioe);
        }
    }
}
