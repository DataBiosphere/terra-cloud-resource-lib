package bio.terra.cloudres.google.serviceusage;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.serviceusage.v1beta1.ServiceUsage;
import com.google.api.services.serviceusage.v1beta1.model.BatchEnableServicesRequest;
import com.google.api.services.serviceusage.v1beta1.model.Operation;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleServiceUsage {
  private ServiceUsage serviceUsage;

  public GoogleServiceUsage(Credentials credentials) throws IOException, GeneralSecurityException {
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    this.serviceUsage =
        new ServiceUsage.Builder(
                httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
            .setApplicationName("terra")
            .build();
  }

  public Operation batchEnableRaw(String projectId, List<String> services) throws IOException {
    return serviceUsage
        .services()
        .batchEnable(
            "projects/" + projectId, new BatchEnableServicesRequest().setServiceIds(services))
        .execute();
  }

  public Operation getOperationRaw(String operationName) throws IOException {
    return serviceUsage.operations().get(operationName).execute();
  }
}
