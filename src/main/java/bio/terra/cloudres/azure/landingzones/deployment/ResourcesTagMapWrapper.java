package bio.terra.cloudres.azure.landingzones.deployment;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.relay.models.RelayNamespace.DefinitionStages.WithCreate;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ResourcesTagMapWrapper {

    private final Map<Creatable<?>, Map<String, String>> resourcesTagsMap = new HashMap<>();

    private final Map<WithCreate, Map<String, String>> relayResourcesTagsMap = new HashMap<>();
    private final ClientLogger logger = new ClientLogger(ResourcesTagMapWrapper.class);
    private final String landingZoneId;

    ResourcesTagMapWrapper(String landingZoneId) {

        if (StringUtils.isBlank(landingZoneId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Landing Zone ID is invalid. It can't be blank or null"));
        }

        this.landingZoneId = landingZoneId;
    }

    private <T extends Creatable<?> & Resource.DefinitionWithTags<?>> void putTagKeyValue(T resource, String key, String value) {
        Map<String, String> tagMap = resourcesTagsMap.get(resource);
        if (tagMap == null) {
            tagMap = new HashMap<>();
        }

        tagMap.put(key, value);
        resourcesTagsMap.put(resource, tagMap);
    }

    <T extends Creatable<?> & Resource.DefinitionWithTags<?>> void putWithPurpose(T resource, ResourcePurpose purpose) {

        putWithLandingZone(resource);
        putTagKeyValue(resource, LandingZoneTagKeys.LANDING_ZONE_PURPOSE.toString(), purpose.toString());
    }

    void putResource(Creatable<?> resource) {

        resourcesTagsMap.put(resource, null);
    }

    <T extends Creatable<?> & Resource.DefinitionWithTags<?>> void putWithLandingZone(T resource) {

        putTagKeyValue(resource, LandingZoneTagKeys.LANDING_ZONE_ID.toString(), landingZoneId);
    }

    Map<Creatable<?>, Map<String, String>> getResourcesTagsMap() {
        return resourcesTagsMap;
    }

    Map<WithCreate, Map<String, String>> getRelayResourcesTagsMap() {
        return relayResourcesTagsMap;
    }

    Map<String, String> getResourceTagsMap(Creatable<?> resource) {
        return resourcesTagsMap.get(resource);
    }

    void putWithVNetWithPurpose(Network.DefinitionStages.WithCreateAndSubnet virtualNetwork, String subnetName, SubnetResourcePurpose purpose) {
        putWithLandingZone(virtualNetwork);
        Map<String, String> tagMap = resourcesTagsMap.get(virtualNetwork);
        if (tagMap == null) {
            tagMap = new HashMap<>();
        }

        tagMap.put(purpose.toString(), subnetName);
        resourcesTagsMap.put(virtualNetwork, tagMap);
    }

    void putWithPurpose(WithCreate relay, ResourcePurpose purpose) {
        putTagKeyValue(relay, LandingZoneTagKeys.LANDING_ZONE_ID.toString(), landingZoneId);
        putTagKeyValue(relay, LandingZoneTagKeys.LANDING_ZONE_PURPOSE.toString(), purpose.toString());
    }

    private void putTagKeyValue(WithCreate relay, String key, String value) {
        Map<String, String> tagMap = relayResourcesTagsMap.get(relay);
        if (tagMap == null) {
            tagMap = new HashMap<>();
        }

        tagMap.put(key, value);
        relayResourcesTagsMap.put(relay, tagMap);
    }
}
