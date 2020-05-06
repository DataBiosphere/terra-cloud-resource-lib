package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.util.CloudResourceException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.ResourceId;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** Sample Code of using Google API Service direcrtly instead of using Google Client Library. */
public class GoogleCloudResourceManagerApi {
    private final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.crm.GoogleCloudResourceManager");

    private CloudResourceManager cloudResourceManager;

    public GoogleCloudResourceManagerApi(Credentials credentials) throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        this.cloudResourceManager = new CloudResourceManager.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName("terra")
                .build();
    }

    /**
     *  Creates Google Project.
     *
     * @param projectId The project id to be create
     * @param parentResourceId The parent resource id to create
     */
    public Operation createProjectRaw(String projectId, ResourceId parentResourceId) throws CloudResourceException {
        try {
            Project requestBody = new Project().setProjectId(projectId).setParent(parentResourceId);
            logger.debug("Creating Google project: projectId = {}, resourceId = {} " + projectId, parentResourceId);
            return cloudResourceManager.projects().create(requestBody).execute();
        } catch(IOException e) {
            logger.error("Failed to create Google project: projectId = {}, resourceId = {} " + projectId, parentResourceId);
            // TODO(yonghao): Make CloudResourceException can also take IOException or all exception if we  want to use
            // this class.
            throw new CloudResourceException("Failed to create Google Project");
        }

    }
}