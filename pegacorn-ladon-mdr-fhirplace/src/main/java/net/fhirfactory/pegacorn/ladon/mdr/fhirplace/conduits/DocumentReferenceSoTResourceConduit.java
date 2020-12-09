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
package net.fhirfactory.pegacorn.ladon.mdr.fhirplace.conduits;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenParam;
import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.DocumentReferenceSoTConduitController;
import net.fhirfactory.pegacorn.ladon.mdr.fhirplace.accessor.FHIRPlaceFoundationDocumentsMDRAccessor;
import net.fhirfactory.pegacorn.ladon.mdr.fhirplace.conduits.common.FHIRPlaceSoTConduitCommon;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.businesskey.VirtualDBKeyManagement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceGradeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitSearchResponseElement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.SoTConduitGradeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.searches.SearchNameEnum;
import net.fhirfactory.pegacorn.platform.restfulapi.PegacornInternalFHIRClientServices;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DocumentReferenceSoTResourceConduit extends FHIRPlaceSoTConduitCommon {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentReferenceSoTResourceConduit.class);

    @Inject
    private DocumentReferenceSoTConduitController conduitSplicer;

    @Inject
    VirtualDBKeyManagement virtualDBKeyResolver;

    @Inject
    private FHIRPlaceFoundationDocumentsMDRAccessor servicesAccessor;

    @Override
    protected Logger getLogger(){
        return(LOG);
    }

    @Override
    protected PegacornInternalFHIRClientServices specifySecureAccessor() {
        return (servicesAccessor);
    }

    @Override
    protected String specifySourceOfTruthEndpointSystemName() {
        return (getPegacornFHIRPlaceMDRComponentNames().getFoundationDocumentsPegacornMDRSubsystem());
    }

    @Override
    protected Identifier getBestIdentifier(MethodOutcome outcome) {
        if(outcome == null){
            return(null);
        }
        Resource containedResource = (Resource)outcome.getResource();
        if(containedResource == null){
            return(null);
        }
        DocumentReference docRef = (DocumentReference)containedResource;
        if(docRef.hasIdentifier()){
            Identifier bestIdentifier = virtualDBKeyResolver.getBestIdentifier(docRef.getIdentifier());
            return(bestIdentifier);
        }
        return(null);
    }

    @Override
    protected void registerWithSoTCConduitController() {
        conduitSplicer.addResourceConduit(this);
    }

    /**
     * This is the CREATE (POST) Function for the DocumentReference Resource --> persisting
     * the content within a FHIRPlace instance.
     *
     * This function, after invoking the default FHIRPlace writer/create function, adds the
     * CREATE_FULL_COVERAGE response - since, within the context of a DocumentReference
     * resource, FHIRPlace is the sole persistence framework (as of Release 1.0.0).
     *
     * @param resourceToCreate The DocumentReference (Resource) to be persisted
     * @return A Response/Outcome of the operation, including a copy of the Resource.
     */
    @Override
    public ResourceSoTConduitActionResponse createResource(Resource resourceToCreate) {
        LOG.debug(".createResource(): Entry, resourceToCreate --> {}", resourceToCreate);
        ResourceSoTConduitActionResponse outcome = standardCreateResource(resourceToCreate);
        outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        outcome.setSoTGrade(SoTConduitGradeEnum.AUTHORITATIVE);
        LOG.debug(".createResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     * This is the REVIEW (GET) Function for the DocumentReference Resource --> retrieving
     * the DocumentReference resource from a FHIRPlace instance.
     *
     * This function, after invoking the default FHIRPlace read/get function, adds the
     * REVIEW_FULL_COVERAGE response - since, within the context of a DocumentReference
     * resource, FHIRPlace is the sole persistence framework (as of Release 1.0.0).
     *
     * @param identifier The identifier of the DocumentReference to be retrieved
     * @return A Response/Outcome of the operation, including a copy of the Resource (if found).
     */
    @Override
    public ResourceSoTConduitActionResponse getResourceViaIdentifier(Identifier identifier) {
        LOG.debug(".reviewResource(): Entry, identifier --> {}", identifier);
        ResourceSoTConduitActionResponse outcome = standardGetResourceViaIdentifier(ResourceType.DocumentReference.toString(), identifier);
        if(outcome.getStatusEnum().equals(VirtualDBActionStatusEnum.REVIEW_FINISH)) {
            outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
            outcome.setSoTGrade(SoTConduitGradeEnum.AUTHORITATIVE);
        }
        LOG.debug(".reviewResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     * This is the REVIEW (GET) Function for the CareTeam Resource --> retrieving
     * the CareTeam resource from a FHIRPlace instance.
     *
     * This function, after invoking the default FHIRPlace read/get function, adds the
     * REVIEW_FULL_COVERAGE response - since, within the context of a CareTeam
     * resource, FHIRPlace is the sole persistence framework (as of Release 1.0.0).
     *
     * @param id The identifier of the CareTeam to be retrieved
     * @return A Response/Outcome of the operation, including a copy of the Resource (if found).
     */
    @Override
    public ResourceSoTConduitActionResponse reviewResource(IdType id) {
        LOG.debug(".readResource(): Entry, identifier --> {}", id);
        ResourceSoTConduitActionResponse outcome = standardReviewResource(DocumentReference.class, id);
        outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        outcome.setSoTGrade(SoTConduitGradeEnum.AUTHORITATIVE);
        LOG.debug(".readResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     * This is the UPDATE (PUT) Function for the DocumentReference Resource --> updating the
     * content within a FHIRPlace instance.
     *
     * This function, after invoking the default FHIRPlace update/put function, adds the
     * UPDATE_FULL_COVERAGE response - since, within the context of a DocumentReference
     * resource, FHIRPlace is the sole persistence framework (as of Release 1.0.0).
     *
     * @param resourceToUpdate The DocumentReference (Resource) to be updated
     * @return A Response/Outcome of the operation, including a copy of the Resource.
     */
    @Override
    public ResourceSoTConduitActionResponse updateResource(Resource resourceToUpdate) {
        LOG.debug(".updateResource(): Entry, resourceToUpdate --> {}", resourceToUpdate);
        ResourceSoTConduitActionResponse outcome = standardUpdateResource(resourceToUpdate);
        outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        outcome.setSoTGrade(SoTConduitGradeEnum.AUTHORITATIVE);
        LOG.debug(".updateResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    @Override
    public ResourceSoTConduitActionResponse deleteResource(Resource resourceToDelete) {
        return null;
    }

    @Override
    public List<ResourceSoTConduitSearchResponseElement> searchSourceOfTruthUsingCriteria(ResourceType resourceType, SearchNameEnum searchName, Map<Property, Serializable> parameterSet) {
        ArrayList<ResourceSoTConduitSearchResponseElement> resourceList = new ArrayList<ResourceSoTConduitSearchResponseElement>();
        if(searchName.equals(SearchNameEnum.DOCUMENT_REFERENCE_DATE_AND_TYPE)) {
            resourceList.add(getDocumentReferenceByTypeAndDate(parameterSet));
        }
        return(resourceList);
    }

    @Override
    public boolean supportiveOfSearch(SearchNameEnum searchName) {
        return false;
    }

    @Override
    public boolean supportsDirectCreateAction(Resource wholeResource) {
        return true;
    }

    @Override
    public boolean supportsDirectUpdateAction(Resource wholeResource) {
        return true;
    }

    @Override
    public boolean supportsDirectDeleteAction(Resource wholeResource) {
        return false;
    }

    //
    // Supported Searches
    //

    private boolean isDocumentReferenceSearchByTypeAndDate(Map<Property, Serializable> parameterSet){
        if(parameterSet.size() != 2){
            return(false);
        }
        boolean isRightSearch = false;
        Set<Property> propertyList = parameterSet.keySet();
        for(Property currentProperty: propertyList){
            if(currentProperty.getName().contentEquals("type")){
                isRightSearch = true;
                break;
            }
        }
        if(!isRightSearch){
            return(false);
        }
        isRightSearch = false;
        for(Property currentProperty: propertyList){
            Serializable currentElement = parameterSet.get(currentProperty);
            if(currentElement instanceof DateRangeParam){
                isRightSearch = true;
                break;
            }
        }
        return(isRightSearch);
    }

    private ResourceSoTConduitSearchResponseElement getDocumentReferenceByTypeAndDate(Map<Property, Serializable> parameterSet){
        boolean hasDocumentReferenceTypeParam = false;
        boolean hasDocumentReferenceCreationDateParam = false;
        TokenParam documentReferenceTypeValue = null;
        Set<Property> propertyList = parameterSet.keySet();
        for (Property currentProperty : propertyList) {
            if (currentProperty.getName().contentEquals("type")) {
                Serializable currentElement = parameterSet.get(currentProperty);
                if(currentElement instanceof TokenParam) {
                    documentReferenceTypeValue = (TokenParam) currentElement;
                    hasDocumentReferenceTypeParam = true;
                    break;
                }
            }
        }
        DateRangeParam dateRangeParam = null;
        for (Property currentProperty : propertyList) {
            if (currentProperty.getName().contentEquals("date")) {
                Serializable currentElement = parameterSet.get(currentProperty);
                if(currentElement instanceof DateRangeParam) {
                    dateRangeParam = (DateRangeParam) currentElement;
                    hasDocumentReferenceCreationDateParam = true;
                    break;
                }
            }
        }
        ResourceSoTConduitSearchResponseElement searchResponse = new ResourceSoTConduitSearchResponseElement();
        if(!(hasDocumentReferenceCreationDateParam && hasDocumentReferenceTypeParam)) {
            //Todo this is empty, needs populating
            return (searchResponse);
        }
        Bundle response = getFHIRPlaceShardClient()
                .search()
                .forResource(DocumentReference.class)
                .where(DocumentReference.DATE.after().millis(dateRangeParam.getLowerBoundAsInstant()))
                .and(DocumentReference.DATE.beforeOrEquals().millis(dateRangeParam.getUpperBoundAsInstant()))
                .and(DocumentReference.TYPE.exactly().systemAndCode(documentReferenceTypeValue.getSystem(), documentReferenceTypeValue.getValue()))
                .returnBundle(Bundle.class)
                .execute();

        if(response == null){
            //Todo this is empty, needs populating
            return(searchResponse);
        }
        if(response.getTotal() == 0){
            //Todo this is empty, needs populating
            return(searchResponse);
        }
        for(Bundle.BundleEntryComponent entry: response.getEntry()){
            Resource currentResource = entry.getResource();
            if(currentResource.getResourceType() == ResourceType.DocumentReference){
                searchResponse.addResource(currentResource);
            }
        }
        searchResponse.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        searchResponse.setSoTConduitGrade(SoTConduitGradeEnum.AUTHORITATIVE);
        return(searchResponse);
    }

/*    private ResourceSoTConduitSearchResponseElement getDocumentReferenceByIdentifier(Map<Property, Serializable> parameterSet) {
        boolean hasDocumentReferenceIdentifierInfo = false;
        TokenParam documentReferenceIdentifierParam = null;
        Set<Property> propertyList = parameterSet.keySet();
        for (Property currentProperty : propertyList) {
            if (currentProperty.getName().contentEquals("identifier")) {
                Serializable currentElement = parameterSet.get(currentProperty);
                if(currentElement instanceof TokenParam) {
                    documentReferenceIdentifierParam = (TokenParam) currentElement;
                    hasDocumentReferenceIdentifierInfo = true;
                    break;
                }
            }
        }
        ResourceSoTConduitSearchResponseElement searchResponse = new ResourceSoTConduitSearchResponseElement();
        if(hasDocumentReferenceIdentifierInfo) {
            String searchURL = "DocumentReference?identifier:of_type=" + documentReferenceIdentifierParam.getSystem() + "|" + documentReferenceIdentifierParam.getValue();
            Bundle response = getFHIRPlaceShardClient()
                    .search()
                    .byUrl(searchURL)
                    .returnBundle(Bundle.class)
                    .execute();
            for(Bundle.BundleEntryComponent entry: response.getEntry()){
                Resource currentResource = entry.getResource();
                if(currentResource.getResourceType() == ResourceType.DocumentReference){
                    searchResponse.addResource(currentResource);
                }
            }
            searchResponse.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
            searchResponse.setSoTConduitGrade(SoTConduitGradeEnum.AUTHORITATIVE);
            return(searchResponse);
        }
        return(searchResponse);
    }

*/


}
