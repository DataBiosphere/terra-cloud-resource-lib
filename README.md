# Terra Cloud Resource Library

Cloud Resource Library (CRL) wraps cloud API client libraries for Terra services. It enforces unified logging and
allows central changes to how Terra services use Cloud APIs.

TODO add more about library principles and Cloud Object Wrappers.

## Cleanup Mode
Terra manages cloud resources for users. To have confidence in our changes, we need integration test suites that
exercises real cloud resource lifecycle operations. The expected use of tests is that tests will fail sometimes.
Nevertheless, we still want to (eventually) clean up cloud resources created by tests. Enter CRL cleanup mode.

CRL can be configured to run in an integration test cleanup mode. In this mode, when a cloud resource is about to be
created with a Cloud Object Wrapper method, the unique identifier for that cloud resource is persisted to an external
database. The resources tracked for cleanup can be later deleted, if they have not already been deleted. See
[CRL Janitor](https://github.com/DataBiosphere/crl-janitor).

# Development

## Requirements

- Java 17
- [git-secret](https://git-secret.io/installation)
- git config updated via `./minnie-kenny.sh -f`

## Using the Gradle wrapper
Set executable permissions:
```
chmod +x gradlew
```

### Dependencies
We use [Gradle's dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html)
to ensure that builds use the same transitive dependencies, so they're reproducible. This means that
adding or updating a dependency requires telling Gradle to save the change. If you're getting errors
that mention "dependency lock state" after changing a dep, you need to do this step.
```
./gradlew dependencies --write-locks
```

## Testing

### Unit tests
Build and run unit tests:
```
./gradlew build test
```

### Integration Tests
Integration tests are run against cloud providers. Cloud resources required for integration tests are defined in
[terraform-ap-modules/crl-test](https://github.com/broadinstitute/terraform-ap-modules/tree/master/crl-test).

To run them locally, you need account credentials. Today, two json files with the google service account credentials
is expected at `common/src/testFixtures/resources/integration_service_account-admin.json`.
And `common/src/testFixtures/resources/integration_service_account-user.json`

Broad employees can get the credentials from Vault with:
```
./local-dev/render-test-config.sh
```
Then actually run the tests with:
```
./gradlew integrationTest
```

TODO: Make this easier, consider making credentials path configurable.

## Linter
Automatically fix linting issues:
```
./gradlew spotlessApply
```

## Publishing an update

# Quickstart
The current available packages are:  
#### google-bigquery
Wraps [Google Cloud BigQuery API](https://cloud.google.com/bigquery/docs/apis).  
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-bigquery).
#### google-billing
Wraps [Google Billing API](https://cloud.google.com/billing/docs/apis).  
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-billing).
#### google-cloudresourcemanager
Wraps [Google Cloud ResourceManager Client API](https://cloud.google.com/resource-manager/docs/apis).
Targets the Cloud Resource Manager v1 for Projects and Organizations. [github](https://github.com/googleapis/google-api-java-client-services/tree/master/clients/google-api-services-cloudresourcemanager/v1).
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-cloudresourcemanager).
#### google-notebooks
Wraps [Google Cloud AI Notebooks Client API](https://cloud.google.com/ai-platform/notebooks/docs/reference/rest).
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-notebooks).
#### google-serviceusage
Wraps [Google Service Usage API](https://cloud.google.com/service-usage/docs/overview).  
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-serviceusage).
#### google-storage
Wraps [Google Cloud Storage API](https://cloud.google.com/storage/docs/apis).  
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-storage).
#### azure-resourcemanager-compute
Wraps [Azure Compute API](https://docs.microsoft.com/en-us/rest/api/compute/).
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/azure-resourcemanager-compute).
#### azure-resourcemanager-containerinstance
Wraps [Azure Container Instances API](https://docs.microsoft.com/en-us/rest/api/container-instances/).
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/azure-resourcemanager-containerinstance).
#### azure-resourcemanager-relay
Wraps [Azure Relay API](https://docs.microsoft.com/en-us/rest/api/relay/).
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/azure-resourcemanager-relay).
#### cloud-resource-schema
The general schema for how cloud resources are presented in CRL.   
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/cloud-resource-schema).
