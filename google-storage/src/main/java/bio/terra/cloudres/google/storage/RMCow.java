package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class RMCow {
    private final Logger logger = LoggerFactory.getLogger(ResourceManagerCow.class);

    private final ClientConfig clientConfig;
    private final OperationAnnotator operationAnnotator;
    private final CloudResourceManager cloudResourceManager;

    public ResourceManagerCow(ClientConfig clientConfig, GoogleCredentials credentials) throws IOException, GeneralSecurityException {
        this.clientConfig = clientConfig;
        this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
        this.cloudResourceManager = createCloudResourceManagerService(clientConfig, credentials);
    }

    public ProjectCow create(Project project) {
        CleanupRecorder.record(SerializeUtils.create(project.getProjectId()), clientConfig);
        return new ProjectCow(clientConfig, operationAnnotator.executeCheckedCowOperation(
                CloudOperation.GOOGLE_CREATE_PROJECT,
                () -> cloudResourceManager.projects().create(project).execute(),
                () -> SerializeUtils.convert(project)));
    }

    public boolean delete(String projectId) throws IOException {
        CleanupRecorder.record(SerializeUtils.create(projectId), clientConfig);
        return operationAnnotator.executeCheckedCowOperation(
                CloudOperation.GOOGLE_CREATE_PROJECT,
                () -> cloudResourceManager.projects().delete(projectId).execute(),
                () -> SerializeUtils.convert("projectId", projectId));
    }

    private CloudResourceManager createCloudResourceManagerService(ClientConfig clientConfig, GoogleCredentials googleCredentials)
            throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new CloudResourceManager.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(googleCredentials))
                .setApplicationName(clientConfig.getClientName())
                .build();
    }
}
