package bio.terra.cloudres.common;

/**
 * The Cloud API operations, each cloud API package implements its own supported operations. If we
 * put all cloud operations here, adding new operation will need version upgrade for all packages.
 */
public interface CloudOperation {
  /** Gets the name of operation. */
  String name();
}
