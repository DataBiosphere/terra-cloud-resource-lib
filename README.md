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

