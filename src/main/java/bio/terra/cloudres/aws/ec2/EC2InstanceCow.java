package bio.terra.cloudres.aws.ec2;

import bio.terra.cloudres.aws.notebook.SageMakerNotebookCow;
import bio.terra.cloudres.common.ClientConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DeleteTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

/**
 * A Cloud Object Wrapper(COW) for AWS Elastic Compute Cloud (EC2) Library ({@link Ec2Client},
 * focusing on Instance-related API calls.
 *
 * <p>Generally, this should be used inside a try-with-resources block in order to close the
 * underlying Ec2Client properly after use.
 */
public class EC2InstanceCow extends EC2CowBase {
  private static Logger logger = LoggerFactory.getLogger(SageMakerNotebookCow.class);

  @VisibleForTesting
  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  /** Constructor for test usage, allows mock injections. */
  @VisibleForTesting
  public EC2InstanceCow(ClientConfig clientConfig, Ec2Client ec2Client, Ec2Waiter ec2Waiter) {
    super(logger, clientConfig, ec2Client, ec2Waiter);
  }

  private EC2InstanceCow(
      Logger logger,
      ClientConfig clientConfig,
      AwsCredentialsProvider credentialsProvider,
      Region region) {
    super(logger, clientConfig, credentialsProvider, region);
  }

  /**
   * Factory method to create an instance of {@link EC2InstanceCow}.
   *
   * @param clientConfig CRL Cloud Config
   * @param credentialsProvider AWS credentials provider to use when making EC2 calls
   * @param region AWS region to target EC2 calls to
   * @return an instance of class {@link EC2InstanceCow}
   */
  public static EC2InstanceCow instanceOf(
      ClientConfig clientConfig, AwsCredentialsProvider credentialsProvider, Region region) {
    return new EC2InstanceCow(logger, clientConfig, credentialsProvider, region);
  }

