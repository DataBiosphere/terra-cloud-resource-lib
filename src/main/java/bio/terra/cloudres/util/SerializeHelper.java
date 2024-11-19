package bio.terra.cloudres.util;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

/** Utils for serializing {@link com.google.cloud} objects. */
public class SerializeHelper {
  public SerializeHelper() {}

  /**
   * Helper for Gson convertible classes. Should only be used with classes that play nicely with
   * Gson.
   */
  public static <R> JsonObject convertWithGson(R r, Type t) {
    return createGson().toJsonTree(r, t).getAsJsonObject();
  }

  public static Gson createGson() {
    return Converters.registerAll(new GsonBuilder())
        .registerTypeAdapter(
            Duration.class,
            (JsonSerializer<Duration>)
                (src, typeOfSrc, context) -> new JsonPrimitive(src.toMillis()))
        .create();
  }
}
