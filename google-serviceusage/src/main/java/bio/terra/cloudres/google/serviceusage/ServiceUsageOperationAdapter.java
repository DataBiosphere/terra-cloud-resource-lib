package bio.terra.cloudres.google.serviceusage;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import com.google.api.services.serviceusage.v1.model.Operation;
import com.google.api.services.serviceusage.v1.model.Status;
import java.util.List;
import java.util.Map;

/** A {@link OperationCow.OperationAdapter} for {@link Operation}. */
class ServiceUsageOperationAdapter implements OperationCow.OperationAdapter<Operation> {
  private final Operation operation;

  ServiceUsageOperationAdapter(Operation operation) {
    this.operation = operation;
  }

  @Override
  public Operation getOperation() {
    return operation;
  }

  @Override
  public String getName() {
    return operation.getName();
  }

  @Override
  public Boolean getDone() {
    return operation.getDone();
  }

  @Override
  public StatusAdapter getError() {
    Status status = operation.getError();
    if (status == null) {
      return null;
    }
    return new StatusAdapter(status);
  }

  /** A {@link OperationCow.OperationAdapter.StatusAdapter} for {@link Status}. */
  private static class StatusAdapter implements OperationCow.OperationAdapter.StatusAdapter {
    private final Status status;

    private StatusAdapter(Status status) {
      this.status = status;
    }

    @Override
    public Integer getCode() {
      return status.getCode();
    }

    @Override
    public String getMessage() {
      return status.getMessage();
    }

    @Override
    public List<Map<String, Object>> getDetails() {
      return status.getDetails();
    }
  }
}
