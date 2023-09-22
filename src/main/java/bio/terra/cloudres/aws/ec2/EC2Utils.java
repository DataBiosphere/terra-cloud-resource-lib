package bio.terra.cloudres.aws.ec2;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import org.slf4j.Logger;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;

/**
 * Static utility functions for frequently used error checking and result parsing when using the AWS
 * EC2 API's.
 */
public class EC2Utils {

  /**
   * Log and surface errors returned via {@link ResponseOrException} to CRL clients.
   *
   * @param responseOrException response/error union returned by AWS API call
   * @param logger logger to log exception info to
   * @param message message to log if an exception is present
   * @param <ResponseT> response type wrapped by the {@link ResponseOrException}
   */
  public static <ResponseT> void checkResponseOrException(
      ResponseOrException<ResponseT> responseOrException, Logger logger, String message) {
    if (responseOrException.exception().isPresent()) {
      // Log and surface any errors from AWS, clients should handle these appropriately.
      var t = responseOrException.exception().get();
      logger.error(message, t);
      throw new CrlEC2Exception(message, t);
    }
  }

  /**
   * Several EC2 API calls return a {@link List} of values in responses to requests related to
   * single resources. This results in lots of repeated code to check both a "has" value indicating
   * that a value was returned, as well as confirming that the size of the returned array is exactly
   * one.
   *
   * <p>
   *
   * <p>This method provides a repeatable mechanism for validating that one and only one value of a
   * given type is returned in a response. It requires passing the response type, as well as {@link
   * Function} representing the "has a" presence check as well as a getter for the {@link List}
   * being queried (both to be called on the passed response).
   *
   * @param response response object to call the passed "has" and "get" functions on
   * @param hasFunction function to call on the response object to test for existence of a value
   * @param getFunction function to call on the response object to get the list of values
   * @return {@code getFunction.apply(response).get(0)}
   * @param <ResponseT>
   * @param <ValueT>
   * @throws NoSuchElementException if the passed hasFunction returns false when called on the
   *     response object
   * @throws AssertionError if the list returned by calling getFunction on the response object does
   *     not have exactly one item
   */
  public static <ResponseT, ValueT> ValueT extractSingleValue(
      ResponseT response,
      Function<ResponseT, Boolean> hasFunction,
      Function<ResponseT, List<ValueT>> getFunction) {
    if (!hasFunction.apply(response)) {
      throw new NoSuchElementException("No elements returned from describe call.");
    }

    List<ValueT> list = getFunction.apply(response);
    if (list.size() != 1) {
      throw new AssertionError("List should have exactly one element.");
    }

    return list.get(0);
  }
}
