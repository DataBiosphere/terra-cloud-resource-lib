package bio.terra.cloudres.aws.common;

import java.util.Collection;
import java.util.stream.Collectors;

/** Utilities to convert between {@link GenericTag} and service-specific Tag types. */
public class TagUtils {

  // Hide implicit ctor for all-static util class
  private TagUtils() {}

  /**
   * Convert from {@link GenericTag} to {@link software.amazon.awssdk.services.s3.model.Tag}
   *
   * @param genericTag {@link GenericTag}
   * @return {@link software.amazon.awssdk.services.s3.model.Tag}
   */
  public static software.amazon.awssdk.services.s3.model.Tag toS3Tag(GenericTag genericTag) {
    return software.amazon.awssdk.services.s3.model.Tag.builder()
        .key(genericTag.getKey())
        .value(genericTag.getValue())
        .build();
  }

  /**
   * Convert from collection of {@link GenericTag} to a collection of {@link
   * software.amazon.awssdk.services.s3.model.Tag}
   *
   * @param tags collection of {@link GenericTag}
   * @return collection of {@link software.amazon.awssdk.services.s3.model.Tag}
   */
  public static Collection<software.amazon.awssdk.services.s3.model.Tag> toS3Tags(
      Collection<GenericTag> tags) {
    return tags.stream().map(tag -> toS3Tag(tag)).collect(Collectors.toList());
  }

  /**
   * Convert from {@link software.amazon.awssdk.services.s3.model.Tag} to {@link GenericTag}
   *
   * @param tag {@link software.amazon.awssdk.services.s3.model.Tag}
   * @return {@link GenericTag}
   */
  public static GenericTag fromS3Tag(software.amazon.awssdk.services.s3.model.Tag tag) {
    return GenericTag.builder().key(tag.key()).value(tag.value()).build();
  }

  /**
   * Convert from collection of {@link software.amazon.awssdk.services.s3.model.Tag} to a collection
   * of {@link GenericTag}
   *
   * @param tags collection of {@link software.amazon.awssdk.services.s3.model.Tag}
   * @return collection of {@link GenericTag}
   */
  public static Collection<GenericTag> fromS3Tags(
      Collection<software.amazon.awssdk.services.s3.model.Tag> tags) {
    return tags.stream().map(tag -> fromS3Tag(tag)).collect(Collectors.toList());
  }

  /**
   * Convert from {@link GenericTag} to {@link software.amazon.awssdk.services.ec2.model.Tag}
   *
   * @param genericTag {@link GenericTag}
   * @return {@link software.amazon.awssdk.services.ec2.model.Tag}
   */
  public static software.amazon.awssdk.services.ec2.model.Tag toEc2Tag(GenericTag genericTag) {
    return software.amazon.awssdk.services.ec2.model.Tag.builder()
        .key(genericTag.getKey())
        .value(genericTag.getValue())
        .build();
  }

  /**
   * Convert from collection of {@link GenericTag} to a collection of {@link
   * software.amazon.awssdk.services.ec2.model.Tag}
   *
   * @param tags collection of {@link GenericTag}
   * @return collection of {@link software.amazon.awssdk.services.ec2.model.Tag}
   */
  public static Collection<software.amazon.awssdk.services.ec2.model.Tag> toEc2Tags(
      Collection<GenericTag> tags) {
    return tags.stream().map(tag -> toEc2Tag(tag)).collect(Collectors.toList());
  }

  /**
   * Convert from {@link software.amazon.awssdk.services.ec2.model.Tag} to {@link GenericTag}
   *
   * @param tag {@link software.amazon.awssdk.services.ec2.model.Tag}
   * @return {@link GenericTag}
   */
  public static GenericTag fromEc2Tag(software.amazon.awssdk.services.ec2.model.Tag tag) {
    return GenericTag.builder().key(tag.key()).value(tag.value()).build();
  }

  /**
   * Convert from collection of {@link software.amazon.awssdk.services.ec2.model.Tag} to a
   * collection of {@link GenericTag}
   *
   * @param tags collection of {@link software.amazon.awssdk.services.ec2.model.Tag}
   * @return collection of {@link GenericTag}
   */
  public static Collection<GenericTag> fromEc2Tags(
      Collection<software.amazon.awssdk.services.ec2.model.Tag> tags) {
    return tags.stream().map(tag -> fromEc2Tag(tag)).collect(Collectors.toList());
  }

