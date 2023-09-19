package bio.terra.cloudres.aws.compute;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

@Tag("unit")
public class EC2InstanceCowTest {
  private EC2InstanceCow cow;
  @Mock Ec2Client mockClient = mock(Ec2Client.class);
  @Mock Ec2Waiter mockWaiter = mock(Ec2Waiter.class);
  @Mock private Logger mockLogger = mock(Logger.class);
  private static final InstanceType instanceType = InstanceType.T3_MEDIUM;
  private static final String instanceAmi = "FakeImageName";
  private static final String instanceId = "i-11111111111111111";

  private static final Instance fakeInstance =
      Instance.builder()
          .instanceId(instanceId)
          .instanceType(instanceType)
          .imageId(instanceAmi)
          .build();

  @BeforeEach
  public void setupMocks() {
    ClientConfig unitTestConfig =
        ClientConfig.Builder.newBuilder().setClient("EC2InstanceCowTest").build();
    EC2InstanceCow.setLogger(mockLogger);
    cow = new EC2InstanceCow(unitTestConfig, mockClient, mockWaiter);
  }

  private void verifyInstanceIdLogging(EC2InstanceOperation operation) {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedId = cow.serializeInstanceId(instanceId);
    assertEquals(json.getAsJsonObject("requestData"), serializedId);
    assertEquals(operation.toString(), json.get("operation").getAsString());
  }

  private void verifyRequestLogging(Object request, EC2InstanceOperation operation) {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = cow.createJsonObjectWithSingleField("request", request);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(operation.toString(), json.get("operation").getAsString());
  }

  @Test
  public void runTest() {

    RunInstancesRequest request =
        RunInstancesRequest.builder().instanceType(instanceType).imageId(instanceAmi).build();

    RunInstancesResponse response =
        RunInstancesResponse.builder().instances(List.of(fakeInstance)).build();

    when(mockClient.runInstances((RunInstancesRequest) any())).thenReturn(response);

    assertEquals(
        fakeInstance,
        EC2Utils.extractSingleValue(
            cow.run(request), RunInstancesResponse::hasInstances, RunInstancesResponse::instances));

    verifyRequestLogging(request, EC2InstanceOperation.AWS_RUN_EC2_INSTANCES);
  }

  @Test
  void getTest() {
    DescribeInstancesResponse response =
        DescribeInstancesResponse.builder()
            .reservations(List.of(Reservation.builder().instances(List.of(fakeInstance)).build()))
            .build();

    when(mockClient.describeInstances((DescribeInstancesRequest) any())).thenReturn(response);
    assertEquals(fakeInstance, cow.get(instanceId));

    ArgumentCaptor<DescribeInstancesRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeInstancesRequest.class);
    verify(mockClient).describeInstances(requestArgumentCaptor.capture());
    DescribeInstancesRequest capturedRequest = requestArgumentCaptor.getValue();
    assertTrue(capturedRequest.hasInstanceIds());
    assertEquals(1, capturedRequest.instanceIds().size());
    assertEquals(instanceId, capturedRequest.instanceIds().get(0));

