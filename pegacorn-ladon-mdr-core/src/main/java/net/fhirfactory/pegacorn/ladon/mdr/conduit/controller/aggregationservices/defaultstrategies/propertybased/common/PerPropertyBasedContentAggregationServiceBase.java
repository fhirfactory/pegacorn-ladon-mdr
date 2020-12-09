/*
 * Copyright (c) 2020 Mark A. Hunter (ACT Health)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.propertybased.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.common.DefaultResourceContentAggregationServiceBase;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceGradeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitSearchResponseElement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionTypeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;

public abstract class PerPropertyBasedContentAggregationServiceBase extends DefaultResourceContentAggregationServiceBase {

    @Inject
    private PerPropertyMergeHelpers mergeHelpers;

    abstract protected void aggregateIntoBasePropertyByProperty(ResourceSoTConduitActionResponse baseResource, ResourceSoTConduitActionResponse additiveResource);
    abstract protected void aggregateResourceSuperClassByAttribute(ResourceSoTConduitActionResponse baseResponse, ResourceSoTConduitActionResponse additiveResponse);
    abstract protected void aggregateDomainResourceSuperClassByAttribute(ResourceSoTConduitActionResponse baseResponse, ResourceSoTConduitActionResponse additiveResponse);

    protected PerPropertyMergeHelpers getMergeHelpers(){return(mergeHelpers);}
    protected boolean baseHasPrecedence(String propertyName, ResourceSoTConduitActionResponse base, ResourceSoTConduitActionResponse other){
        return(mergeHelpers.baseHasPrecedence(propertyName, base, other));
    }

    //
    //
    // Default Aggregation Methods
    //
    //

    @Override
    public VirtualDBMethodOutcome aggregateCreateResponseSet(List<ResourceSoTConduitActionResponse> responseSet) {
        VirtualDBMethodOutcome outcome = defaultActionOutcomeAggregationService(VirtualDBActionTypeEnum.CREATE, responseSet);
        return(outcome);
    }

    @Override
    public VirtualDBMethodOutcome aggregateGetResponseSet(List<ResourceSoTConduitActionResponse> responseSet) {
        VirtualDBMethodOutcome outcome = defaultActionOutcomeAggregationService(VirtualDBActionTypeEnum.REVIEW, responseSet);
        return(outcome);
    }

    @Override
    public VirtualDBMethodOutcome aggregateUpdateResponseSet(List<ResourceSoTConduitActionResponse> responseSet) {
        VirtualDBMethodOutcome outcome = defaultActionOutcomeAggregationService(VirtualDBActionTypeEnum.UPDATE, responseSet);
        return(outcome);
    }

    @Override
    public VirtualDBMethodOutcome aggregateDeleteResponseSet(List<ResourceSoTConduitActionResponse> responseSet) {
        VirtualDBMethodOutcome outcome = defaultActionOutcomeAggregationService(VirtualDBActionTypeEnum.DELETE, responseSet);
        return(outcome);
    }

    @Override
    public VirtualDBMethodOutcome aggregateSearchResultSet(List<ResourceSoTConduitSearchResponseElement> responseSet) {
        return null;
    }

    //
    //
    // Default Aggregation Methods
    //
    //

    protected VirtualDBMethodOutcome defaultActionOutcomeAggregationService(VirtualDBActionTypeEnum action, List<ResourceSoTConduitActionResponse> outcomeList){
        getLogger().debug(".defaultActionOutcomeAggregationService(): Entry, action --> {}", action);
        if(outcomeList == null){
            VirtualDBMethodOutcome aggregatedOutcome = generateBadAttributeOutcome("defaultCreateActionOutcomeAggregation()", action, "Empty Outcome List!!!");
            getLogger().debug(".defaultActionOutcomeAggregationService(): Exit, outcomeList is null");
            return(aggregatedOutcome);
        }
        if(outcomeList.isEmpty()){
            getLogger().debug(".defaultActionOutcomeAggregationService(): Exit, outcomeList is empty");
            VirtualDBMethodOutcome aggregatedOutcome = generateBadAttributeOutcome("defaultCreateActionOutcomeAggregation()", action, "Empty Outcome List!!!");
        }
        boolean hasFailure = false;
        VirtualDBMethodOutcome failedOutcome = null;
        for(ResourceSoTConduitActionResponse currentOutcome: outcomeList) {
            if(!successfulCompletion(currentOutcome.getStatusEnum())){
                return(currentOutcome);
            }
            boolean emptyResource = true;
            if(currentOutcome.getResponseResourceGrade() == null ) {
                emptyResource = false;
            } else {
                if(currentOutcome.getResponseResourceGrade().equals(ResourceGradeEnum.EMPTY)){
                    emptyResource = true;
                } else {
                    emptyResource = false;
                }
            }
            boolean noResource = !(currentOutcome.hasResource());
            if(emptyResource || noResource){
                return(currentOutcome);
            }
        }
        getLogger().trace(".defaultActionOutcomeAggregationService(): A good outcome is detected");
        // Sort the List in terms of Precendence
        Collections.sort(outcomeList);
        // Now make sure there is an "Authoritative Id" assigned to the Resource
        ResourceSoTConduitActionResponse precendenceResponse = outcomeList.get(0);
        String resourceId = precendenceResponse.getId().getValue();
        for(ResourceSoTConduitActionResponse currentOutcome: outcomeList) {
            if(!baseHasPrecedence("id", precendenceResponse, currentOutcome)){
                resourceId = currentOutcome.getId().getValue();
            }
        }
//        if(resourceId != null) {
//            for (ResourceSoTConduitActionResponse currentOutcome : outcomeList) {
//                Resource currentResource = (Resource) currentOutcome.getResource();
//                currentResource.setId(resourceId);
//            }
//        }
        // If there is only one, return it
        if(outcomeList.size() == 1) {
//            mapIdToIdentifier(precendenceResponse);
            return(precendenceResponse);
        }
        // Otherwise Aggregate the Resource, Property-by-Property
        ArrayList<ResourceSoTConduitActionResponse> otherResponseSet = new ArrayList<>();
        otherResponseSet.addAll(outcomeList);
        otherResponseSet.remove(precendenceResponse);
        for(ResourceSoTConduitActionResponse currentOutcome: otherResponseSet){
//            mapIdToIdentifier(currentOutcome);
            aggregateResourceSuperClassByAttribute(precendenceResponse, currentOutcome);
            aggregateDomainResourceSuperClassByAttribute(precendenceResponse, currentOutcome);
            aggregateIntoBasePropertyByProperty(precendenceResponse, currentOutcome);
        }
        return(precendenceResponse);
    }
}
