package bio.terra.cloudres.aws.ec2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ArchitectureType;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;
import software.amazon.awssdk.services.ec2.model.ProcessorInfo;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

@Tag("unit")
public class EC2ImageCowTest {
  private EC2ImageCow cow;
  @Mock Ec2Client mockClient = mock(Ec2Client.class);
  @Mock Ec2Waiter mockWaiter = mock(Ec2Waiter.class);
  @Mock private Logger mockLogger = mock(Logger.class);
  private final InstanceType instanceType = InstanceType.T3_MEDIUM;
  private final List<ArchitectureType> supportedArchitectures =
      List.of(ArchitectureType.X86_64, ArchitectureType.I386);

  @BeforeEach
  public void setupMocks() {
    ClientConfig unitTestConfig =
        ClientConfig.Builder.newBuilder().setClient("EC2ImageCowTest").build();
    EC2ImageCow.setLogger(mockLogger);
    cow = new EC2ImageCow(unitTestConfig, mockClient, mockWaiter);

    DescribeInstanceTypesResponse archResponse =
        DescribeInstanceTypesResponse.builder()
            .instanceTypes(
                List.of(
                    InstanceTypeInfo.builder()
                        .processorInfo(
                            ProcessorInfo.builder()
                                .supportedArchitectures(supportedArchitectures)
                                .build())
                        .build()))
            .build();

    when(mockClient.describeInstanceTypes((DescribeInstanceTypesRequest) any()))
        .thenReturn(archResponse);
  }

  String imageIdFromOffset(Integer offset) {
    return String.format("Image%02d", offset);
  }

  Image buildImage(Integer offset) {
    return Image.builder()
        .imageId(imageIdFromOffset(offset))
        .creationDate(Instant.EPOCH.plusSeconds(offset).toString())
        .build();
  }

  void expectSuccess(EC2ImageChannel channel) {

    when(mockClient.describeImages((DescribeImagesRequest) any()))
        .thenReturn(
            DescribeImagesResponse.builder()
                .images(List.of(buildImage(0), buildImage(1), buildImage(2), buildImage(6)))
                .nextToken("next")
                .build())
        .thenReturn(
            DescribeImagesResponse.builder()
                .images(List.of(buildImage(5), buildImage(4), buildImage(3)))
                .build());

    assertEquals(imageIdFromOffset(6), cow.getLatestImage(channel, instanceType));
  }

  @Test
  public void getLatestImageTest() {
    for (EC2ImageChannel channel : EC2ImageChannel.values()) {
      expectSuccess(channel);
    }
  }

  @Test
  public void getLatestImageEmptyTest() {
    when(mockClient.describeImages((DescribeImagesRequest) any()))
        .thenReturn(DescribeImagesResponse.builder().images(List.of()).build());

    assertThrows(
        NoSuchElementException.class,
        () -> cow.getLatestImage(EC2ImageChannel.FLATCAR_LINUX_STABLE, instanceType));
  }
}
