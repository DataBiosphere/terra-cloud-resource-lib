package bio.terra.cloudres.google.workflow;

import com.uber.cadence.workflow.WorkflowMethod;

public interface CreateProjectWorkflow {
  @WorkflowMethod
  void createProject(CreateProjectArguments arguments);
}
