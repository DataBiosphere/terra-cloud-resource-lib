package bio.terra.cloudres.google.api.services.common;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;

/** Utility class for defaults for CRL working with {@code com.google.api.services} packages. */
public class Defaults {
  private Defaults() {}

  private static HttpTransport defaultTransport;

  private static JsonFactory defaultJsonFactory;

  /** Returns a default {@link HttpTransport} for initializing a services builder. */
  public static HttpTransport httpTransport() throws GeneralSecurityException, IOException {
    if (defaultTransport == null) {
      defaultTransport = GoogleNetHttpTransport.newTrustedTransport();
    }
    return defaultTransport;
  }

  /** Returns a defaul {@link JsonFactory} for initializing a services builder. */
  public static JsonFactory jsonFactory() {
    if (defaultJsonFactory == null) {
      defaultJsonFactory = GsonFactory.getDefaultInstance();
    }
    return defaultJsonFactory;
  }
}
