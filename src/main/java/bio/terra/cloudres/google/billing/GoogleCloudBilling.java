package bio.terra.cloudres.google.billing;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudbilling.Cloudbilling;
import com.google.api.services.cloudbilling.model.ProjectBillingInfo;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleCloudBilling {
    private Cloudbilling cloudBilling;

    public GoogleCloudBilling(Credentials credentials) throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        this.cloudBilling = new Cloudbilling.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName("terra")
                .build();
    }

    public ProjectBillingInfo setBillingRaw(String projectId, String billingAccount) throws IOException {
        ProjectBillingInfo content = new ProjectBillingInfo().setBillingAccountName(billingAccount);

        return cloudBilling.projects().updateBillingInfo("projects/" + projectId, content).execute();
    }
}
