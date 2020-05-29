package bio.terra.cloudres.util;

import bio.terra.cloudres.common.CloudOperation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.opencensus.trace.TraceId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import org.slf4j.Logger;

/** Helper class to log events in CRL. */
public class LoggerHelper {
  /**
   * Logs cloud calls. This should be in debug level.
   *
   * @param logger the logger to log things.
   * @param traceId the traceId where log happens
   * @param operation the operation to log.
   * @param clientName the client name of the operation
   * @param request the request of the log
   * @param response the request of the log
   */
  public static <R, T> void logEvent(
      Logger logger,
      TraceId traceId,
      CloudOperation operation,
      String clientName,
      T request,
      @Nullable R response,
      OptionalInt errorCode) {
    if (logger.isDebugEnabled()) {
      Map<String, String> jsonMap = new LinkedHashMap<>();
      jsonMap.put("traceId:", traceId.toString());
      jsonMap.put("operation:", operation.name());
      jsonMap.put("clientName:", clientName);
      // null if no error code'
      if (errorCode.isPresent()) {
        jsonMap.put("errorCode:", String.valueOf(errorCode.getAsInt()));
      }
      String jsonString = JsonConverter.convert(jsonMap);

      // Now append the already formatted request & response.
      Gson gson = new Gson();
      JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
      JsonConverter.appendFormattedString(jsonObject, "request:", JsonConverter.convert(request));
      JsonConverter.appendFormattedString(jsonObject, "response:", JsonConverter.convert(response));

      logger.debug(jsonObject.toString());
    }
  }
}