    verifyInstanceIdLogging(EC2InstanceOperation.AWS_GET_EC2_INSTANCE);
  }

  @Test
  void getByTagTest() {
    String tagName = "ResourceID";
    String tagValue = UUID.randomUUID().toString();

    DescribeInstancesResponse response =
        DescribeInstancesResponse.builder()
            .reservations(List.of(Reservation.builder().instances(List.of(fakeInstance)).build()))
            .build();

    when(mockClient.describeInstances((DescribeInstancesRequest) any())).thenReturn(response);
    assertEquals(response, cow.getByTag(tagName, tagValue));

    ArgumentCaptor<DescribeInstancesRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeInstancesRequest.class);

    verify(mockClient).describeInstances(requestArgumentCaptor.capture());
    DescribeInstancesRequest capturedRequest = requestArgumentCaptor.getValue();
    assertFalse(capturedRequest.hasInstanceIds());
    assertTrue(capturedRequest.hasFilters());
    assertEquals(1, capturedRequest.filters().size());

    Filter filter = capturedRequest.filters().get(0);
    assertEquals(String.format("tag:%s", tagName), filter.name());
    assertTrue(filter.hasValues());
    assertEquals(1, filter.values().size());
    assertEquals(tagValue, filter.values().get(0));

    verifyRequestLogging(capturedRequest, EC2InstanceOperation.AWS_GET_BY_TAG_EC2_INSTANCE);
  }

  private WaiterResponse<DescribeInstancesResponse> prepareWaiterResponse() {
    WaiterResponse<DescribeInstancesResponse> waiterResponse = mock(WaiterResponse.class);

    DescribeInstancesResponse response =
        DescribeInstancesResponse.builder()
            .reservations(List.of(Reservation.builder().instances(List.of(fakeInstance)).build()))
            .build();

    when(waiterResponse.matched()).thenReturn(ResponseOrException.response(response));
    return waiterResponse;
  }

  @Test
  void terminateAndWaitTest() {

    WaiterResponse<DescribeInstancesResponse> waiterResponse = prepareWaiterResponse();
    when(mockWaiter.waitUntilInstanceTerminated((DescribeInstancesRequest) any()))
        .thenReturn(waiterResponse);
    cow.terminateAndWait(instanceId);

    ArgumentCaptor<TerminateInstancesRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(TerminateInstancesRequest.class);
    verify(mockClient).terminateInstances(requestArgumentCaptor.capture());
    TerminateInstancesRequest capturedRequest = requestArgumentCaptor.getValue();
    assertEquals(1, capturedRequest.instanceIds().size());
    assertEquals(instanceId, capturedRequest.instanceIds().get(0));

    ArgumentCaptor<DescribeInstancesRequest> waitRequestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeInstancesRequest.class);
    verify(mockWaiter).waitUntilInstanceTerminated(waitRequestArgumentCaptor.capture());
    DescribeInstancesRequest capturedWaitRequest = waitRequestArgumentCaptor.getValue();
    assertEquals(1, capturedWaitRequest.instanceIds().size());
    assertEquals(instanceId, capturedWaitRequest.instanceIds().get(0));

    verifyInstanceIdLogging(EC2InstanceOperation.AWS_TERMINATE_EC2_INSTANCE);
  }

  @Test
  void startAndWaitTest() {

    WaiterResponse<DescribeInstancesResponse> waiterResponse = prepareWaiterResponse();
    when(mockWaiter.waitUntilInstanceRunning((DescribeInstancesRequest) any()))
        .thenReturn(waiterResponse);
    cow.startAndWait(instanceId);

    ArgumentCaptor<StartInstancesRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(StartInstancesRequest.class);
    verify(mockClient).startInstances(requestArgumentCaptor.capture());
    StartInstancesRequest capturedRequest = requestArgumentCaptor.getValue();
    assertEquals(1, capturedRequest.instanceIds().size());
    assertEquals(instanceId, capturedRequest.instanceIds().get(0));

    ArgumentCaptor<DescribeInstancesRequest> waitRequestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeInstancesRequest.class);
    verify(mockWaiter).waitUntilInstanceRunning(waitRequestArgumentCaptor.capture());
    DescribeInstancesRequest capturedWaitRequest = waitRequestArgumentCaptor.getValue();
    assertEquals(1, capturedWaitRequest.instanceIds().size());
    assertEquals(instanceId, capturedWaitRequest.instanceIds().get(0));

    verifyInstanceIdLogging(EC2InstanceOperation.AWS_START_EC2_INSTANCE);
  }

  @Test
  void stopAndWaitTest() {

    WaiterResponse<DescribeInstancesResponse> waiterResponse = prepareWaiterResponse();
    when(mockWaiter.waitUntilInstanceStopped((DescribeInstancesRequest) any()))
        .thenReturn(waiterResponse);
    cow.stopAndWait(instanceId);

    ArgumentCaptor<StopInstancesRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(StopInstancesRequest.class);
    verify(mockClient).stopInstances(requestArgumentCaptor.capture());
    StopInstancesRequest capturedRequest = requestArgumentCaptor.getValue();
    assertEquals(1, capturedRequest.instanceIds().size());
    assertEquals(instanceId, capturedRequest.instanceIds().get(0));

    ArgumentCaptor<DescribeInstancesRequest> waitRequestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeInstancesRequest.class);
    verify(mockWaiter).waitUntilInstanceStopped(waitRequestArgumentCaptor.capture());
    DescribeInstancesRequest capturedWaitRequest = waitRequestArgumentCaptor.getValue();
    assertEquals(1, capturedWaitRequest.instanceIds().size());
    assertEquals(instanceId, capturedWaitRequest.instanceIds().get(0));

    verifyInstanceIdLogging(EC2InstanceOperation.AWS_STOP_EC2_INSTANCE);
  }

  @Test
  void waitForStatusOKTest() {
    WaiterResponse<DescribeInstanceStatusResponse> waiterResponse = mock(WaiterResponse.class);

    DescribeInstanceStatusResponse response = mock(DescribeInstanceStatusResponse.class);

    when(waiterResponse.matched()).thenReturn(ResponseOrException.response(response));
    when(mockWaiter.waitUntilInstanceStatusOk((DescribeInstanceStatusRequest) any()))
        .thenReturn(waiterResponse);

    cow.waitForStatusOK(instanceId);

    ArgumentCaptor<DescribeInstanceStatusRequest> waitRequestArgumentCaptor =
        ArgumentCaptor.forClass(DescribeInstanceStatusRequest.class);
    verify(mockWaiter).waitUntilInstanceStatusOk(waitRequestArgumentCaptor.capture());
    DescribeInstanceStatusRequest capturedWaitRequest = waitRequestArgumentCaptor.getValue();
    assertEquals(1, capturedWaitRequest.instanceIds().size());
    assertEquals(instanceId, capturedWaitRequest.instanceIds().get(0));
  }

  @Test
  void waitForStateUnsupportedTest() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> cow.waitForState(instanceId, EC2InstanceState.PENDING));
    assertThrows(
        UnsupportedOperationException.class,
        () -> cow.waitForState(instanceId, EC2InstanceState.SHUTTING_DOWN));
  }
}
