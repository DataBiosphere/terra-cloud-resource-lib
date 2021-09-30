package bio.terra.cloudres.azure.resourcemanager.common;

import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.ResourceMetadata;
import com.google.gson.JsonObject;
import java.util.Optional;

/**
 * An abstract representation of data passed to Azure Resource Manager requests.
 *
 * <p>Contains functionality for serializing request data for structured logging; and tracking
 * resource creations for clean-up.
 */
public interface ResourceManagerRequestData {

  /** The {@link CloudOperation} value for this request. */
  CloudOperation cloudOperation();

  /**
   * The {@link CloudResourceUid} of the resource that will be created by this request, if this
   * request creates a resource.
   *
   * <p>Should be overridden by subclasses that create resources.
   */
  default Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.empty();
  }

  /**
   * The {@link ResourceMetadata} of the resource that will be created by this request, if this
   * request creates a resource that should have metadata.
   *
   * <p>Should only be non-empty when {@link #resourceUidCreation()} is present.
   */
  default Optional<ResourceMetadata> resourceCreationMetadata() {
    return Optional.empty();
  }

  /** How to serialize the request for logging. */
  JsonObject serialize();
}
