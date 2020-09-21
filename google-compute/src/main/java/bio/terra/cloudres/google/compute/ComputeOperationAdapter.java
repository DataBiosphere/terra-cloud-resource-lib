package bio.terra.cloudres.google.compute;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import com.google.api.services.compute.model.Operation;
import com.google.api.services.compute.model.Operation.Error;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A {@link OperationCow.OperationAdapter} for {@link Operation}. */
class ComputeOperationAdapter implements OperationCow.OperationAdapter<Operation> {
  private final Operation operation;

  private static final String DONE = "DONE";

  ComputeOperationAdapter(Operation operation) {
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
    return operation.getStatus().equals(DONE);
  }

  @Override
  public StatusAdapter getError() {
    Error error = operation.getError();
    if (error == null) {
      return null;
    }
    return new StatusAdapter(operation);
  }

  /** A {@link OperationCow.OperationAdapter.StatusAdapter} for errors in {@link Operation}. */
  private static class StatusAdapter implements OperationCow.OperationAdapter.StatusAdapter {
    private final Operation operation;

    private StatusAdapter(Operation operation) {
      this.operation = operation;
    }

    @Override
    public Integer getCode() {
      return operation.getHttpErrorStatusCode();
    }

    @Override
    public String getMessage() {
      return operation.getHttpErrorMessage();
    }

    /** Convert {@link Error.Errors} to raw {@code List<Map<String, Object>>} type. */
    @Override
    public List<Map<String, Object>> getDetails() {
      List<Map<String, Object>> errorDetails = new ArrayList<>();
      operation.getError().getErrors().forEach(e -> errorDetails.add(new HashMap<>(e)));
      return errorDetails;
    }
  }
}
