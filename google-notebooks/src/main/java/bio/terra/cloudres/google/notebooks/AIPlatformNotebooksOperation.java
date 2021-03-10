package bio.terra.cloudres.google.notebooks;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google Cloud Resource Manager API. */
public enum AIPlatformNotebooksOperation implements CloudOperation {
  GOOGLE_CREATE_NOTEBOOKS_INSTANCE,
  GOOGLE_DELETE_NOTEBOOKS_INSTANCE,
  GOOGLE_GET_NOTEBOOKS_INSTANCE,
  GOOGLE_NOTEBOOKS_OPERATION_GET,
}
