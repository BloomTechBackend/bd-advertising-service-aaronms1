package com.amazon.ata.advertising.service.businesslogic;

import com.amazon.ata.advertising.service.dao.ReadableDao;
import com.amazon.ata.advertising.service.model.*;
import com.amazon.ata.advertising.service.targeting.TargetingEvaluator;
import com.amazon.ata.advertising.service.targeting.TargetingGroup;

import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;

/**
 * This class is responsible for picking the advertisement to be rendered.
 */
public class AdvertisementSelectionLogic {
    
    private static final Logger LOG = LogManager.getLogger(AdvertisementSelectionLogic.class);
    
    private final ReadableDao<String, List<AdvertisementContent>> contentDao;
    private final ReadableDao<String, List<TargetingGroup>>       targetingGroupDao;
    private       Random                                          random = new Random();
    
    /**
     * Constructor for AdvertisementSelectionLogic.
     * @param contentDao        Source of advertising content.
     * @param targetingGroupDao Source of targeting groups for each advertising
     *                          content.
     */
    @Inject
    public AdvertisementSelectionLogic(
      ReadableDao<String, List<AdvertisementContent>> contentDao,
      ReadableDao<String, List<TargetingGroup>> targetingGroupDao) {
        this.contentDao = contentDao;
        this.targetingGroupDao = targetingGroupDao;
    }
    
    /**
     * Setter for Random class.
     * @param random generates random number used to select advertisements.
     */
    public void setRandom(Random random) {
        this.random = random;
    }
    
    /**
     * Author: ax56 "<a href="https://github.com/aaronms1">...</a>"
     * Gets all of the content and metadata for the marketplace and determines
     * which content can be shown.  Returns the eligible content with the
     * highest click through rate.  If no advertisement is available or
     * eligible, returns an EmptyGeneratedAdvertisement.
     * @param customerId    - the customer to generate a custom advertisement
     *                      for
     * @param marketplaceId - the id of the marketplace the advertisement will
     *                      be rendered on
     * @return an advertisement customized for the customer id provided, or an
     *   empty advertisement if one could not be generated.
     */
    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) {
        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
        if (StringUtils.isEmpty(marketplaceId)) {
            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
        } else {
            //MARKER: MT3
            RequestContext                            requestContext        =
              new RequestContext(customerId, marketplaceId);
            TargetingEvaluator                        targetingEvaluator    =
              new TargetingEvaluator(requestContext);
            List<AdvertisementContent>                advertisementContents =
              contentDao.get(marketplaceId);
            Map<TargetingGroup, AdvertisementContent> eligibleAds
              =
              new TreeMap<>(Comparator.comparingDouble(TargetingGroup::getClickThroughRate).reversed());
            for (AdvertisementContent advertisementContent : advertisementContents) {
                List<TargetingGroup>                      targetingGroups    =
                  targetingGroupDao.get(advertisementContent.getContentId());
            for (TargetingGroup targetingGroup : targetingGroups) {
                //test System.out.println("helo");
                        TargetingPredicateResult result =
                          targetingEvaluator.evaluate(targetingGroup);
                        if (result.isTrue()) {
                            eligibleAds.put(targetingGroup,
                                            advertisementContent);
                        }
                }
            }
            if (!eligibleAds.isEmpty()) {
                generatedAdvertisement =
                  new GeneratedAdvertisement(eligibleAds.entrySet()
                                               .iterator().next().getValue());
            }
        }
        return generatedAdvertisement;
    }
}

//           generatedAdvertisement = new GeneratedAdvertisement(contentDao.get(marketplaceId).stream()
//MARKER: MT2
//                    .map(advertisementContent -> targetingGroupDao.get(advertisementContent.getContentId())
//                            .stream()
//                            .sorted(Comparator.comparingDouble(TargetingGroup::getClickThroughRate))
//                            .map(targetingEvaluator::evaluate)
//                            .anyMatch(TargetingPredicateResult::isTrue) ? advertisementContent : null)
//                    .filter(Objects::nonNull)
//                    .findAny()
//                    .get());
//        }
//        return generatedAdvertisement;
//    }
//          generatedAdvertisement = new GeneratedAdvertisement(eligibleAds
//          .entrySet().iterator().next().getValue());
