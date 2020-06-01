package bio.terra.cloudres.common;

import com.google.gson.JsonObject;

/**
 * Abstraction about what {@link OperationAnnotator} needs to know about a single cloud operation to
 * annotate what happens on execution.
 */
public interface CowOperation<R> {
  /** Gets the The {@code CloudOperation}. */
  CloudOperation getCloudOperation();

  /** How to execute this operation */
  R execute();

  /** How to serialize Request */
  JsonObject serializeRequest();
}
