package bio.terra.cloudres.aws.sts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityRequest;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.utils.StringUtils;

/**
 * Note: For AWS APIs, we do not significantly modify the API surface, we just decorate with useful
 * features (currently, logging and metric generation). Unlike GCP APIs, these are entirely unit
 * tests that validate CRL behavior but do not call out to live AWS environments. Services should
 * perform connected or integration tests as necessary to validate integration with AWS.
 */
@Tag("unit")
public class SecurityTokenServiceCowTest {

  private SecurityTokenServiceCow stsCow;
  @Mock private StsClient mockStsClient = mock(StsClient.class);
  @Mock private Logger mockLogger = mock(Logger.class);

  @BeforeEach
  public void setupMocks() {
    ClientConfig unitTestConfig =
        ClientConfig.Builder.newBuilder().setClient("SecurityTokenServiceCowTest").build();
    SecurityTokenServiceCow.setLogger(mockLogger);
    stsCow = new SecurityTokenServiceCow(unitTestConfig, mockStsClient);
  }

  @Test
  public void createRefreshRequestTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);

    Arn fakeArn = Arn.fromString("arn:this:is:a:fake:value/resource");
    String serviceAccountEmail = "my-fake-service-account@gserviceaccount.com";
    String idToken = "real-id-token-trust-me";
    stsCow.createRefreshRequest(
        fakeArn,
        SecurityTokenServiceCow.MIN_ROLE_SESSION_TOKEN_DURATION,
        serviceAccountEmail,
        idToken);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest =
        stsCow.serialize(
            fakeArn, SecurityTokenServiceCow.MIN_ROLE_SESSION_TOKEN_DURATION, serviceAccountEmail);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
  }

  @Test
  public void createRefreshRequestTruncatesSessionName() {
    Arn fakeArn = Arn.fromString("arn:this:is:a:fake:value/resource");
    String sessionName =
        StringUtils.repeat("a", SecurityTokenServiceCow.MAX_ROLE_SESSION_NAME_LENGTH * 2);
    String idToken = "real-id-token-trust-me";
    AssumeRoleWithWebIdentityRequest request =
        stsCow.createRefreshRequest(
            fakeArn, SecurityTokenServiceCow.MIN_ROLE_SESSION_TOKEN_DURATION, sessionName, idToken);
    assertEquals(
        SecurityTokenServiceCow.MAX_ROLE_SESSION_NAME_LENGTH, request.roleSessionName().length());
  }

  @Test
  public void assumeRoleTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);

    AssumeRoleRequest request =
        AssumeRoleRequest.builder()
            .roleArn("arn:this:is:a:fake:value/role")
            .durationSeconds(
                (int) SecurityTokenServiceCow.MIN_ROLE_SESSION_TOKEN_DURATION.toSeconds())
            .roleSessionName("mySessionName")
            .build();

    Credentials mockCredentials = mock(Credentials.class);
    AssumeRoleResponse mockResponse = mock(AssumeRoleResponse.class);
    when(mockResponse.credentials()).thenReturn(mockCredentials);
    when(mockStsClient.assumeRole((AssumeRoleRequest) any())).thenReturn(mockResponse);

    stsCow.assumeRole(request);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = stsCow.serialize(request);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
  }
}
