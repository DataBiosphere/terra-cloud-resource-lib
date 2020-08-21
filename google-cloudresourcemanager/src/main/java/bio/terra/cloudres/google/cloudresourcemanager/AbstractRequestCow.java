package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.gson.JsonObject;
import java.io.IOException;

/**
 * An abstract Cloud Object Wrapper to mirror {@link
 * com.google.api.client.googleapis.services.AbstractGoogleClientRequest}.
 */
// TODO move this class to a new google-apicommon package.
public abstract class AbstractRequestCow<T> {
  private final CloudOperation operation;
  private final AbstractGoogleClientRequest<T> request;
  private final OperationAnnotator operationAnnotator;

  protected AbstractRequestCow(
      CloudOperation operation,
      AbstractGoogleClientRequest<T> request,
      OperationAnnotator operationAnnotator) {
    this.operation = operation;
    this.request = request;
    this.operationAnnotator = operationAnnotator;
  }

  public T execute() throws IOException {
    return operationAnnotator.executeCheckedCowOperation(
        operation, request::execute, this::serialize);
  }

  /** How to serialize the request for logging. */
  protected abstract JsonObject serialize();
}
