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
package net.fhirfactory.pegacorn.ladon.mdr.conduit.common;

import net.fhirfactory.pegacorn.ladon.mdr.conduit.aggregationservices.common.ResourceContentAggregationServiceBase;
import net.fhirfactory.pegacorn.ladon.mdr.conduit.aggregationservices.defaultstrategies.propertybased.common.PerPropertyBasedContentAggregationServiceBase;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceGradeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitSearchResponseElement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.SoTResourceConduit;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class ResourceSoTConduitController {
    abstract protected Logger getLogger();

    private HashSet<SoTResourceConduit> conduitSet;
    private ResourceType resourceType;

    public ResourceSoTConduitController(){
        this.conduitSet = new HashSet<>();
        this.resourceType = specifyResourceType();
    }

    public void addResourceConduit(SoTResourceConduit newConduit){
        getLogger().debug(".addResourceConduit(): Entry, newConduit (SoTResourceConduit) --> {}", newConduit);
        if(newConduit == null){
            getLogger().error(".addResourceConduit(): newConduit (SoTResourceConduit) is null");
            return;
        }
        if(conduitSet.contains(newConduit)){
            getLogger().debug(".addResourceConduit(): SoTResourceConduit already exists in set, exiting");
            return;
        }
        getLogger().trace(".addResourceConduit(): Adding SoTResourceConduit to set");
        conduitSet.add(newConduit);
        getLogger().debug(".addResourceConduit(): Exit, SoTResourceConduit added");
    }

    abstract protected ResourceType specifyResourceType();
    abstract protected ResourceContentAggregationServiceBase specifyAggregationService();

    protected ResourceType getResourceType(){
        return(specifyResourceType());
    }

    protected ResourceContentAggregationServiceBase getAggregationService(){
        return(specifyAggregationService());
    }


    //
    // Review / Get Conduit Invocation
    //

    /**
     *
     * @param identifier
     * @return
     */
    protected List<ResourceSoTConduitActionResponse> getResourceFromEachConduit(Identifier identifier){
        getLogger().debug(".getResourceFromEachConduit(): Entry, identifier (Identifier)--> {}", identifier);
        ArrayList<ResourceSoTConduitActionResponse> loadedResources = new ArrayList<ResourceSoTConduitActionResponse>();
        for(SoTResourceConduit currentConduit: conduitSet){
            ResourceSoTConduitActionResponse currentResponse = currentConduit.reviewResource(identifier);
            if(currentResponse.getResponseResourceGrade() != ResourceGradeEnum.EMPTY) {
                loadedResources.add(currentResponse);
            }
        }
        getLogger().debug(".getResourceFromEachConduit(): Exit");
        return(loadedResources);
    }

    //
    // Review / Get Conduit Invocation
    //

    /**
     *
     * @param id
     * @return
     */
    protected List<ResourceSoTConduitActionResponse> getResourceFromEachConduit(IdType id){
        getLogger().debug(".getResourceFromEachConduit(): Entry, id (IdType)--> {}", id);
        ArrayList<ResourceSoTConduitActionResponse> loadedResources = new ArrayList<ResourceSoTConduitActionResponse>();
        for(SoTResourceConduit currentConduit: conduitSet){
            ResourceSoTConduitActionResponse currentResponse = currentConduit.reviewResource(id);
            if(currentResponse.getResponseResourceGrade() != ResourceGradeEnum.EMPTY) {
                loadedResources.add(currentResponse);
            }
        }
        getLogger().debug(".getResourceFromEachConduit(): Exit");
        return(loadedResources);
    }

    //
    // Review / Get Conduit Invocation
    //

    /**
     *
     * @param identifiers
     * @return
     */
    protected List<ResourceSoTConduitActionResponse> getResourceFromEachConduit(List<Identifier> identifiers){
        getLogger().debug(".getResourceFromEachConduit(): Entry, identifiers (List<Identifier>)--> {}", identifiers);
        ArrayList<ResourceSoTConduitActionResponse> loadedResources = new ArrayList<ResourceSoTConduitActionResponse>();
        for(SoTResourceConduit currentConduit: conduitSet){
            for(Identifier identifier: identifiers) {
                ResourceSoTConduitActionResponse currentResponse = currentConduit.reviewResource(identifier);
                if (currentResponse.getResponseResourceGrade() != ResourceGradeEnum.EMPTY) {
                    loadedResources.add(currentResponse);
                }
            }
        }
        getLogger().debug(".getResourceFromEachConduit(): Exit");
        return(loadedResources);
    }

    //
    // Create Conduit Invocation
    //

    /**
     *
     * @param wholeResource
     * @return
     */
    protected List<ResourceSoTConduitActionResponse> createResourceViaEachConduit(Resource wholeResource){
        getLogger().debug(".writeResourceToEachConduit(): Entry, wholeResource --> {}", wholeResource);
        ArrayList<ResourceSoTConduitActionResponse> outcomeSet = new ArrayList<>();
        for(SoTResourceConduit currentConduit: conduitSet) {
            if(currentConduit.supportsDirectCreateAction(wholeResource)) {
                ResourceSoTConduitActionResponse outcome = currentConduit.createResource(wholeResource);
                outcomeSet.add(outcome);
            }
        }
        getLogger().debug(".writeResourceToEachConduit(): Exit");
        return(outcomeSet);
    }

    //
    // Update Conduit Invocation
    //

    /**
     *
     * @param wholeResource
     * @return
     */
    protected List<ResourceSoTConduitActionResponse> updateResourceViaEachConduit(Resource wholeResource){
        getLogger().debug(".writeResourceToEachConduit(): Entry, wholeResource --> {}", wholeResource);
        ArrayList<ResourceSoTConduitActionResponse> outcomeSet = new ArrayList<>();
        for(SoTResourceConduit currentConduit: conduitSet) {
            if(currentConduit.supportsDirectUpdateAction(wholeResource)) {
                ResourceSoTConduitActionResponse outcome = currentConduit.updateResource(wholeResource);
                outcomeSet.add(outcome);
            }
        }
        getLogger().debug(".writeResourceToEachConduit(): Exit");
        return(outcomeSet);
    }

    //
    // Delete Conduit Invocation
    //

    /**
     *
     * @param wholeResource
     * @return
     */
    protected List<ResourceSoTConduitActionResponse> deleteResourceViaEachConduit(Resource wholeResource){
        getLogger().debug(".writeResourceToEachConduit(): Entry, wholeResource --> {}", wholeResource);
        ArrayList<ResourceSoTConduitActionResponse> outcomeSet = new ArrayList<>();
        for(SoTResourceConduit currentConduit: conduitSet) {
            if(currentConduit.supportsDirectDeleteAction(wholeResource)) {
                ResourceSoTConduitActionResponse outcome = currentConduit.updateResource(wholeResource);
                outcomeSet.add(outcome);
            }
        }
        getLogger().debug(".writeResourceToEachConduit(): Exit");
        return(outcomeSet);
    }

    //
    // Searches
    //

    protected List<ResourceSoTConduitSearchResponseElement> attemptResourceSearch(Map<Property, Serializable> parameterSet){
        getLogger().debug(".attemptResourceSearch(): Entry");
        ArrayList<ResourceSoTConduitSearchResponseElement> loadedResources = new ArrayList<ResourceSoTConduitSearchResponseElement>();
        for(SoTResourceConduit currentConduit: conduitSet) {
            List<ResourceSoTConduitSearchResponseElement> currentResponse = currentConduit.getResourcesViaSearchCriteria(getResourceType(), parameterSet);
            loadedResources.addAll(currentResponse);
        }
        getLogger().debug(".attemptResourceSearch(): Exit");
        return(loadedResources);
    }

    protected List<ResourceSoTConduitSearchResponseElement> attemptResourceSearch(Property attributeName, Element attributeValue){
        getLogger().debug(".attemptResourceSearch(): Entry");
        ArrayList<ResourceSoTConduitSearchResponseElement> loadedResources = new ArrayList<ResourceSoTConduitSearchResponseElement>();
        for(SoTResourceConduit currentConduit: conduitSet) {
            List<ResourceSoTConduitSearchResponseElement> currentResponse = currentConduit.getResourcesViaSearchCriteria(getResourceType(), attributeName, attributeValue);
            loadedResources.addAll(currentResponse);
        }
        getLogger().debug(".attemptResourceSearch(): Exit");
        return(loadedResources);
    }

    //
    // Public Methods
    //

    public VirtualDBMethodOutcome createResource(Resource resourceToCreate) {
        List<ResourceSoTConduitActionResponse> methodOutcomes = this.createResourceViaEachConduit(resourceToCreate);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateCreateResponseSet(methodOutcomes);
        return(aggregatedMethodOutcome);
    }

    public VirtualDBMethodOutcome reviewResource(Identifier identifier) {
        List<ResourceSoTConduitActionResponse> methodOutcomes = this.getResourceFromEachConduit(identifier);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateGetResponseSet(methodOutcomes);
        return(aggregatedMethodOutcome);
    }

    public VirtualDBMethodOutcome reviewResource(IdType id) {
        List<ResourceSoTConduitActionResponse> methodOutcomes = this.getResourceFromEachConduit(id);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateGetResponseSet(methodOutcomes);
        return(aggregatedMethodOutcome);
    }

    public VirtualDBMethodOutcome reviewResource(List<Identifier> identifiers) {
        List<ResourceSoTConduitActionResponse> methodOutcomes = this.getResourceFromEachConduit(identifiers);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateGetResponseSet(methodOutcomes);
        return(aggregatedMethodOutcome);
    }

    public VirtualDBMethodOutcome updateResource(Resource resourceToUpdate) {
        List<ResourceSoTConduitActionResponse> methodOutcomes = this.updateResourceViaEachConduit(resourceToUpdate);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateUpdateResponseSet(methodOutcomes);
        return(aggregatedMethodOutcome);
    }

    public VirtualDBMethodOutcome deleteResource(Resource resourceToDelete) {
        List<ResourceSoTConduitActionResponse> methodOutcomes = this.deleteResourceViaEachConduit(resourceToDelete);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateDeleteResponseSet(methodOutcomes);
        return(aggregatedMethodOutcome);
    }

    public VirtualDBMethodOutcome getResourcesViaSearchCriteria(ResourceType resourceType, Property attributeName, Element attributeValue) {
        List<ResourceSoTConduitSearchResponseElement> responseElements = this.attemptResourceSearch(attributeName, attributeValue);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateSearchResultSet(responseElements);
        return(aggregatedMethodOutcome);
    }

    public VirtualDBMethodOutcome getResourcesViaSearchCriteria(ResourceType resourceType, Map<Property, Serializable> parameterSet) {
        List<ResourceSoTConduitSearchResponseElement> responseElements = this.attemptResourceSearch(parameterSet);
        VirtualDBMethodOutcome aggregatedMethodOutcome = getAggregationService().aggregateSearchResultSet(responseElements);
        return(aggregatedMethodOutcome);
    }
}