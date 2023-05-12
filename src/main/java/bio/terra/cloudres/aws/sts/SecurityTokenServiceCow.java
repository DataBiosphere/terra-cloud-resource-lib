package bio.terra.cloudres.aws.sts;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleWithWebIdentityCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityRequest;
import software.amazon.awssdk.services.sts.model.Credentials;

public class SecurityTokenServiceCow implements AutoCloseable {

  public static final int MAX_ROLE_SESSION_NAME_LENGTH = 64;
  public static final Duration MIN_ROLE_SESSION_TOKEN_DURATION = Duration.ofSeconds(900);

  private static Logger logger = LoggerFactory.getLogger(SecurityTokenServiceCow.class);
  private final OperationAnnotator operationAnnotator;
  private final StsClient stsClient;

  @VisibleForTesting
  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  public SecurityTokenServiceCow(ClientConfig clientConfig, StsClient stsClient) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.stsClient = stsClient;
  }

  /** Create a {@link SecurityTokenServiceCow} with some default configurations for convenience. */
  public static SecurityTokenServiceCow create(
      ClientConfig clientConfig, AwsCredentialsProvider awsCredential) {
    StsClient stsClient =
        StsClient.builder().credentialsProvider(awsCredential).region(Region.AWS_GLOBAL).build();
    return new SecurityTokenServiceCow(clientConfig, stsClient);
  }

  /**
   * Create a AwsCredentialsProvider object to cache and refresh AWS credentials for assuming a role
   * via web identity federation.
   *
   * <p>Unlike other operations, this is just a utility for generating a credential provider. It
   * does not perform any actual cloud operations until credentials are requested. This provider
   * will automatically cache and refresh actual credentials as needed However, CRL cannot log each
   * time the refresh request is used, so we just log when the original provider is created.
   *
   * @param roleArn ARN of the role to request credentials for.
   * @param duration Total duration these credentials should be valid for. Note this is not the same
   *     as the duration of a console session created from these credentials - that can be specified
   *     separately at session creation time.
   * @param staleTime Time, relative to token expiration, that a cached token should be considered
   *     stale and refreshed.
   * @param serviceAccountEmail Email of the GCP service account used to assume this role. This is
   *     used for the human-readable role session name.
   * @param idTokenSupplier A function producing an OIDC token for the GCP service account used to
   *     assume this role via web identity federation.
   * @return A request body to assume the specified role via web identity federation. Note this
   *     method does not make any calls to STS.
   */
  public AwsCredentialsProvider createAssumeRoleCredentialsProvider(
      Arn roleArn,
      Duration duration,
      Duration staleTime,
      String serviceAccountEmail,
      Supplier<String> idTokenSupplier) {
    String roleSessionName = getRoleSessionName(serviceAccountEmail);
    return operationAnnotator.executeCowOperation(
        SecurityTokenServiceOperation.AWS_CREATE_GCP_CREDENTIALS_PROVIDER,
        () ->
            StsAssumeRoleWithWebIdentityCredentialsProvider.builder()
                .stsClient(stsClient)
                .staleTime(staleTime)
                .refreshRequest(
                    () -> createRefreshRequest(roleArn, duration, roleSessionName, idTokenSupplier))
                .build(),
        () -> serialize(roleArn, duration, staleTime, roleSessionName));
  }

  /** Build a request body for refreshing credentials to assume an AWS role. */
  private AssumeRoleWithWebIdentityRequest createRefreshRequest(
      Arn roleArn, Duration duration, String roleSessionName, Supplier<String> idTokenSupplier) {
    return AssumeRoleWithWebIdentityRequest.builder()
        .roleArn(roleArn.toString())
        .durationSeconds((int) duration.toSeconds())
        .roleSessionName(roleSessionName)
        .webIdentityToken(idTokenSupplier.get())
        .build();
  }

  /**
   * Call STS to get short-lived credentials for a role.
   *
   * <p>This produces a single, non-refreshable credential for the specified role. This is generally
   * used for WSM to provide user-scoped credentials to callers - for longer-lived service
   * credentials, prefer using the {@code StsAssumeRoleWithWebIdentityCredentialsProvider} with a
   * refresh request from {@link #createAssumeRoleCredentialsProvider(Arn, Duration, Duration,
   * String, Supplier)}, which will automatically cache and refresh credentials to minimize actual
   * calls to STS.
   *
   * @param request The request to make to STS
   * @return A Credentials object containing access and secret keys as well as session token and
   *     expiration time.
   */
  public Credentials assumeRole(AssumeRoleRequest request) {
    return operationAnnotator.executeCheckedCowOperation(
        SecurityTokenServiceOperation.AWS_ASSUME_ROLE_WITH_WEB_IDENTITY,
        () -> stsClient.assumeRole(request).credentials(),
        () -> serialize(request));
  }

  private static String getRoleSessionName(String value) {
    return (value.length() > MAX_ROLE_SESSION_NAME_LENGTH)
        ? value.substring(0, MAX_ROLE_SESSION_NAME_LENGTH)
        : value;
  }

  @VisibleForTesting
  public JsonObject serialize(
      Arn roleArn, Duration duration, Duration staleTime, String serviceAccountEmail) {
    var ser = new JsonObject();
    ser.addProperty("roleArn", roleArn.toString());
    ser.addProperty("duration", duration.toString());
    ser.addProperty("staleTime", staleTime.toString());
    ser.addProperty("serviceAccountEmail", serviceAccountEmail);
    return ser;
  }

  @VisibleForTesting
  public JsonObject serialize(AssumeRoleRequest request) {
    var ser = new JsonObject();
    // Per AssumeRoleRequest.toString() documentation, sensitive data will be redacted from this
    // string using a placeholder value.
    ser.addProperty("request", request.toString());
    return ser;
  }

  @Override
  public void close() throws Exception {
    stsClient.close();
  }
}
