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

To run them locally, you need account credentials. Today, a single json file with the google service account credentials
is expected at `src/test/resources/integration_service_account.json`. 

Broad employees can get the credentials from Vault with:
```
docker run -it --rm -v $HOME:/root broadinstitute/dsde-toolbox:dev \
  vault read -format json secret/dsde/terra/crl-test/default/service-account.json \
| jq .data > src/test/resources/integration_service_account.json
```

TODO: Make this easier, consider making credentials path configurable.

## Linter
Automatically fix linting issues:
```
./gradlew spotlessApply
```
