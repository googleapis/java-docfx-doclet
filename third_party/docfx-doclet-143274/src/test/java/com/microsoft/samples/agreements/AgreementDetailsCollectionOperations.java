package com.microsoft.samples.agreements;

import com.microsoft.samples.BasePartnerComponentString;
import com.microsoft.samples.IPartner;

/**
 * Agreement details collection operations implementation class.
 *
 * @deprecated Use {@link AgreementMetaData} instead.
 */
public class AgreementDetailsCollectionOperations extends BasePartnerComponentString
    implements IAgreementDetailsCollection {
  /**
   * Initializes a new instance of the AgreementDetailsCollectionOperations class.
   *
   * @param rootPartnerOperations The root partner operations instance.
   */
  public AgreementDetailsCollectionOperations(IPartner rootPartnerOperations) {
    super(rootPartnerOperations);
  }

  /**
   * Retrieves the agreement details.
   *
   * @return A list of agreement details.
   * @deprecated Some text
   */
  public ResourceCollection<AgreementMetaData> get() {
    return null;
  }
}
