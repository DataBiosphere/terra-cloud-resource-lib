package bio.terra.cloudres.google.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import bio.terra.cloudres.util.GPAllocService;
import bio.terra.cloudres.util.Project;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import java.io.FileInputStream;
import java.util.UUID;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class GoogleCloudStorageTest {
  Config conf = ConfigFactory.load();

  @Test
  public void shouldCreateBuckets() throws Exception {
    // todo: how do we get service account credentials in other integration/automation tests
    String saKeyFile = conf.getConfig("test").getString("saKeyFile");
    GoogleCredentials credentials =
        ServiceAccountCredentials.fromStream(new FileInputStream(saKeyFile))
            .createScoped("https://www.googleapis.com/auth/cloud-platform");

    GPAllocService gpAllocService = new GPAllocService();
    GoogleCloudStorage cloudStorageService = new GoogleCloudStorage(credentials);

    Project project = gpAllocService.getProject();
    String projectName =
        project
            .getProjectName(); // todo: now we need to get SA credentials for this project somehow
    String bucketName = String.format("mtalbott-%s", UUID.randomUUID().toString());

    Bucket createdBucket = cloudStorageService.create(BucketInfo.newBuilder(bucketName).build());

    try {
      assertThat(createdBucket.getName(), equalTo(bucketName));
    } finally {
      cloudStorageService.deleteBucketRaw(bucketName);
      gpAllocService.releaseProject(projectName);
    }
  }
}
