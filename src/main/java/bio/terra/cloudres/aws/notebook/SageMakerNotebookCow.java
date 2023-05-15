package bio.terra.cloudres.aws.notebook;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.waiters.WaiterOverrideConfiguration;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateNotebookInstanceRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateNotebookInstanceResponse;
import software.amazon.awssdk.services.sagemaker.model.CreatePresignedNotebookInstanceUrlRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteNotebookInstanceRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeNotebookInstanceRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeNotebookInstanceResponse;
import software.amazon.awssdk.services.sagemaker.model.NotebookInstanceStatus;
import software.amazon.awssdk.services.sagemaker.model.StartNotebookInstanceRequest;
import software.amazon.awssdk.services.sagemaker.model.StopNotebookInstanceRequest;
import software.amazon.awssdk.services.sagemaker.waiters.SageMakerWaiter;

/**
 * A Cloud Object Wrapper(COW) for AWS SageMakerClient Library: {@link SageMakerClient}. Generally,
 * this should be used inside a try-with-resources block in order to close the underlying
 * SageMakerClient properly after use.
 */
public class SageMakerNotebookCow implements AutoCloseable {

  private static final Duration SAGEMAKER_NOTEBOOK_WAITER_TIMEOUT_DURATION =
      Duration.ofSeconds(900);
  private static Logger logger = LoggerFactory.getLogger(SageMakerNotebookCow.class);
  private final OperationAnnotator operationAnnotator;
  private final SageMakerClient notebooksClient;
  private final SageMakerWaiter notebooksWaiter;

