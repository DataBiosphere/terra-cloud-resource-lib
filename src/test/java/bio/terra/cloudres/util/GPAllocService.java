package bio.terra.cloudres.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;

public class GPAllocService {
  private final HttpClient client;
  private final ObjectMapper mapper;

  // todo: read this from config to get projects in test vs quality orgs
  private final String baseUrl = "https://gpalloc-dev.dsp-techops.broadinstitute.org";

  // todo: how to get this token for the right user/SA
  private final String token = "";

  public GPAllocService() {
    this.mapper = new ObjectMapper();

    ArrayList<Header> headers = new ArrayList<Header>();
    headers.add(new BasicHeader("Authorization", String.format("Bearer %s", token)));
    this.client = HttpClients.custom().setDefaultHeaders(headers).build();
  }

  public Project getProject() throws Exception {
    HttpGet req = new HttpGet(baseUrl + "/api/googleproject");
    HttpResponse response = this.client.execute(req);
    return this.mapper.readValue(response.getEntity().getContent(), Project.class);
  }

  public void releaseProject(String projectName) throws Exception {
    HttpDelete req = new HttpDelete(baseUrl + "/api/googleproject/" + projectName);
    this.client.execute(req);
  }
}
