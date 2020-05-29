package bio.terra.cloudres.util;

import bio.terra.cloudres.common.CloudOperation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.opencensus.trace.TraceId;
import java.util.LinkedHashMap;

      Logger logger,
      TraceId traceId,
      CloudOperation operation,
      String clientName,

      logger.debug(jsonObject.toString());
    }
  }
}