  @VisibleForTesting
  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  public SageMakerNotebookCow(
      ClientConfig clientConfig, SageMakerClient notebooksClient, SageMakerWaiter notebooksWaiter) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.notebooksClient = notebooksClient;
    this.notebooksWaiter = notebooksWaiter;
  }

  /** Create a {@link SageMakerNotebookCow} with some default configurations for convenience. */
  public static SageMakerNotebookCow create(
      ClientConfig clientConfig, AwsCredentialsProvider credentials, String region) {
    SageMakerClient notebooksClient =
        SageMakerClient.builder()
            .region(Region.of(region))
            .credentialsProvider(credentials)
            .build();
    return new SageMakerNotebookCow(
        clientConfig,
        notebooksClient,
        SageMakerWaiter.builder()
            .client(notebooksClient)
            .overrideConfiguration(
                WaiterOverrideConfiguration.builder()
                    .waitTimeout(SAGEMAKER_NOTEBOOK_WAITER_TIMEOUT_DURATION)
                    .build())
            .build());
  }

  public CreateNotebookInstanceResponse create(CreateNotebookInstanceRequest request) {
    return operationAnnotator.executeCowOperation(
        SageMakerNotebookOperation.AWS_CREATE_NOTEBOOK,
        () -> notebooksClient.createNotebookInstance(request),
        () -> createJsonObjectWithSingleField("request", request));
  }

  /** Get information about an existing SageMaker notebook instance. */
  public DescribeNotebookInstanceResponse get(String instanceName) {
    return operationAnnotator.executeCowOperation(
        SageMakerNotebookOperation.AWS_GET_NOTEBOOK,
        () ->
            notebooksClient.describeNotebookInstance(
                DescribeNotebookInstanceRequest.builder()
                    .notebookInstanceName(instanceName)
                    .build()),
        () -> serializeInstanceName(instanceName));
  }

  public String createPresignedUrl(String instanceName) {
    return operationAnnotator
        .executeCowOperation(
            SageMakerNotebookOperation.AWS_CREATE_PRESIGNED_URL_NOTEBOOK,
            () ->
                notebooksClient.createPresignedNotebookInstanceUrl(
                    CreatePresignedNotebookInstanceUrlRequest.builder()
                        .notebookInstanceName(instanceName)
                        .build()),
            () -> serializeInstanceName(instanceName))
        .authorizedUrl();
  }

  /**
   * Request that SageMaker start a notebook instance. This method will return immediately after
   * sending the request, use {@link #startAndWait(String)} to additionally wait until the instance
   * is available to a user.
   */
  public void start(String instanceName) {
    operationAnnotator.executeCowOperation(
        SageMakerNotebookOperation.AWS_START_NOTEBOOK,
        () ->
            notebooksClient.startNotebookInstance(
                StartNotebookInstanceRequest.builder().notebookInstanceName(instanceName).build()),
        () -> serializeInstanceName(instanceName));
  }

  /**
   * Request that SageMaker start a notebook instance and block until the instance is usable. To
   * return immediately after making the request instead, use {@link #start(String)}.
   */
  public void startAndWait(String instanceName) {
    start(instanceName);
    pollForNotebookStatus(instanceName, NotebookInstanceStatus.IN_SERVICE);
  }

  /**
   * Request that SageMaker stop a notebook instance. This method will return immediately after
   * sending the request, use {@link #stopAndWait(String)} to additionally wait until the instance
   * is fully stopped.
   */
  public void stop(String instanceName) {
    operationAnnotator.executeCowOperation(
        SageMakerNotebookOperation.AWS_STOP_NOTEBOOK,
        () ->
            notebooksClient.stopNotebookInstance(
                StopNotebookInstanceRequest.builder().notebookInstanceName(instanceName).build()),
        () -> serializeInstanceName(instanceName));
  }

  /**
   * Request that SageMaker stop a notebook instance and block until the instance is stopped. To
   * return immediately after making the request instead, use {@link #stop(String)}.
   */
  public void stopAndWait(String instanceName) {
    stop(instanceName);
    pollForNotebookStatus(instanceName, NotebookInstanceStatus.STOPPED);
  }

  /** Delete a SageMaker notebook. */
  public void delete(String instanceName) {
    operationAnnotator.executeCowOperation(
        SageMakerNotebookOperation.AWS_DELETE_NOTEBOOK,
        () ->
            notebooksClient.deleteNotebookInstance(
                DeleteNotebookInstanceRequest.builder().notebookInstanceName(instanceName).build()),
        () -> serializeInstanceName(instanceName));
  }

  private void pollForNotebookStatus(String instanceName, NotebookInstanceStatus expectedStatus) {
    WaiterResponse<DescribeNotebookInstanceResponse> waiterResponse;
    DescribeNotebookInstanceRequest describeRequest =
        DescribeNotebookInstanceRequest.builder().notebookInstanceName(instanceName).build();

    if (expectedStatus == NotebookInstanceStatus.IN_SERVICE) {
      waiterResponse = notebooksWaiter.waitUntilNotebookInstanceInService(describeRequest);
    } else if (expectedStatus == NotebookInstanceStatus.STOPPED) {
      waiterResponse = notebooksWaiter.waitUntilNotebookInstanceStopped(describeRequest);
    } else {
      throw new UnsupportedOperationException(
          "Unsupported poll for expected notebook status " + expectedStatus.toString());
    }

    ResponseOrException<DescribeNotebookInstanceResponse> responseOrException =
        waiterResponse.matched();
    if (responseOrException.exception().isPresent()) {
      // Log and surface any errors from AWS, clients should handle these appropriately.
      var t = responseOrException.exception().get();
      logger.error("Error polling notebook instance expectedStatus {}: ", expectedStatus, t);
      throw new CrlSageMakerException("Error while polling for notebook status: ", t);
    } else if (responseOrException.response().isEmpty()) {
      logger.error(
          "Encountered ResponseOrException without response or exception. This should never happen.");
    }
  }

  /** Utility to create a JsonObject with a single key-value pair */
  @VisibleForTesting
  public JsonObject createJsonObjectWithSingleField(String key, Object value) {
    var obj = new JsonObject();
    obj.addProperty(key, value.toString());
    return obj;
  }

  @VisibleForTesting
  public JsonObject serializeInstanceName(String instanceName) {
    return createJsonObjectWithSingleField("instanceName", instanceName);
  }

  @Override
  public void close() {
    notebooksClient.close();
    // Per documentation, the waiter will not close the SDK client on a call to close().
    notebooksWaiter.close();
  }
}
