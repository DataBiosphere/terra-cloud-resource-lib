package bio.terra.cloudres.aws.ec2;

/**
 * Channels are families of AWS EC2 VM Images (AMI) running a specific operating system version.
 * AMI's are region and architecture specific.
 */
public enum EC2ImageChannel {
  // Flatcar Linux Channels:
  // https://www.flatcar.org/docs/latest/installing/cloud/aws-ec2/#choosing-a-channel
  FLATCAR_LINUX_ALPHA,
  FLATCAR_LINUX_BETA,
  FLATCAR_LINUX_STABLE;

  public String getFilterName() {
    return switch (this) {
        // Flatcar names the images published to their AWS account based on release stream.
      case FLATCAR_LINUX_ALPHA -> "Flatcar-alpha-*";
      case FLATCAR_LINUX_BETA -> "Flatcar-beta-*";
      case FLATCAR_LINUX_STABLE -> "Flatcar-stable-*";
    };
  }

  public String getOwner() {
    return switch (this) {
        // Flatcar linux are published in Kinvolk's AWS account; this can be used as the "owner"
        // when querying images from AWS.
      case FLATCAR_LINUX_ALPHA, FLATCAR_LINUX_BETA, FLATCAR_LINUX_STABLE -> "075585003325";
    };
  }
}
