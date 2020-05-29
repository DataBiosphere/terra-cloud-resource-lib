package bio.terra.cloudres.common;

/**
 * interface for a single Cloud Operation
 *
 * <p> We expect each Cow implements its own CowOperations includes:
 * <ul>
 *     <li> The {@code CloudOperation}
 *     <li> how to execute this operation
 *     <li> how to serialize Request
 * </ul>
 *
 */
public interface CowOperation<R> {
  CloudOperation getCloudOperation();

  R execute();

  String serializeRequest();
}
