package bio.terra.cloudres.azure.landingzones.deployment;

/**
 * Record of a subnet deployed in a landing zone.
 *
 * @param id subnet id.
 * @param name subnet name.
 */
public record DeployedSubnet(String id, String name) {}