package bio.terra.cloudres.util;

import static bio.terra.cloudres.util.MetricsHelper.GENERIC_UNKNOWN_ERROR_CODE;
import static bio.terra.cloudres.util.MetricsHelper.KEY_ERROR;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.cloudres.testing.StubCloudOperation;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.time.Duration;
import java.util.OptionalInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Test for {@link MetricsHelper} */
@Tag("unit")
public class MetricsHelperTest {
  public static final String CLIENT = "TestClient";

  private static final Duration METRICS_COLLECTION_INTERVAL = Duration.ofMillis(10);
  private MetricsHelper metricsHelper;
  private TestMetricExporter testMetricExporter;

  @BeforeEach
  void setup() {
    testMetricExporter = new TestMetricExporter();
    metricsHelper = new MetricsHelper(openTelemetry(testMetricExporter));
  }

  public OpenTelemetry openTelemetry(TestMetricExporter testMetricExporter) {
    var sdkMeterProviderBuilder =
        SdkMeterProvider.builder()
            .registerMetricReader(
                PeriodicMetricReader.builder(testMetricExporter)
                    .setInterval(METRICS_COLLECTION_INTERVAL)
                    .build());

    return OpenTelemetrySdk.builder().setMeterProvider(sdkMeterProviderBuilder.build()).build();
  }

  public MetricData waitForMetrics() {
    await()
        .atMost(METRICS_COLLECTION_INTERVAL.multipliedBy(10))
        .pollInterval(METRICS_COLLECTION_INTERVAL)
        .until(
            () ->
                testMetricExporter.getLastMetrics() != null
                    && testMetricExporter.getLastMetrics().size() == 1);
    return testMetricExporter.getLastMetrics().iterator().next();
  }

  @Test
  public void testRecordApiCount() throws Exception {
    metricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);
    metricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);
    metricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);
    metricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);

    var metric = waitForMetrics();

    assertEquals(MetricsHelper.API_COUNT_METER_NAME, metric.getName());
    assertEquals(1, metric.getData().getPoints().size());
    assertEquals(4, ((LongPointData) metric.getData().getPoints().iterator().next()).getValue());
  }

  @Test
  public void testRecordErrorCount() throws Exception {
    metricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(401));
    metricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(401));
    metricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(401));
    metricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(403));
    metricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.empty());

    var metric = waitForMetrics();

    assertEquals(MetricsHelper.ERROR_COUNT_METER_NAME, metric.getName());
    assertEquals(3, metric.getData().getPoints().size());
    assertEquals(
        3,
        ((LongPointData)
                metric.getData().getPoints().stream()
                    .filter(p -> "401".equals(p.getAttributes().get(KEY_ERROR)))
                    .findFirst()
                    .get())
            .getValue());
    assertEquals(
        1,
        ((LongPointData)
                metric.getData().getPoints().stream()
                    .filter(p -> "403".equals(p.getAttributes().get(KEY_ERROR)))
                    .findFirst()
                    .get())
            .getValue());
    assertEquals(
        1,
        ((LongPointData)
                metric.getData().getPoints().stream()
                    .filter(
                        p ->
                            String.valueOf(GENERIC_UNKNOWN_ERROR_CODE)
                                .equals(p.getAttributes().get(KEY_ERROR)))
                    .findFirst()
                    .get())
            .getValue());
  }

  @Test
  public void testRecordLatency() throws Exception {
    metricsHelper.recordLatency(CLIENT, StubCloudOperation.TEST_OPERATION, Duration.ofMillis(1));
    metricsHelper.recordLatency(CLIENT, StubCloudOperation.TEST_OPERATION, Duration.ofMillis(1));
    metricsHelper.recordLatency(CLIENT, StubCloudOperation.TEST_OPERATION, Duration.ofMillis(0));

    var metric = waitForMetrics();

    assertEquals(MetricsHelper.LATENCY_METER_NAME, metric.getName());
    assertEquals(1, metric.getData().getPoints().size());
    var point = (HistogramPointData) metric.getData().getPoints().iterator().next();
    assertEquals(1, point.getCounts().get(0));
    assertEquals(2, point.getCounts().get(1));
  }
}
