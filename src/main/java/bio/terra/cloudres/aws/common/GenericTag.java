package bio.terra.cloudres.aws.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Generic Key/Value POJO class that can be used to create service-agnostic sets of tags, which can
 * then be converted to service-specific tag types using the {@link TagUtils} utility class. This
 * makes it possible to write reusable code for generating sets of tags across different AWS service
 * types.
 */
public class GenericTag {
  private final String key;
  private final String value;

  private GenericTag(String key, @Nullable String value) {
    checkNotNull(key);
    this.key = key;
    this.value = value;
  }

  /** Get tag key */
  @Nullable
  public String getKey() {
    return key;
  }

  /** Get tag value */
  public String getValue() {
    return value;
  }

  /** Get a {@link Builder} instance */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GenericTag that = (GenericTag) o;
    return Objects.equals(getKey(), that.getKey()) && Objects.equals(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getKey(), getValue());
  }

  /** Builder for class {@link GenericTag} */
  public static class Builder {
    private String key;
    private String value;

    private Builder() {
      key = null;
      value = null;
    }

    public Builder key(String key) {
      this.key = key;
      return this;
    }

    public Builder value(String value) {
      this.value = value;
      return this;
    }

    public GenericTag build() {
      return new GenericTag(this.key, this.value);
    }
  }
}
