package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google Cloud Resource Manager API. */
public enum CloudResourceManagerOperation implements CloudOperation {
  GOOGLE_CREATE_PROJECT,
  GOOGLE_DELETE_PROJECT,
  GOOGLE_GET_PROJECT,
  GOOGLE_GET_IAM_POLICY_PROJECT,
  GOOGLE_SET_IAM_POLICY_PROJECT,
  GOOGLE_TEST_IAM_PERMISSIONS_FOLDER,
  GOOGLE_RESOURCE_MANAGER_OPERATION_GET,
}
