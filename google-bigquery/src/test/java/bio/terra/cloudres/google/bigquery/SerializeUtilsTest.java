package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.bigquery.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class SerializeUtilsTest {
  private static final DatasetId DATASET_ID = DatasetId.of("datasetId1");
  private static final TableId TABLE_ID = TableId.of(DATASET_ID.getDataset(), "tableId1");

  @Test
  public void convertDatasetIdWithOptions() {
    assertEquals(
        "{\"datasetId\":{\"dataset\":\"datasetId1\"},"
            + "\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},"
            + "{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                DATASET_ID,
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithOptions() {
    assertEquals(
        "{\"datasetInfo\":{\"datasetId\":{\"dataset\":\"datasetId1\"},"
            + "\"labels\":{\"userMap\":{}}},"
            + "\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},"
            + "{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        SerializeUtils.convert(
                DatasetInfo.newBuilder(DATASET_ID).build(),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS),
                BigQuery.DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME))
            .toString());
  }

  @Test
  public void convertDatasetInfoWithDeleteOptions() {
    assertEquals(
        "{\"datasetId\":{\"dataset\":\"datasetId1\"},"
            + "\"datasetDeleteOptions\":[{\"rpcOption\":\"DELETE_CONTENTS\",\"value\":true}]}",
        SerializeUtils.convert(DATASET_ID, BigQuery.DatasetDeleteOption.deleteContents())
            .toString());
  }

  @Test
  public void convertTableInfoWithTableOptions() {
    assertEquals(
        "{\"tableInfo\":{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"},"
            + "\"definition\":{\"type\":{\"constant\":\"TABLE\"},\"location\":\"location\"},"
            + "\"labels\":{\"userMap\":{}}},"
            + "\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(
                TableInfo.newBuilder(
                        TABLE_ID,
                        StandardTableDefinition.newBuilder().setLocation("location").build())
                    .build(),
                BigQuery.TableOption.fields())
            .toString());
  }

  @Test
  public void convertTableId() {
    assertEquals(
        "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"}}",
        SerializeUtils.convert(TABLE_ID).toString());
  }

  @Test
  public void convertTableIdWithTableOptions() {
    assertEquals(
        "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"},"
            + "\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(TABLE_ID, BigQuery.TableOption.fields()).toString());
  }

  @Test
  public void convertDatasetIdWithTableOptions() {
    assertEquals(
        "{\"datasetId\":{\"dataset\":\"datasetId1\"},"
            + "\"tableListOptions\":[{\"rpcOption\":\"MAX_RESULTS\",\"value\":1}]}",
        SerializeUtils.convert(DATASET_ID, BigQuery.TableListOption.pageSize(1)).toString());
  }

  @Test
  public void convertTableIdWithTableDefinitionWithTableOption() {
    assertEquals(
        "{\"tableId\":{\"dataset\":\"datasetId1\",\"table\":\"tableId1\"},"
            + "\"tableDefinition\":{\"type\":{\"constant\":\"TABLE\"},\"location\":\"location\"},"
            + "\"tableOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"type,tableReference\"}]}",
        SerializeUtils.convert(
                TABLE_ID,
                StandardTableDefinition.newBuilder().setLocation("location").build(),
                BigQuery.TableOption.fields())
            .toString());
  }
}
