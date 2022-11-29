package com.amazon.ata.advertising.service.targeting.predicate;

import com.amazon.ata.advertising.service.dao.ReadableDao;
import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.customerservice.CustomerProfile;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

/**
 * Author: ax56 Date: 2022-28-11 "<a href="https://github.com/aaronms1">...</a>
 * <ul>
 *     <li>...</li>{@link UnknownPredicate}</li>
 *     This class is used to evaluate a TargetingPredicate of type UNKNOWN.
 *     Its a copy of the {@link ParentPredicate} class., but with the
 *     {@link TargetingPredicate} changed to UNKNOWN.
 * </ul>
 */
public class UnknownPredicate extends TargetingPredicate {
    
    @Inject
    ReadableDao<String, CustomerProfile> customerProfileDao;
    
    /**
     * Evaluates to true if a customer has an unknown profile in marketplace
     * @param inverse Can force the predicate to evaluate to true only if a customer
     *     does not have an unknown profile in marketplace
     */
    public UnknownPredicate(boolean inverse) { super(inverse); }
    
    /**
     * Evaluates to true if a customer has an unknown profile in marketplace
     */
    public UnknownPredicate() {}
    
    @Override
    TargetingPredicateResult evaluateRecognizedCustomer(RequestContext context) {
        final CustomerProfile profile = customerProfileDao.get(context.getCustomerId());
    
        return profile.isUnknown() == null ? TargetingPredicateResult
            .INDETERMINATE : profile.isUnknown() ? TargetingPredicateResult
            .TRUE : TargetingPredicateResult.FALSE;
    }
    
    @VisibleForTesting
    void setCustomerProfileDao(ReadableDao<String, CustomerProfile> customerProfileDao) {
        this.customerProfileDao = customerProfileDao;
    }
}
