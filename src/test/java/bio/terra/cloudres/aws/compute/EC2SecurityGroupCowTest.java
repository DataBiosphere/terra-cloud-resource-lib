package bio.terra.cloudres.aws.compute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupEgressRequest;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

@Tag("unit")
public class EC2SecurityGroupCowTest {
  private EC2SecurityGroupCow cow;
  @Mock Ec2Client mockClient = mock(Ec2Client.class);
  @Mock Ec2Waiter mockWaiter = mock(Ec2Waiter.class);
  @Mock private Logger mockLogger = mock(Logger.class);
  private static final String securityGroupName = UUID.randomUUID().toString();
  private static final String securityGroupDescription = "My Security Group";
  private static final String vpcId = "vpc-33333333333333333";
  private static final String securityGroupId = "sg-44444444444444444";

  private static final SecurityGroup fakeSecurityGroup =
      SecurityGroup.builder()
          .groupId(securityGroupId)
          .vpcId(vpcId)
          .groupName(securityGroupName)
          .description(securityGroupDescription)
          .build();

  @BeforeEach
  public void setupMocks() {
    ClientConfig unitTestConfig =
        ClientConfig.Builder.newBuilder().setClient("EC2SecurityGroupCowTest").build();
    EC2SecurityGroupCow.setLogger(mockLogger);
    cow = new EC2SecurityGroupCow(unitTestConfig, mockClient, mockWaiter);
  }

  private void verifySecurityGroupIdLogging(EC2SecurityGroupOperation operation) {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedId = cow.serializeSecurityGroupId(securityGroupId);
    Assertions.assertEquals(json.getAsJsonObject("requestData"), serializedId);
    Assertions.assertEquals(operation.toString(), json.get("operation").getAsString());
  }

  private void verifyRequestLogging(Object request, EC2SecurityGroupOperation operation) {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = cow.createJsonObjectWithSingleField("request", request);
    Assertions.assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    Assertions.assertEquals(operation.toString(), json.get("operation").getAsString());
  }

  @Test
  public void createTest() {

    CreateSecurityGroupRequest request =
        CreateSecurityGroupRequest.builder()
            .vpcId(vpcId)
            .description(securityGroupDescription)
            .groupName(securityGroupName)
            .build();

    CreateSecurityGroupResponse response =
        CreateSecurityGroupResponse.builder().groupId(securityGroupId).build();

    when(mockClient.createSecurityGroup((CreateSecurityGroupRequest) any())).thenReturn(response);
    assertEquals(response, cow.create(request));
    verifyRequestLogging(request, EC2SecurityGroupOperation.AWS_CREATE_EC2_SECURITY_GROUP);
  }