  /**
   * Convert from {@link GenericTag} to {@link software.amazon.awssdk.services.sagemaker.model.Tag}
   *
   * @param genericTag {@link GenericTag}
   * @return {@link software.amazon.awssdk.services.sagemaker.model.Tag}
   */
  public static software.amazon.awssdk.services.sagemaker.model.Tag toSageMakerTag(
      GenericTag genericTag) {
    return software.amazon.awssdk.services.sagemaker.model.Tag.builder()
        .key(genericTag.getKey())
        .value(genericTag.getValue())
        .build();
  }

  /**
   * Convert from collection of {@link GenericTag} to a collection of {@link
   * software.amazon.awssdk.services.sagemaker.model.Tag}
   *
   * @param tags collection of {@link GenericTag}
   * @return collection of {@link software.amazon.awssdk.services.sagemaker.model.Tag}
   */
  public static Collection<software.amazon.awssdk.services.sagemaker.model.Tag> toSageMakerTags(
      Collection<GenericTag> tags) {
    return tags.stream().map(tag -> toSageMakerTag(tag)).collect(Collectors.toList());
  }

  /**
   * Convert from {@link software.amazon.awssdk.services.sagemaker.model.Tag} to {@link GenericTag}
   *
   * @param tag {@link software.amazon.awssdk.services.sagemaker.model.Tag}
   * @return {@link GenericTag}
   */
  public static GenericTag fromSageMakerTag(
      software.amazon.awssdk.services.sagemaker.model.Tag tag) {
    return GenericTag.builder().key(tag.key()).value(tag.value()).build();
  }

  /**
   * Convert from collection of {@link software.amazon.awssdk.services.sagemaker.model.Tag} to a
   * collection of {@link GenericTag}
   *
   * @param tags collection of {@link software.amazon.awssdk.services.sagemaker.model.Tag}
   * @return collection of {@link GenericTag}
   */
  public static Collection<GenericTag> fromSageMakerTags(
      Collection<software.amazon.awssdk.services.sagemaker.model.Tag> tags) {
    return tags.stream().map(tag -> fromSageMakerTag(tag)).collect(Collectors.toList());
  }

  /**
   * Convert from {@link GenericTag} to {@link software.amazon.awssdk.services.sts.model.Tag}
   *
   * @param genericTag {@link GenericTag}
   * @return {@link software.amazon.awssdk.services.sts.model.Tag}
   */
  public static software.amazon.awssdk.services.sts.model.Tag toStsTag(GenericTag genericTag) {
    return software.amazon.awssdk.services.sts.model.Tag.builder()
        .key(genericTag.getKey())
        .value(genericTag.getValue())
        .build();
  }

  /**
   * Convert from collection of {@link GenericTag} to a collection of {@link
   * software.amazon.awssdk.services.sts.model.Tag}
   *
   * @param tags collection of {@link GenericTag}
   * @return collection of {@link software.amazon.awssdk.services.sts.model.Tag}
   */
  public static Collection<software.amazon.awssdk.services.sts.model.Tag> toStsTags(
      Collection<GenericTag> tags) {
    return tags.stream().map(tag -> toStsTag(tag)).collect(Collectors.toList());
  }

  /**
   * Convert from {@link software.amazon.awssdk.services.sts.model.Tag} to {@link GenericTag}
   *
   * @param tag {@link software.amazon.awssdk.services.sts.model.Tag}
   * @return {@link GenericTag}
   */
  public static GenericTag fromStsTag(software.amazon.awssdk.services.sts.model.Tag tag) {
    return GenericTag.builder().key(tag.key()).value(tag.value()).build();
  }

  /**
   * Convert from collection of {@link software.amazon.awssdk.services.sts.model.Tag} to a
   * collection of {@link GenericTag}
   *
   * @param tags collection of {@link software.amazon.awssdk.services.sts.model.Tag}
   * @return collection of {@link GenericTag}
   */
  public static Collection<GenericTag> fromStsTags(
      Collection<software.amazon.awssdk.services.sts.model.Tag> tags) {
    return tags.stream().map(tag -> fromStsTag(tag)).collect(Collectors.toList());
  }
}
