package bio.terra.cloudres.google.cloudresourcemanager.operation;

import bio.terra.cloudres.google.cloudresourcemanager.AbstractRequestCow;
import java.io.IOException;

/**
 * A Cloud Object Wrapper (COW) for api-services Operations.
 *
 * <p>Each api service has its own classes for Operations that follow the same pattern. This Cow
 * adds some higher level abstractions for treating them similarly.
 */
public class OperationCow<T> {
  /** The underlying api service Operation (within an adapter). */
  private final OperationAdapter<T> operationAdapter;

  private final OperationAdapter.Factory<T> operationAdapterFactory;
  private final GetRequestFactory<T> getRequestFactory;

  public OperationCow(
      T operation,
      OperationAdapter.Factory<T> operationAdapterFactory,
      GetRequestFactory<T> getRequestFactory) {
    this.operationAdapterFactory = operationAdapterFactory;
    this.getRequestFactory = getRequestFactory;
    this.operationAdapter = operationAdapterFactory.create(operation);
  }

  /** Returns the underlying Operation wrapped by this Cow. */
  public T getOperation() {
    return operationAdapter.getOperation();
  }

  public OperationAdapter<T> getOperationAdapter() {
    return operationAdapter;
  }

  /**
   * Execute a "get" for the Operation, returning a new OperationCow with the updated Operation
   * information.
   */
  public OperationCow<T> executeGet() throws IOException {
    AbstractRequestCow<T> getRequest = getRequestFactory.create(getOperation());
    T newOperation = getRequest.execute();
    return new OperationCow(newOperation, operationAdapterFactory, getRequestFactory);
  }

  /**
   * Factory interface for creating new {@link AbstractRequestCow}s for getting operation updates.
   */
  @FunctionalInterface
  public interface GetRequestFactory<T> {
    AbstractRequestCow<T> create(T operation) throws IOException;
  }
}
