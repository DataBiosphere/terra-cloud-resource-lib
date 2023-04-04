package bio.terra.cloudres.aws.notebook;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using AWS Sagemaker Notebook API. */
public enum SageMakerNotebookOperation implements CloudOperation {
  AWS_CREATE_NOTEBOOK,
  AWS_DELETE_NOTEBOOK,
  AWS_GET_NOTEBOOK,
  AWS_START_NOTEBOOK,
  AWS_STOP_NOTEBOOK
}