  @Test
  public void getTest() {

    DescribeSecurityGroupsResponse response =
        DescribeSecurityGroupsResponse.builder().securityGroups(List.of(fakeSecurityGroup)).build();

    assertTrue(response.hasSecurityGroups());
    assertEquals(1, response.securityGroups().size());

    when(mockClient.describeSecurityGroups((DescribeSecurityGroupsRequest) any()))
        .thenReturn(response);
    assertEquals(fakeSecurityGroup, cow.get(securityGroupId));

    ArgumentCaptor<DescribeSecurityGroupsRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeSecurityGroupsRequest.class);
    verify(mockClient).describeSecurityGroups(requestArgumentCaptor.capture());
    DescribeSecurityGroupsRequest capturedRequest = requestArgumentCaptor.getValue();
    Assertions.assertEquals(1, capturedRequest.groupIds().size());
    Assertions.assertEquals(securityGroupId, capturedRequest.groupIds().get(0));
    verifySecurityGroupIdLogging(EC2SecurityGroupOperation.AWS_GET_EC2_SECURITY_GROUP);
  }

  @Test
  public void getByTagTest() {

    String tagName = "ResourceID";
    String tagValue = UUID.randomUUID().toString();

    DescribeSecurityGroupsResponse response =
        DescribeSecurityGroupsResponse.builder().securityGroups(List.of(fakeSecurityGroup)).build();

    when(mockClient.describeSecurityGroups((DescribeSecurityGroupsRequest) any()))
        .thenReturn(response);

    assertEquals(response, cow.getByTag(tagName, tagValue));

    ArgumentCaptor<DescribeSecurityGroupsRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeSecurityGroupsRequest.class);
    verify(mockClient).describeSecurityGroups(requestArgumentCaptor.capture());
    DescribeSecurityGroupsRequest capturedRequest = requestArgumentCaptor.getValue();
    assertFalse(capturedRequest.hasGroupIds());
    assertTrue(capturedRequest.hasFilters());
    Assertions.assertEquals(1, capturedRequest.filters().size());

    Filter filter = capturedRequest.filters().get(0);
    Assertions.assertEquals(String.format("tag:%s", tagName), filter.name());
    assertTrue(filter.hasValues());
    Assertions.assertEquals(1, filter.values().size());
    Assertions.assertEquals(tagValue, filter.values().get(0));

    verifyRequestLogging(
        capturedRequest, EC2SecurityGroupOperation.AWS_GET_BY_TAG_EC2_SECURITY_GROUP);
  }

  @Test
  public void deleteTest() {
    cow.delete(securityGroupId);

    ArgumentCaptor<DeleteSecurityGroupRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(DeleteSecurityGroupRequest.class);
    verify(mockClient).deleteSecurityGroup(requestArgumentCaptor.capture());
    DeleteSecurityGroupRequest capturedRequest = requestArgumentCaptor.getValue();
    Assertions.assertEquals(securityGroupId, capturedRequest.groupId());
    verifySecurityGroupIdLogging(EC2SecurityGroupOperation.AWS_DELETE_EC2_SECURITY_GROUP);
  }

  @Test
  public void deleteDependencyViolationTest() {
    when(mockClient.deleteSecurityGroup((DeleteSecurityGroupRequest) any()))
        .thenThrow(
            Ec2Exception.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorCode("DependencyViolation").build())
                .build());

    assertThrows(CrlEC2DependencyViolationException.class, () -> cow.delete(securityGroupId));
  }

  @Test
  public void deleteRethrowTest() {
    when(mockClient.deleteSecurityGroup((DeleteSecurityGroupRequest) any()))
        .thenThrow(
            Ec2Exception.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorCode("FakeErrorCode").build())
                .build());

    assertThrows(Ec2Exception.class, () -> cow.delete(securityGroupId));
  }

  @Test
  public void authorizeIngressTest() {

    AuthorizeSecurityGroupIngressRequest request =
        AuthorizeSecurityGroupIngressRequest.builder().groupId(securityGroupId).build();
    cow.authorizeIngress(request);

    ArgumentCaptor<AuthorizeSecurityGroupIngressRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(AuthorizeSecurityGroupIngressRequest.class);
    verify(mockClient).authorizeSecurityGroupIngress(requestArgumentCaptor.capture());
    AuthorizeSecurityGroupIngressRequest capturedRequest = requestArgumentCaptor.getValue();
    Assertions.assertEquals(securityGroupId, capturedRequest.groupId());
    verifyRequestLogging(
        request, EC2SecurityGroupOperation.AWS_AUTHORIZE_INGRESS_EC2_SECURITY_GROUP);
  }

  @Test
  public void authorizeIngressDuplicateTest() {
    when(mockClient.authorizeSecurityGroupIngress((AuthorizeSecurityGroupIngressRequest) any()))
        .thenThrow(
            Ec2Exception.builder()
                .awsErrorDetails(
                    AwsErrorDetails.builder().errorCode("InvalidPermission.Duplicate").build())
                .build());

    assertThrows(
        CrlEC2DuplicateSecurityRuleException.class,
        () -> cow.authorizeIngress(AuthorizeSecurityGroupIngressRequest.builder().build()));
  }

  @Test
  public void authorizeIngressRethrowTest() {
    when(mockClient.authorizeSecurityGroupIngress((AuthorizeSecurityGroupIngressRequest) any()))
        .thenThrow(
            Ec2Exception.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorCode("FakeErrorCode").build())
                .build());

    assertThrows(
        Ec2Exception.class,
        () -> cow.authorizeIngress(AuthorizeSecurityGroupIngressRequest.builder().build()));
  }

  @Test
  public void authorizeEgressTest() {
    AuthorizeSecurityGroupEgressRequest request =
        AuthorizeSecurityGroupEgressRequest.builder().groupId(securityGroupId).build();
    cow.authorizeEgress(request);

    ArgumentCaptor<AuthorizeSecurityGroupEgressRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(AuthorizeSecurityGroupEgressRequest.class);
    verify(mockClient).authorizeSecurityGroupEgress(requestArgumentCaptor.capture());
    AuthorizeSecurityGroupEgressRequest capturedRequest = requestArgumentCaptor.getValue();
    Assertions.assertEquals(securityGroupId, capturedRequest.groupId());
    verifyRequestLogging(
        request, EC2SecurityGroupOperation.AWS_AUTHORIZE_EGRESS_EC2_SECURITY_GROUP);
  }

  @Test
  void waitForExistenceTest() {
    WaiterResponse<DescribeSecurityGroupsResponse> waiterResponse = mock(WaiterResponse.class);

    DescribeSecurityGroupsResponse response = mock(DescribeSecurityGroupsResponse.class);

    when(waiterResponse.matched()).thenReturn(ResponseOrException.response(response));
    when(mockWaiter.waitUntilSecurityGroupExists((DescribeSecurityGroupsRequest) any()))
        .thenReturn(waiterResponse);

    cow.waitForExistence(securityGroupId);

    ArgumentCaptor<DescribeSecurityGroupsRequest> waitRequestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeSecurityGroupsRequest.class);
    verify(mockWaiter).waitUntilSecurityGroupExists(waitRequestArgumentCaptor.capture());
    DescribeSecurityGroupsRequest capturedWaitRequest = waitRequestArgumentCaptor.getValue();
    Assertions.assertEquals(1, capturedWaitRequest.groupIds().size());
    Assertions.assertEquals(securityGroupId, capturedWaitRequest.groupIds().get(0));
  }
}
