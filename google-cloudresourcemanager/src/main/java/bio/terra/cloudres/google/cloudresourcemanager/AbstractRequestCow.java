package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import bio.terra.janitor.model.CloudResourceUid;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Optional;

/**
 * An abstract Cloud Object Wrapper to mirror {@link
 * com.google.api.client.googleapis.services.AbstractGoogleClientRequest}.
 */
// TODO move this class to a new google-apicommon package.
public abstract class AbstractRequestCow<T> {
  private final AbstractGoogleClientRequest<T> request;

  private final CloudOperation operation;
  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;

  protected AbstractRequestCow(
      CloudOperation operation,
      ClientConfig clientConfig,
      OperationAnnotator operationAnnotator,
      AbstractGoogleClientRequest<T> request) {
    this.operation = operation;
    this.clientConfig = clientConfig;
    this.request = request;
    this.operationAnnotator = operationAnnotator;
  }

  /** See {@link AbstractGoogleClientRequest#execute()}. */
  public T execute() throws IOException {
    resourceUidCreation()
        .ifPresent(resourceUid -> CleanupRecorder.record(resourceUid, clientConfig));
    return operationAnnotator.executeCheckedCowOperation(
        operation, request::execute, this::serialize);
  }

  /**
   * The {@link CloudResourceUid} of the reosurce that will be created by this request, if this
   * request creates a resource.
   *
   * <p>Should be overrided by subclasses that create resources.
   */
  protected Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.empty();
  }

  /** How to serialize the request for logging. */
  protected abstract JsonObject serialize();
}
