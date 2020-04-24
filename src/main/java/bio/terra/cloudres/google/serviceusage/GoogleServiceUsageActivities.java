package bio.terra.cloudres.google.serviceusage;

import com.google.api.services.serviceusage.v1beta1.model.Operation;
import java.util.List;

public interface GoogleServiceUsageActivities {
  String batchEnable(String projectId, List<String> services);

  Operation getOperation(String operationName);
}
