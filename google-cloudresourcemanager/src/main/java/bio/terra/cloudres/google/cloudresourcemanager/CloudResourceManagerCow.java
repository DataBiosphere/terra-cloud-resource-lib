package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.ClientConfig;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link CloudResourceManager} */
public class CloudResourceManagerCow {
  private final ClientConfig clientConfig;
  private final CloudResourceManager manager;

  public CloudResourceManagerCow(
      ClientConfig clientConfig, CloudResourceManager.Builder managerBuilder) {
    this.clientConfig = clientConfig;
    this.manager = managerBuilder.build();
  }
}
