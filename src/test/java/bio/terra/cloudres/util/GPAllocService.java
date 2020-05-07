package bio.terra.cloudres.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

public class GPAllocService {
  Config conf = ConfigFactory.load();

  private final HttpClient client;
  private final ObjectMapper mapper;
  // todo: read this from config to get projects in test vs quality orgs
  private final String baseUrl = conf.getConfig("test").getString("gpallocBaseUrl");

  public GPAllocService(String token) {
    this.mapper = new ObjectMapper();

    ArrayList<Header> headers = new ArrayList<>();
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
