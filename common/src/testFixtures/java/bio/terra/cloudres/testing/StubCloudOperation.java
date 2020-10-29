package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.CloudOperation;

/** A stub {@link CloudOperation} for test */
public enum StubCloudOperation implements CloudOperation {
  GOOGLE_CREATE_PROJECT {
    @Override
    public String getName() {
      return "GOOGLE_CREATE_PROJECT";
    }
  }
}
