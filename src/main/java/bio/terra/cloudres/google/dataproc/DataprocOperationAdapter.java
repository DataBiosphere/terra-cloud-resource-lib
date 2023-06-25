package bio.terra.cloudres.google.dataproc;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import com.google.api.services.dataproc.model.Operation;
import com.google.api.services.dataproc.model.Status;
import java.util.List;
import java.util.Map;

/** A {@link OperationCow.OperationAdapter} for {@link Operation}. */
class DataprocOperationAdapter implements OperationCow.OperationAdapter<Operation> {
  private final Operation operation;

  private static final String DONE = "DONE";

  DataprocOperationAdapter(Operation operation) {
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

  /** A {@link OperationCow.OperationAdapter.StatusAdapter} for errors in {@link Operation}. */
  private record StatusAdapter(Status status)
      implements OperationCow.OperationAdapter.StatusAdapter {

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
