package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.CloudOperation;

/** A stub {@link CloudOperation} for test */
public enum StubCloudOperation implements CloudOperation {
  TEST_OPERATION {
    @Override
    public String getName() {
      return this.name();
    }
  }
}
