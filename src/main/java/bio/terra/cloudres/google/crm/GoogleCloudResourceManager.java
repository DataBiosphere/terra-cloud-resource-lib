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

    public boolean addPolicyBindingsRaw(String projectId, Map<String, Set<String>> policiesToAdd) throws IOException {
        Policy existingPolicy = cloudResourceManager.projects().getIamPolicy(projectId, null).execute();

        List<Binding> bindings = existingPolicy.getBindings() == null ? new ArrayList<>() : existingPolicy.getBindings();

        boolean updateRequired = false;

        // update any bindings for existing roles
        Set<String> existingRoleBindings = new HashSet<>();
        for (Binding binding : bindings) {
            if (binding.getCondition() != null) continue;

            existingRoleBindings.add(binding.getRole());
            Set<String> newMembers = policiesToAdd.get(binding.getRole());
            if (newMembers != null) {
                Set<String> existingMembers = new HashSet<>(binding.getMembers() == null ? Collections.emptyList() : binding.getMembers());
                updateRequired |= existingMembers.addAll(newMembers);
                binding.setMembers(new ArrayList<>(existingMembers));
            }
        }

        // add any missing bindings
        for(Map.Entry<String, Set<String>> policyToAddEntry : policiesToAdd.entrySet()) {
            String role = policyToAddEntry.getKey();
            Set<String> members = policyToAddEntry.getValue();
            if (!existingRoleBindings.contains(role)) {
                updateRequired = true;
                bindings.add(new Binding().setRole(role).setMembers(new ArrayList<>(members)));
            }
        }

        if (updateRequired) {
            SetIamPolicyRequest policyRequest = new SetIamPolicyRequest().setPolicy(existingPolicy.setBindings(bindings));
            cloudResourceManager.projects().setIamPolicy(projectId, policyRequest).execute();
        }

        return updateRequired;
    }
}