package bio.terra.cloudres.google.cloudresourcemanager.operation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/** An interface to unite the different api services model classes of the same "operation" data. */
public interface OperationAdapter<T> {
  /** Return the underlying Operation. */
  T getOperation();

  String getName();

  @Nullable
  Boolean getDone();

  /** Returns a StatusAdapter for the status error if there is one, or else null. */
  StatusAdapter getError();

  /**
   * An interface to unite the different api services model classes of the same status error data.
   */
  interface StatusAdapter {
    Integer getCode();

    String getMessage();

    List<Map<String, Object>> getDetails();
  }

  /** A factory for creating {@link OperationAdapter}s. */
  @FunctionalInterface
  interface Factory<T> {
    OperationAdapter<T> create(T operation);
  }
}
