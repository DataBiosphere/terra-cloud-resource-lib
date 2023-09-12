package bio.terra.cloudres.aws.compute;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.gson.JsonObject;
import java.time.Duration;
import org.slf4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

/**
 * Base class to allow reuse of {@link OperationAnnotator}, {@link Ec2Client}, and {@link Ec2Waiter}
 * instance management code among EC2 COW classes.
 */
public class EC2CowBase implements AutoCloseable {
  private static final Duration EC2_WAITER_TIMEOUT_DURATION = Duration.ofSeconds(900);

  private final OperationAnnotator operationAnnotator;
  private final Ec2Client ec2Client;
  private final Ec2Waiter ec2Waiter;

  /** Constructor for test usage, allows mock injections. */
  protected EC2CowBase(
      Logger logger, ClientConfig clientConfig, Ec2Client ec2Client, Ec2Waiter ec2Waiter) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.ec2Client = ec2Client;
    this.ec2Waiter = ec2Waiter;
  }

  /**
   * Private constructor that instantiates an Ec2Waiter with the passed Ec2Client instance. Meant to
   * be chained from protected ctor that builds the Ec2Client.
   */
  private EC2CowBase(Logger logger, ClientConfig clientConfig, Ec2Client ec2Client) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.ec2Client = ec2Client;
    this.ec2Waiter =
        Ec2Waiter.builder()
            .client(ec2Client)
            .overrideConfiguration(
                WaiterOverrideConfiguration.builder()
                    .waitTimeout(EC2_WAITER_TIMEOUT_DURATION)
                    .build())
            .build();
  }

  /**
   * Constructor for use by extending COW classes; builds shared {@link OperationAnnotator}, {@link
   * Ec2Client}, and {@link Ec2Waiter} instances for use by these COW implementations.
   */
  protected EC2CowBase(
      Logger logger,
      ClientConfig clientConfig,
      AwsCredentialsProvider credentialsProvider,
      Region region) {
    this(
        logger,
        clientConfig,
        Ec2Client.builder().region(region).credentialsProvider(credentialsProvider).build());
  }

  /** Getter for {@link OperationAnnotator} to be used by extending COW implementation classes. */
  protected OperationAnnotator getOperationAnnotator() {
    return operationAnnotator;
  }

  /** Getter for {@link Ec2Client} to be used by extending COW implementation classes. */
  protected Ec2Client getClient() {
    return ec2Client;
  }

  /** Getter for {@link Ec2Waiter} to be used by extending COW implementation classes. */
  protected Ec2Waiter getWaiter() {
    return ec2Waiter;
  }

  @Override
  public void close() {
    ec2Client.close();
    ec2Waiter.close();
  }

  protected JsonObject createJsonObjectWithSingleField(String key, Object value) {
    var obj = new JsonObject();
    obj.addProperty(key, value.toString());
    return obj;
  }
}
