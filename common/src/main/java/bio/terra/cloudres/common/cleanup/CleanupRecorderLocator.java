package bio.terra.cloudres.common.cleanup;

/** Locates what {@link CleanupRecorder} instance should be used. */
public class CleanupRecorderLocator {
  private CleanupRecorderLocator() {}

  // TODO: Set a real CleanupRecorder from configuration.
  private static CleanupRecorder instance = new NullCleanupRecorder();

  /** Returns the CleanupRecorder to be used. */
  public static CleanupRecorder get() {
    return instance;
  }

  /** Provide a new CleanupRecorder to be returned. */
  public static void provide(CleanupRecorder recorder) {
    instance = recorder;
  }
}
