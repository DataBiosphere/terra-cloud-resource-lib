package bio.terra.cloudres.google.api.services.common;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.ResourceMetadata;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Optional;

/** An abstract Cloud Object Wrapper to mirror {@link AbstractGoogleClientRequest}. */
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
  public final T execute() throws IOException {
    Optional<ResourceMetadata> metadata = resourceCreationMetadata();
    Preconditions.checkState(
        metadata.isEmpty() || resourceUidCreation().isPresent(),
        "If the ResourceMetadata is specified, a CloudResourceUid must be specified for the metadata to be meaningful.");
    resourceUidCreation()
        .ifPresent(
            resourceUid ->
                CleanupRecorder.record(resourceUid, metadata.orElse(null), clientConfig));
    return operationAnnotator.executeCheckedCowOperation(
        operation, request::execute, this::serialize);
  }

  /**
   * The {@link CloudResourceUid} of the resource that will be created by this request, if this
   * request creates a resource.
   *
   * <p>Should be overridden by subclasses that create resources.
   */
  protected Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.empty();
  }

  /**
   * The {@link ResourceMetadata} of the resource that will be created by this request, if this
   * request creates a resource that should have metadata.
   *
   * <p>Should only be non-empty when {@link #resourceUidCreation()} is present.
   */
  protected Optional<ResourceMetadata> resourceCreationMetadata() {
    return Optional.empty();
  }

  /** How to serialize the request for logging. */
  protected abstract JsonObject serialize();
}
