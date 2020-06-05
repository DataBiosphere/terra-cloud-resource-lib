# terra-cloud-resource-lib

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
is expected at `src/test/resources/integration_service_account-admin.json`. 
And `src/test/resources/integration_service_account-user.json` 

Broad employees can get the credentials from Vault with:
```
docker run -it --rm -v $HOME:/root broadinstitute/dsde-toolbox:dev \
  vault read -format json secret/dsde/terra/crl-test/default/service-account-admin.json \
  | jq .data > src/test/resources/integration_service_account_admin.json &&#
docker run -it --rm -v $HOME:/root broadinstitute/dsde-toolbox:dev \
  vault read -format json secret/dsde/terra/crl-test/default/service-account-user.json \
  | jq .data > src/test/resources/integration_service_account_user.json
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
