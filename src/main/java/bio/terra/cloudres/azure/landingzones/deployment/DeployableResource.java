package bio.terra.cloudres.azure.landingzones.deployment;

import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource.DefinitionWithTags;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

public interface DeployableResource extends Creatable, DefinitionWithTags {
}
