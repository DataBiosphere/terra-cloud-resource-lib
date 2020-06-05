package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.DatasetInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class SerializeUtilsTest {
  @Test
  public void convertDatasetIdWithOptions() {
    assertEquals(
        "{\"datasetId\":\"123\",\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                "123",
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithOptions() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    assertEquals(
        "{\"datasetId\":{\"datasetId\":{\"dataset\":\""
            + datasetId
            + "\"},\"labels\":{\"userMap\":{}}},\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                DatasetInfo.newBuilder(datasetId).build(),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithDeleteOptions() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    assertEquals(
        "{\"datasetId\":\""
            + datasetId
            + "\",\"datasetDeleteOptions\":\"[{\\\"rpcOption\\\":\\\"DELETE_CONTENTS\\\",\\\"value\\\":true}]\"}",
        SerializeUtils.convert(datasetId, BigQuery.DatasetDeleteOption.deleteContents())
            .toString());
  }
}
