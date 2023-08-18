package com.microsoft.samples.agreements;

/**
 * Encapsulates the operations on the agreement metadata collection.
 *
 * @deprecated This one is deprecated :(
 */
public interface IAgreementDetailsCollection {
  /**
   * Retrieves all current agreement metadata.
   *
   * @return The current agreement metadata.
   */
  ResourceCollection<AgreementMetaData> get();
}
