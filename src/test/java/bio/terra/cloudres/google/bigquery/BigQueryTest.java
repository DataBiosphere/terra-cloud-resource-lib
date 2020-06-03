package bio.terra.cloudres.google.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetOption;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
public class BigQueryTest {
    private static final DatasetOption DATASET_OPTION_ACCESS = DatasetOption.fields(BigQuery.DatasetField.ACCESS);
    private static final DatasetOption DATASET_OPTION_CREATION_TIME = DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME);

    @Test
    public void convertDatasetIdWithOptions() {

        assertEquals(
                "{\"datasetId\":\"123\",\"options\":\"[DatasetOption{name=fields, value=datasetReference,access}, DatasetOption{name=fields, value=datasetReference,creationTime}]\"}",
                BigQueryCow.convert("123", DATASET_OPTION_ACCESS, DATASET_OPTION_CREATION_TIME));
    }
}
