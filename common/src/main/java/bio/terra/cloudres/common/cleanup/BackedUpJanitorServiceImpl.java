package bio.terra.cloudres.common.cleanup;

import bio.terra.janitor.ApiClient;
import bio.terra.janitor.ApiException;
import bio.terra.janitor.controller.JanitorApi;
import bio.terra.janitor.model.CreateResourceRequestBody;

/** Janitor Service Interface */
public class BackedUpJanitorServiceImpl {
    private final JanitorApi janitorApi;

    public BackedUpJanitorServiceImpl(String accessToken, String janitorBasePath) {
        ApiClient client = new ApiClient();
        client.setAccessToken(accessToken);
        this.janitorApi = new JanitorApi(client.setBasePath(janitorBasePath));
    }

    public void createTrackedResource(CreateResourceRequestBody body) {
        try {
            janitorApi.createResource(body);
        } catch (ApiException apiException) {
            throw new RuntimeException("Failed to create tracked resource in Janitor", apiException);
        }
    }
}
