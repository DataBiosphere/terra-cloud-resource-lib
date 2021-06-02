# Cloud Resource Library Gradle Platform

This project publishes a [Gradle Platform](https://docs.gradle.org/current/userguide/java_platform_plugin.html) which applies a set of version constraints that downstream consumers can use to get an aligned set of versions of CRL libraries.  The consumer specifies a dependency on a version of this platform, and does not need to specify individual CRL library versions.

## Bumping CRL Library Versions
When bumping the version of a CRL library or libraries, the platform should be updated to reflect the new version(s), and the platform version should be bumped accordingly.  Note that if any CRL library versions bump their minor or major version, the platform version should do the same.

## Consuming Downstream
Consumers of CRL libraries who wish to use the platform should declare a dependency on the desired version of the platform.  Dependencies on specific libraries are then declared without versions, and the versions specified in the platform will be used:

```
dependencies {
  ...

  implementation platform('bio.terra.cloud-resource-lib:platform:0.1.0')
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-bigquery'
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-billing'
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-cloudresourcemanager'
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-compute'
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-iam'
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-notebooks'
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-serviceusage'
  implementation group: 'bio.terra.cloud-resource-lib', name: 'google-storage'

  ...
}
```

Note that when declaring a platform as a dependency, this does not imply that all specified CRL libraries are included as dependencies; this just provides constraints on the versions used.  For more info on the difference between constraints and dependencies, see [here](https://docs.gradle.org/current/userguide/java_platform_plugin.html#sec:java_platform_separation).

For more info on consuming Gradle Platforms, see [here](https://docs.gradle.org/current/userguide/java_platform_plugin.html#sec:java_platform_consumption).