  /**
   * Run one or more EC2 virtual machine instances.
   *
   * <p>Note that the EC2 API uses the term "run" for what Terra services would generally call a
   * "create" CRUD operation; this COW class aligns to the AWS API nomenclature.
   *
   * @param request {@link RunInstancesRequest} describing the EC2 Instance(s) to create.
   * @return a {@link RunInstancesResponse} describing the created EC2 Instance(s)
   */
  public RunInstancesResponse run(RunInstancesRequest request) {
    return getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_RUN_EC2_INSTANCES,
            () -> getClient().runInstances(request),
            () -> createJsonObjectWithSingleField("request", request));
  }

  /**
   * Gets a description of an EC2 instance
   *
   * @param instanceId ID of the Instance to describe
   * @return {@link Instance} object describing the instance
   */
  public Instance get(String instanceId) {
    DescribeInstancesResponse response =
        getOperationAnnotator()
            .executeCowOperation(
                EC2InstanceOperation.AWS_GET_EC2_INSTANCE,
                () ->
                    getClient()
                        .describeInstances(
                            DescribeInstancesRequest.builder()
                                .instanceIds(List.of(instanceId))
                                .build()),
                () -> serializeInstanceId(instanceId));

    return EC2Utils.extractSingleValue(
        EC2Utils.extractSingleValue(
            response,
            DescribeInstancesResponse::hasReservations,
            DescribeInstancesResponse::reservations),
        Reservation::hasInstances,
        Reservation::instances);
  }

  /**
   * Queries for the description of one or more EC2 Instances by the value of a tag attached to the
   * instance
   *
   * <p>Note that there is no guarantee of uniqueness of an EC2 Instance based on tags, thus the
   * returned response may contain multiple Instance descriptions. Any uniqueness expectations by
   * the consuming application must enforce (and validate) this uniqueness in its own logic.
   *
   * @param key name of the EC2 Instance key to query
   * @param value value of key to query for
   * @return a {@link DescribeInstancesResponse} describing the results of the query
   */
  public DescribeInstancesResponse getByTag(String key, String value) {

    DescribeInstancesRequest request =
        DescribeInstancesRequest.builder()
            .filters(
                List.of(
                    Filter.builder()
                        .name(String.format("tag:%s", key))
                        .values(List.of(value))
                        .build()))
            .build();

    return getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_GET_BY_TAG_EC2_INSTANCE,
            () -> getClient().describeInstances(request),
            () -> createJsonObjectWithSingleField("request", request));
  }

  /**
   * Terminate an EC2 Instance
   *
   * <p>Note that the EC2 API uses the term "terminate" for what Terra services would generally call
   * a "delete" CRUD operation; this COW class aligns to the AWS API nomenclature.
   *
   * @param instanceId ID of Instance to terminate
   */
  public void terminate(String instanceId) {
    getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_TERMINATE_EC2_INSTANCE,
            () ->
                getClient()
                    .terminateInstances(
                        TerminateInstancesRequest.builder()
                            .instanceIds(List.of(instanceId))
                            .build()),
            () -> serializeInstanceId(instanceId));
  }

  /**
   * Terminate an EC2 Instance and wait for it to reach the TERMINATED state
   *
   * <p>Note that the EC2 API uses the term "terminate" for what Terra services would generally call
   * a "delete" CRUD operation; this COW class aligns to the AWS API nomenclature.
   *
   * @param instanceId ID of Instance to terminate
   * @throws {@link CrlEC2Exception} if the API call determines that the TERMINATED state can never
   *     be reached
   */
  public void terminateAndWait(String instanceId) {
    terminate(instanceId);
    waitForState(instanceId, InstanceStateName.TERMINATED);
  }

  /**
   * Start an EC2 Instance that is in the STOPPED state
   *
   * @param instanceId ID of Instance to start
   */
  public void start(String instanceId) {
    getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_START_EC2_INSTANCE,
            () ->
                getClient()
                    .startInstances(
                        StartInstancesRequest.builder().instanceIds(List.of(instanceId)).build()),
            () -> serializeInstanceId(instanceId));
  }

  /**
   * Start an EC2 Instance that is in the Stopped state, waiting for it to reach the RUNNING state
   *
   * @param instanceId ID of Instance to start
   * @throws {@link CrlEC2Exception} if the API call determines that the RUNNING state can never be
   *     reached
   */
  public void startAndWait(String instanceId) {
    start(instanceId);
    waitForState(instanceId, InstanceStateName.RUNNING);
  }

  /**
   * Stop an EC2 Instance that is in the STOPPED state
   *
   * @param instanceId ID of Instance to stop
   */
  public void stop(String instanceId) {
    getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_STOP_EC2_INSTANCE,
            () ->
                getClient()
                    .stopInstances(
                        StopInstancesRequest.builder().instanceIds(List.of(instanceId)).build()),
            () -> serializeInstanceId(instanceId));
  }

  /**
   * Stop an EC2 Instance that is in the Started state, waiting for it to reach the STOPPED state
   *
   * @param instanceId ID of Instance to stop
   * @throws {@link CrlEC2Exception} if the API call determines that the STOPPED state can never be
   *     reached
   */
  public void stopAndWait(String instanceId) {
    stop(instanceId);
    waitForState(instanceId, InstanceStateName.STOPPED);
  }

  /**
   * Wait for an Instance to reach OK Status, meaning the operating system is up and responding to
   * AWS health checks
   *
   * @param instanceId ID of Instance to wait for
   * @throws {@link CrlEC2Exception} if the API call determines that the Instance will never reach
   *     OK status
   */
  public void waitForStatusOK(String instanceId) {
    try {
      getWaiter()
          .waitUntilInstanceStatusOk(
              DescribeInstanceStatusRequest.builder().instanceIds(List.of(instanceId)).build());
    } catch (SdkClientException e) {
      EC2Utils.checkWaiterException(instanceId, e);
    }
  }

  /**
   * Wait for EC2 virtual machine Instance to reach a certain state.
   *
   * <p>The AWS SDK only supports waiting for states RUNNING, STOPPED, or TERMINATED; this
   * implementation aligns to the SDK.
   *
   * @param instanceId ID of Instance to wait for
   * @param expectedState {@link InstanceStateName} to wait for
   * @throws {@link UnsupportedOperationException} if a state other than [RUNNING, STOPPED,
   *     TERMINATED] is passed
   * @throws {@link CrlEC2Exception} if the API call determines that the passed state can never be
   *     reached
   */
  public void waitForState(String instanceId, InstanceStateName expectedState) {

    DescribeInstancesRequest describeRequest =
        DescribeInstancesRequest.builder().instanceIds(List.of(instanceId)).build();

    Ec2Waiter ec2Waiter = getWaiter();

    try {
      switch (expectedState) {
        case RUNNING -> ec2Waiter.waitUntilInstanceRunning(describeRequest);
        case STOPPED -> ec2Waiter.waitUntilInstanceStopped(describeRequest);
        case TERMINATED -> ec2Waiter.waitUntilInstanceTerminated(describeRequest);
        default -> throw new UnsupportedOperationException(
            "Unsupported wait state "
                + expectedState.toString()
                + ", accepts [RUNNING, STOPPED, TERMINATED]");
      }
    } catch (SdkClientException e) {
      EC2Utils.checkWaiterException(instanceId, e);
    }
  }

  /**
   * Add or overwrite one or more tags on an EC2 instance.
   *
   * @param instanceId ID of instance to tag
   * @param tags tags to add/overwrite
   */
  public void createTags(String instanceId, Collection<Tag> tags) {
    getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_CREATE_TAGS_EC2_INSTANCE,
            () ->
                getClient()
                    .createTags(
                        CreateTagsRequest.builder()
                            .resources(List.of(instanceId))
                            .tags(tags)
                            .build()),
            () -> serializeInstanceId(instanceId));
  }

  /**
   * Delete one or more tags from an instance.
   *
   * <p>Note that the AWS DeleteTags API will delete ALL tags if it is called with no tags. To avoid
   * doing this inadvertently we disallow calling this method with an empty list of keys; method
   * {@link EC2InstanceCow#deleteAllTags} can be used for this purpose.
   *
   * @param instanceId ID of instance to delete tags from
   * @param keys of tags to delete
   * @throws {@link IllegalArgumentException} if an empty collection of keys is passed.
   */
  public void deleteTags(String instanceId, Collection<String> keys) {

    if (keys.isEmpty()) {
      throw new IllegalArgumentException(
          "Must pass at least one tag key; to delete all tags call deleteAllTags method.");
    }

    getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_DELETE_TAGS_EC2_INSTANCE,
            () ->
                getClient()
                    .deleteTags(
                        DeleteTagsRequest.builder()
                            .resources(List.of(instanceId))
                            .tags(
                                keys.stream()
                                    .map(key -> Tag.builder().key(key).build())
                                    .collect(Collectors.toList()))
                            .build()),
            () -> serializeInstanceId(instanceId));
  }

  /**
   * Delete all tags from an instance
   *
   * @param instanceId ID of instance to delete tags from
   */
  public void deleteAllTags(String instanceId) {
    getOperationAnnotator()
        .executeCowOperation(
            EC2InstanceOperation.AWS_DELETE_TAGS_EC2_INSTANCE,
            () ->
                getClient()
                    .deleteTags(DeleteTagsRequest.builder().resources(List.of(instanceId)).build()),
            () -> serializeInstanceId(instanceId));
  }

  @VisibleForTesting
  public JsonObject serializeInstanceId(String instanceId) {
    return createJsonObjectWithSingleField("instanceId", instanceId);
  }
}
