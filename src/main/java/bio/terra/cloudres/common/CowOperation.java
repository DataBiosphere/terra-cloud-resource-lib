package bio.terra.cloudres.common;

import com.google.gson.JsonObject;

/**
 * Abstraction about what {@link OperationAnnotator} needs to know about a single cloud operation to
 * annotate what happens on execution.
 */
public class CowOperation {

  /** How to execute this operation */
  public interface CowExecute<R> {
    R execute();
  }

  /** How to serialize Request */
  public interface CowSerialize {
    JsonObject serializeRequest();
  }
}
