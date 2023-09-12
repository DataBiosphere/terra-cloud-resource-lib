package bio.terra.cloudres.aws.compute;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupEgressRequest;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Ec2Request;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

/**
 * A Cloud Object Wrapper(COW) for AWS Elastic Compute Cloud (EC2) Library ({@link Ec2Client},
 * focusing on Security Group-related API calls.
 *
 * <p>Generally, this should be used inside a try-with-resources block in order to close the
 * underlying Ec2Client properly after use.
 */
public class EC2SecurityGroupCow extends EC2CowBase {
  private static Logger logger = LoggerFactory.getLogger(EC2SecurityGroupCow.class);

  @VisibleForTesting
  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  /** Constructor for test usage, allows mock injections. */
  @VisibleForTesting
  public EC2SecurityGroupCow(ClientConfig clientConfig, Ec2Client ec2Client, Ec2Waiter ec2Waiter) {
    super(logger, clientConfig, ec2Client, ec2Waiter);
  }

  private EC2SecurityGroupCow(
      Logger logger,
      ClientConfig clientConfig,
      AwsCredentialsProvider credentialsProvider,
      Region region) {
    super(logger, clientConfig, credentialsProvider, region);
  }

  /**
   * Factory method to create an instance of {@link EC2SecurityGroupCow}
   *
   * @param clientConfig CRL Cloud Config
   * @param credentialsProvider AWS credentials provider to use when making EC2 calls
   * @param region AWS region to target EC2 calls to
   * @return an instance of class {@link EC2SecurityGroupCow}
   */
  public static EC2SecurityGroupCow instanceOf(
      ClientConfig clientConfig, AwsCredentialsProvider credentialsProvider, Region region) {
    return new EC2SecurityGroupCow(logger, clientConfig, credentialsProvider, region);
  }

  /**
   * Create an EC2 Security Group
   *
   * @param request {@link CreateSecurityGroupRequest} describing the Security Group to create
   * @return {@link CreateSecurityGroupResponse} describing the created Security Group
   */
  public CreateSecurityGroupResponse create(CreateSecurityGroupRequest request) {
    return getOperationAnnotator()
        .executeCowOperation(
            EC2SecurityGroupOperation.AWS_CREATE_EC2_SECURITY_GROUP,
            () -> getClient().createSecurityGroup(request),
            () -> createJsonObjectWithSingleField("request", request));
  }

  /**
   * Wait until EC2 group is fully created and can have rules and instances added to it
   *
   * @param securityGroupId ID of Security Group to wait for
   * @throws {@link CrlEC2Exception} if the API call determines that created state can never be
   *     reached
   */
  public void waitForExistence(String securityGroupId) {
    EC2Utils.checkResponseOrException(
        getWaiter()
            .waitUntilSecurityGroupExists(
                DescribeSecurityGroupsRequest.builder().groupIds(List.of(securityGroupId)).build())
            .matched(),
        logger,
        String.format("Error waiting for existence of Security Group with ID %s", securityGroupId));
  }

  /**
   * Gets a description of an EC2 Security Group
   *
   * @param securityGroupId ID of Security Group to describe
   * @return {@link SecurityGroup} object describing the Security Group
   */
  public SecurityGroup get(String securityGroupId) {
    DescribeSecurityGroupsResponse response =
        getOperationAnnotator()
            .executeCowOperation(
                EC2SecurityGroupOperation.AWS_GET_EC2_SECURITY_GROUP,
                () ->
                    getClient()
                        .describeSecurityGroups(
                            DescribeSecurityGroupsRequest.builder()
                                .groupIds(List.of(securityGroupId))
                                .build()),
                () -> serializeSecurityGroupId(securityGroupId));

    return EC2Utils.extractSingleValue(
        response,
        DescribeSecurityGroupsResponse::hasSecurityGroups,
        DescribeSecurityGroupsResponse::securityGroups);
  }

  private <RequestType extends Ec2Request, ReturnValue> void executeSecurityRuleOperation(
      RequestType request,
      CloudOperation cloudOperation,
      OperationAnnotator.CowExecute<ReturnValue> cowExecute,
      OperationAnnotator.CowSerialize cowSerialize) {
    try {
      getOperationAnnotator().executeCowOperation(cloudOperation, cowExecute, cowSerialize);
    } catch (Ec2Exception e) {
      if (e.awsErrorDetails().errorCode().equals("InvalidPermission.Duplicate")) {
        throw new CrlEC2DuplicateSecurityRuleException(request, e);
      }

      // Otherwise just rethrow.
      throw e;
    }
  }

  /**
   * Adds an egress security rule to a security group
   *
   * @param request {@link AuthorizeSecurityGroupEgressRequest} describing egress rule
   * @throws CrlEC2DuplicateSecurityRuleException if a duplicate of the requested egress rule
   *     already exists for the Security Group
   */
  public void authorizeEgress(AuthorizeSecurityGroupEgressRequest request) {

    executeSecurityRuleOperation(
        request,
        EC2SecurityGroupOperation.AWS_AUTHORIZE_EGRESS_EC2_SECURITY_GROUP,
        () -> getClient().authorizeSecurityGroupEgress(request),
        () -> createJsonObjectWithSingleField("request", request));
  }

  /**
   * Adds an ingress security rule to a security group
   *
   * @param request {@link AuthorizeSecurityGroupIngressRequest} describing ingress rule
   * @throws CrlEC2DuplicateSecurityRuleException if a duplicate of the requested ingress rule
   *     already exists for the Security Group
   */
  public void authorizeIngress(AuthorizeSecurityGroupIngressRequest request) {
    executeSecurityRuleOperation(
        request,
        EC2SecurityGroupOperation.AWS_AUTHORIZE_INGRESS_EC2_SECURITY_GROUP,
        () -> getClient().authorizeSecurityGroupIngress(request),
        () -> createJsonObjectWithSingleField("request", request));
  }

  /**
   * Deletes an EC2 Security Group
   *
   * @param securityGroupId ID of Security Group to delete
   * @throws {@link CrlEC2DependencyViolationException} if Security Group is in use by any
   *     non-terminated EC2 Instances
   */
  public void delete(String securityGroupId) {
    try {
      getOperationAnnotator()
          .executeCowOperation(
              EC2SecurityGroupOperation.AWS_DELETE_EC2_SECURITY_GROUP,
              () ->
                  getClient()
                      .deleteSecurityGroup(
                          DeleteSecurityGroupRequest.builder().groupId(securityGroupId).build()),
              () -> serializeSecurityGroupId(securityGroupId));
    } catch (Ec2Exception e) {
      if (e.awsErrorDetails().errorCode().equals("DependencyViolation")) {
        throw new CrlEC2DependencyViolationException(
            EC2SecurityGroupOperation.AWS_DELETE_EC2_SECURITY_GROUP, securityGroupId, e);
      }

      // Otherwise just rethrow.
      throw e;
    }
  }

  @VisibleForTesting
  public JsonObject serializeSecurityGroupId(String instanceId) {
    return createJsonObjectWithSingleField("securityGroupId", instanceId);
  }
}
