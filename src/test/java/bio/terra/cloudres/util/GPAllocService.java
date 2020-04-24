package bio.terra.cloudres.util;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class GPAllocService {
  private final RestTemplate restTemplate;
  private final String baseUrl = "https://gpalloc-dev.dsp-techops.broadinstitute.org"; // todo: read this from config to get projects in test vs quality orgs

  public GPAllocService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  public Project getProject() {
    return this.restTemplate.getForObject(baseUrl + "/api/googleproject", Project.class); // todo: this needs to include an auth header for gpalloc to consume
  }

  public void releaseProject(String projectName) {
    this.restTemplate.delete(String.format("%1/api/%2", baseUrl, projectName));
  }
}

