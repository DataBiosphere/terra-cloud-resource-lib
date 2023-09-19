package bio.terra.cloudres.aws.compute;

import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupEgressRequest;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Request;

/**
 * Exception thrown when adding a Security Group Rule via {@link
 * EC2SecurityGroupCow#authorizeEgress(AuthorizeSecurityGroupEgressRequest)} or {@link
 * EC2SecurityGroupCow#authorizeIngress(AuthorizeSecurityGroupIngressRequest)} fails due to a
 * duplicate rule already existing.
 */
public class CrlEC2DuplicateSecurityRuleException extends CrlEC2Exception {
  public <RequestT extends Ec2Request> CrlEC2DuplicateSecurityRuleException(
      RequestT request, Throwable cause) {
    super(
        String.format(
            "Duplicate security rule already exists matching request: %s", request.toString()),
        cause);
  }
}
