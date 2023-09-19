package bio.terra.cloudres.aws.compute;

import bio.terra.cloudres.common.ClientConfig;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

/**
 * A Cloud Object Wrapper(COW) for AWS Elastic Compute Cloud (EC2) Library ({@link Ec2Client},
 * focusing on operating system VM image (AMI) related API calls.
 *
 * <p>Generally, this should be used inside a try-with-resources block in order to close the
 * underlying Ec2Client properly after use.
 */
public class EC2ImageCow extends EC2CowBase {

  private static final Integer MAX_IMAGES_PER_REQUEST = 1000;

  private static Logger logger = LoggerFactory.getLogger(EC2ImageCow.class);

  @VisibleForTesting
  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  /** Constructor for test usage, allows mock injections. */
  @VisibleForTesting
  public EC2ImageCow(ClientConfig clientConfig, Ec2Client ec2Client, Ec2Waiter ec2Waiter) {
    super(logger, clientConfig, ec2Client, ec2Waiter);
  }

  private EC2ImageCow(
      Logger logger,
      ClientConfig clientConfig,
      AwsCredentialsProvider credentialsProvider,
      Region region) {
    super(logger, clientConfig, credentialsProvider, region);
  }

  /**
   * Factory method to create an instance of {@link EC2ImageCow}.
   *
   * @param clientConfig CRL Cloud Config
   * @param credentialsProvider AWS credentials provider to use when making EC2 calls
   * @param region AWS region to target EC2 calls to
   * @return an instance of class {@link EC2ImageCow}
   */
  public static EC2ImageCow instanceOf(
      ClientConfig clientConfig, AwsCredentialsProvider credentialsProvider, Region region) {
    return new EC2ImageCow(logger, clientConfig, credentialsProvider, region);
  }

  /**
   * Find the latest image in a given release channel that matches the architecture for a given EC2
   * Instance type.
   *
   * @param imageChannel release channel to find an image for
   * @param instanceType instance type that will be launched
   * @return the ID of the AMI image that can be used to launch the AMI described by imageChannel on
   *     an EC2 instance of type instanceType
   * @throws NoSuchElementException if no release image is found in the current region that is
   *     compatible with the passed instance type
   */
  public String getLatestImage(EC2ImageChannel imageChannel, InstanceType instanceType) {

    // Query AWS to get the supported architectures for the given instance type
    List<String> instanceArchitectures = getInstanceTypeArchitectures(instanceType);

    // Set "latestCreateDate" variable to the epoch; we will use this (in conjunction with
    // `latestImageId" to find the most recently created image.

    String latestImageId = null;
    Instant latestCreateDate = Instant.EPOCH;
    String nextToken = null;

    // Query for image descriptions, handling pagination to make sure we iterate all matching
    // instances.  This should continue until a null "next token" value is returned.

    do {
      DescribeImagesResponse describeImagesResponse =
          queryImages(
              List.of(imageChannel.getOwner()),
              List.of(
                  Filter.builder()
                      .name("name")
                      .values(List.of(imageChannel.getFilterName()))
                      .build(),
                  Filter.builder().name("architecture").values(instanceArchitectures).build()),
              MAX_IMAGES_PER_REQUEST,
              nextToken);

      nextToken = describeImagesResponse.nextToken();

      for (Image image : describeImagesResponse.images()) {
        Instant createDate = Instant.parse(image.creationDate());
        if (createDate.isAfter(latestCreateDate)) {
          // This is the latest we've seen ... update "lastCreateDate" and "latestImageId" together.
          latestCreateDate = createDate;
          latestImageId = image.imageId();
        }
      }
    } while (nextToken != null);

    if (latestImageId == null) {

      String error =
          String.format(
              "Could not find AMI image for channel '%s' and instance type %s (architectures: %s).",
              imageChannel.toString(),
              instanceType.toString(),
              String.join(",", instanceArchitectures));

      logger.error(error);
      throw new NoSuchElementException(error);
    }

    return latestImageId;
  }

  private List<String> getInstanceTypeArchitectures(InstanceType instanceType) {
    DescribeInstanceTypesRequest request =
        DescribeInstanceTypesRequest.builder().instanceTypes(List.of(instanceType)).build();

    DescribeInstanceTypesResponse describeInstanceTypesResponse =
        getOperationAnnotator()
            .executeCowOperation(
                EC2ImageOperation.AWS_DESCRIBE_EC2_INSTANCE_TYPES,
                () -> getClient().describeInstanceTypes(request),
                () -> createJsonObjectWithSingleField("request", request));

    InstanceTypeInfo instanceTypeInfo =
        EC2Utils.extractSingleValue(
            describeInstanceTypesResponse,
            DescribeInstanceTypesResponse::hasInstanceTypes,
            DescribeInstanceTypesResponse::instanceTypes);

    return instanceTypeInfo.processorInfo().supportedArchitecturesAsStrings();
  }

  private DescribeImagesResponse queryImages(
      Collection<String> owners, Collection<Filter> filters, Integer maxResults, String nextToken) {

    DescribeImagesRequest request =
        DescribeImagesRequest.builder()
            .maxResults(maxResults)
            .nextToken(nextToken)
            .owners(owners)
            .filters(filters)
            .build();

    return getOperationAnnotator()
        .executeCowOperation(
            EC2ImageOperation.AWS_DESCRIBE_EC2_IMAGES,
            () -> getClient().describeImages(request),
            () -> createJsonObjectWithSingleField("request", request));
  }
}
