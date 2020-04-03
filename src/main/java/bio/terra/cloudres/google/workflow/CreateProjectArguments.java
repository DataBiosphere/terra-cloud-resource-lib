package bio.terra.cloudres.google.workflow;

import com.google.api.services.cloudresourcemanager.model.ResourceId;

import java.util.Map;
import java.util.Set;

public class CreateProjectArguments {
    public final String projectId;
    public final ResourceId parent;
    public final String billingAccount;
    public final Map<String, Set<String>> policies;

    public CreateProjectArguments(String projectId, ResourceId parent, String billingAccount, Map<String, Set<String>> policies) {
        this.parent = parent;
        this.projectId = projectId;
        this.billingAccount = billingAccount;
        this.policies = policies;
    }
}
