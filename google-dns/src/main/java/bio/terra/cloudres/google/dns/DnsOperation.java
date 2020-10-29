package bio.terra.cloudres.google.dns;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google DNS API. */
public enum DnsOperation implements CloudOperation {
  GOOGLE_DNS_CREATE_CHANGEE,
  GOOGLE_DNS_CREATE_ZONE,
  GOOGLE_DNS_GET_CHANGE,
  GOOGLE_DNS_GET_ZONE,
  GOOGLE_DNS_LIST_RESOURCE_RECORD_SETS;

  @Override
  public String getName() {
    return this.name();
  }
}
