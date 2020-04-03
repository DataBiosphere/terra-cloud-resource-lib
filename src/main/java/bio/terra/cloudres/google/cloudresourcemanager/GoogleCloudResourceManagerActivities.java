package bio.terra.cloudres.google.cloudresourcemanager;

import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.ResourceId;

import java.util.Map;
import java.util.Set;

public interface GoogleCloudResourceManagerActivities {
    String createProject(String projectId, ResourceId parent);
    void deleteProject(String projectId);
    Operation getOperation(String operationName);
    boolean addPolicyBindings(String projectId, Map<String, Set<String>> policiesToAdd);
}
