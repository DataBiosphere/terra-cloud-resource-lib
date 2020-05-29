package bio.terra.cloudres.common;

public interface CowOperation<R> {
  CloudOperation getCloudOperation();

  R execute();

  String serializeRequest();
}
