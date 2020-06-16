# terra-cloud-resource-lib

Cloud Resource Library (CRL) wraps cloud API client libraries for Terra services. It enforces unified logging and
allows central changes to how Terra services use Cloud APIs.

TODO add more about library principles and Cloud Object Wrappers.

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
docker run -it --rm -v $HOME:/root broadinstitute/dsde-toolbox:dev \
  vault read -format json secret/dsde/terra/crl-test/default/service-account-admin.json \
  | jq .data > common/src/testFixtures/resources/integration_service_account_admin.json &&#
docker run -it --rm -v $HOME:/root broadinstitute/dsde-toolbox:dev \
  vault read -format json secret/dsde/terra/crl-test/default/service-account-user.json \
  | jq .data > common/src/testFixtures/resources/integration_service_account_user.json
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
