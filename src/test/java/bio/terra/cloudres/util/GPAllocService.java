package bio.terra.cloudres.util;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class GPAllocService {
  private final RestTemplate restTemplate;
  private final String baseUrl = "https://gpalloc-dev.dsp-techops.broadinstitute.org"; // todo: read this from config to get projects in test vs quality orgs
  private final String token = ""; // todo: how to get this token for the right user/SA

  public GPAllocService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.defaultHeader("Authorization", String.format("Bearer %s", token)).build();
  }

  public Project getProject() {
    return this.restTemplate.getForObject(baseUrl + "/api/googleproject", Project.class); // todo: this needs to include an auth header for gpalloc to consume
  }

  public void releaseProject(String projectName) {
    this.restTemplate.delete(baseUrl + "/api/googleproject/" + projectName);
  }
}

