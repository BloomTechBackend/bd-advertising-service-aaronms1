package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;

import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateTypeConverter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A targeting group for an advertisement, required to show if this advertisement should be rendered.
 */
@DynamoDBTable(tableName = "TargetingGroups")
public class TargetingGroup implements Runnable{
    //MARKER: MT2 made this class runnable
    public static final String CONTENT_ID_INDEX = "ContentIdIndex";

    @DynamoDBHashKey(attributeName = "TargetingGroupId")
    private String targetingGroupId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = CONTENT_ID_INDEX, attributeName = "ContentId")
    private String contentId;

    @DynamoDBAttribute(attributeName = "ClickThroughRate")
    private double clickThroughRate;

    @DynamoDBAttribute(attributeName = "TargetingPredicates")
    @DynamoDBTypeConverted(converter = TargetingPredicateTypeConverter.class)
    private List<TargetingPredicate> targetingPredicates;

    /**
     * Creates a TargetingGroup.
     * @param targetingGroupId The ID specifically for this targeting group
     * @param contentId The ID of the content this metadata is tied to.
     * @param clickThroughRate The probability a customer will click on this advertisement.
     * @param targetingPredicates All of the targeting predicates that must be TRUE to show this advertisement.
     */
    public TargetingGroup(String targetingGroupId,
                          String contentId,
                          double clickThroughRate,
                          List<TargetingPredicate> targetingPredicates) {

        this.targetingGroupId = targetingGroupId;
        this.contentId = contentId;
        this.clickThroughRate = clickThroughRate;
        this.targetingPredicates = targetingPredicates;
    }

    /**
     * Creates an empty TargetingGroup.
     */
    public TargetingGroup() {}

    public String getTargetingGroupId() {
        return targetingGroupId;
    }

    public void setTargetingGroupId(String id) {
        this.targetingGroupId = id;
    }

    public String getContentId() {
        return contentId;
    }

    public double getClickThroughRate() {
        return clickThroughRate;
    }

    public List<TargetingPredicate> getTargetingPredicates() {
        return targetingPredicates;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public void setClickThroughRate(double clickThroughRate) {
        this.clickThroughRate = clickThroughRate;
    }

    public void setTargetingPredicates(List<TargetingPredicate> targetingPredicates) {
        this.targetingPredicates = targetingPredicates;
    }
    
    /**
     * Author: ax56 "<a href="https://github.com/aaronms1">...</a>"
     *
     * When an object implementing interface {@code Runnable} is used to create
     * a thread, starting the thread causes the object's {@code run} method to
     * be called in that separately executing thread.
     * <p>
     * The general contract of the method {@code run} is that it may take any
     * action whatsoever.
     * @see Thread#run()
     */
    @Override
    public void run() {
        //MARKER: MT2
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (TargetingPredicate targetingPredicate : targetingPredicates) {
            executorService.execute((Runnable) targetingPredicate);
        }
        executorService.shutdown();
    }
}
