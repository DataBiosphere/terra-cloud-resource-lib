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

## Using the Gradle wrapper
Set executable permissions:
```
chmod +x gradlew
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
./render-config.sh
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

## Adding a new package
Cloud API client libraries are wrapped in separate CRL packages to allow clients to only include the libraries that they
use. To add a new CRL package to support a new client library:

1. Create a new top level directory for the package.
2. Within the new directory, following standard Java directory structure, add the desired code under `src`.
3. Within the new directory, add `build.gradle` and `gradle.properties`.
4. Add the gradle project for the new package to the top [`settings.gradle`](settings.gradle).
5. Add the gradle project for the new package to the list of `artifactory` `publications` in the top 
[`build.gradle`](build.gradle)
6. Update the [`Quickstart`](#Quickstart) section.


TODO add instructions/considerations for adding a new Cloud Object Wrapper or cloud resource.

# Quickstart
Cloud API client libraries are wrapped in separate CRL packages. A new version will be available along with CRL [release](.github/workflows/publish.yml).
The current available packages are:  
#### google-bigquery
Wraps [Google Cloud BigQuery API](https://cloud.google.com/bigquery/docs/apis).  
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-bigquery).
#### google-storage
Wraps [Google Cloud Storage API](https://cloud.google.com/storage/docs/apis).  
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/google-storage).

#### cloud-resource-schema
The general schema for how cloud resources are presented in CRL.   
Artifact Repository can be found [here](https://broadinstitute.jfrog.io/broadinstitute/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/bio/terra/cloud-resource-lib/cloud-resource-schema).
