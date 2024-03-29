package bio.terra.cloudres.aws.sts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import java.time.Duration;
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
import software.amazon.awssdk.services.sts.model.Credentials;

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
  public void createCredentialsProviderTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);

    Arn fakeArn = Arn.fromString("arn:this:is:a:fake:value/resource");
    Duration staleTime = Duration.ofMinutes(1);
    String serviceAccountEmail = "my-fake-service-account@gserviceaccount.com";
    String idToken = "real-id-token-trust-me";
    stsCow.createAssumeRoleCredentialsProvider(
        fakeArn,
        SecurityTokenServiceCow.MIN_ROLE_SESSION_TOKEN_DURATION,
        staleTime,
        serviceAccountEmail,
        () -> idToken);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest =
        stsCow.serialize(
            fakeArn,
            SecurityTokenServiceCow.MIN_ROLE_SESSION_TOKEN_DURATION,
            staleTime,
            serviceAccountEmail);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        SecurityTokenServiceOperation.AWS_CREATE_GCP_CREDENTIALS_PROVIDER.toString(),
        json.get("operation").getAsString());
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
    assertEquals(
        SecurityTokenServiceOperation.AWS_ASSUME_ROLE_WITH_WEB_IDENTITY.toString(),
        json.get("operation").getAsString());
  }
}
