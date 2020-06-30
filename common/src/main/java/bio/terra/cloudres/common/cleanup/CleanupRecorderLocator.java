package bio.terra.cloudres.common.cleanup;

/** Locates what {@link CleanupRecorder} instance should be used. */
public class CleanupRecorderLocator {
  private CleanupRecorderLocator() {}

  // TODO: Set a real CleanupRecorder from configuration.
  private static CleanupRecorder recorder = new NullCleanupRecorder();

  /** Returns the CleanupRecorder to be used. */
  public static CleanupRecorder get() {
    return recorder;
  }

  /** Provide a new CleanupRecorder to be returned. */
  public static void provide(CleanupRecorder recorder) {
    recorder = recorder;
  }
}
