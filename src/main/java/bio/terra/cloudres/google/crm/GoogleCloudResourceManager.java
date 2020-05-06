package bio.terra.cloudres.google.crm;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.api.services.cloudresourcemanager.model.ResourceId;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.SetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** Sample Code of using Google API Service direcrtly instead of using Google Client Library. */
public class GoogleCloudResourceManager {
    private final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.crm.GoogleCloudResourceManager");

    private CloudResourceManager cloudResourceManager;

    public GoogleCloudResourceManager(Credentials credentials) throws IOException, GeneralSecurityException {
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
    public Operation createProjectRaw(String projectId, ResourceId parentResourceId) throws IOException {
        Project requestBody = new Project().setProjectId(projectId).setParent(parentResourceId);
        logger.debug("Creating Google project: projectId = {}, resourceId = {} " + projectId, parentResourceId);
        return cloudResourceManager.projects().create(requestBody).execute();
    }

    /**
     *  Deletes Google Project.
     *
     * @param projectId The project id to be delete
     */
    public void deleteProjectRaw(String projectId) throws IOException {
        cloudResourceManager.projects().delete(projectId).execute();
    }
}