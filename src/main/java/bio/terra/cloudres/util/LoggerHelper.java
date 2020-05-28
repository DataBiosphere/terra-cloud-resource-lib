package bio.terra.cloudres.util;

import bio.terra.cloudres.common.CloudOperation;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import org.slf4j.Logger;

/** Helper class to log events in CRL. */
public class LoggerHelper {
  /** Logs succeed cloud calls. This should be in debug level. */
  public static void logSuccessEvent(
      Logger logger, CloudOperation operation, String clientName, String cloudResource) {
    if (logger.isDebugEnabled()) {
      Map<String, String> jsonMap = new HashMap<>();
      jsonMap.put("operation: ", operation.name());
      jsonMap.put("isSuccess: ", "true");
      jsonMap.put("clientName: ", clientName);
      String logString = JsonConverter.merge(cloudResource, jsonMap);
      logger.debug(logString);
    }
  }

  /** Logs failed cloud calls. This should be in info level. */
  public static void logFailEvent(
      Logger logger, CloudOperation operation, String clientName, OptionalInt errorCode) {
    Map<String, String> jsonMap = new HashMap<>();
    jsonMap.put("operation: ", operation.name());
    jsonMap.put("isSuccess: ", "false");
    jsonMap.put("clientName: ", clientName);
    if (errorCode.isPresent()) {
      jsonMap.put("errorCode: ", String.valueOf(errorCode.getAsInt()));
    }
    String logString = JsonConverter.convert(jsonMap);
    logger.info(logString);
  }

  /** Logs cloud calls. This should be in debug level. */
  public static void logEvent(
          Logger logger, CloudOperation operation, String clientName, String request, String response, OptionalInt errorCode) {
    if (logger.isDebugEnabled()) {
      Map<String, String> jsonMap = new HashMap<>();
      jsonMap.put("operation: ", operation.name());
      jsonMap.put("clientName: ", clientName);
      jsonMap.put("request: ", request);
      jsonMap.put("response: ", response);
      if (errorCode.isPresent()) {
        jsonMap.put("errorCode: ", String.valueOf(errorCode.getAsInt()));
      }
    }
  }
}
