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
import net.fhirfactory.pegacorn.ladon.mdr.conduit.OrganizationSoTConduitController;
import net.fhirfactory.pegacorn.ladon.mdr.fhirplace.accessor.FHIRPlaceBaseEntitiesMDRAccessor;
import net.fhirfactory.pegacorn.ladon.mdr.fhirplace.conduits.common.FHIRPlaceSoTConduitCommon;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.businesskey.VirtualDBKeyManagement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceGradeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitSearchResponseElement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.SoTConduitGradeEnum;
import net.fhirfactory.pegacorn.platform.hapifhir.clients.JPAServerSecureAccessor;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrganizationSoTResourceConduit extends FHIRPlaceSoTConduitCommon {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationSoTResourceConduit.class);

    @Inject
    private OrganizationSoTConduitController conduitController;

    @Inject
    VirtualDBKeyManagement virtualDBKeyResolver;

    @Inject
    private FHIRPlaceBaseEntitiesMDRAccessor fhirPlaceBaseEntitiesMDRAccessor;

    @Override
    protected Logger getLogger(){
        return(LOG);
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
        Organization actualResource = (Organization)containedResource;
        if(actualResource.hasIdentifier()){
            Identifier bestIdentifier = virtualDBKeyResolver.getBestIdentifier(actualResource.getIdentifier());
            return(bestIdentifier);
        }
        return(null);
    }

    @Override
    protected JPAServerSecureAccessor specifyJPAServerSecureAccessor() {
        return (fhirPlaceBaseEntitiesMDRAccessor);
    }

    @Override
    protected void registerWithSoTCConduitController() {
        conduitController.addResourceConduit(this);
    }

    /**
     * This is the CREATE (POST) Function for the Organization Resource --> persisting
     * the content within a FHIRPlace instance.
     *
     * This function, after invoking the default FHIRPlace writer/create function, adds the
     * CREATE_FULL_COVERAGE response - since, within the context of a Organization
     * resource, FHIRPlace is the sole persistence framework (as of Release 1.0.0).
     *
     * @param resourceToCreate The Organization (Resource) to be persisted
     * @return A Response/Outcome of the operation, including a copy of the Resource.
     */
    @Override
    public ResourceSoTConduitActionResponse createResource(Resource resourceToCreate) {
        LOG.debug(".createResource(): Entry, resourceToCreate --> {}", resourceToCreate);
        ResourceSoTConduitActionResponse outcome = standardCreateResource(resourceToCreate);
        outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        outcome.setSoTGrade(SoTConduitGradeEnum.PARTIALLY_AUTHORITATIVE);
        LOG.debug(".createResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     * This is the REVIEW (GET) Function for the Organization Resource --> retrieving
     * the Organization resource from a FHIRPlace instance.
     *
     * This function, after invoking the default FHIRPlace read/get function, adds the
     * REVIEW_FULL_COVERAGE response - since, within the context of a Organization
     * resource, FHIRPlace is the sole persistence framework (as of Release 1.0.0).
     *
     * @param identifier The identifier of the Organization to be retrieved
     * @return A Response/Outcome of the operation, including a copy of the Resource (if found).
     */
    @Override
    public ResourceSoTConduitActionResponse reviewResource(Identifier identifier) {
        LOG.debug(".readResource(): Entry, identifier --> {}", identifier);
        ResourceSoTConduitActionResponse outcome = standardReviewResource(Organization.class, identifier);
        outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        outcome.setSoTGrade(SoTConduitGradeEnum.PARTIALLY_AUTHORITATIVE);
        LOG.debug(".readResource(): Exit, outcome --> {}", outcome);
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
        ResourceSoTConduitActionResponse outcome = standardReviewResource(Organization.class, id);
        outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        outcome.setSoTGrade(SoTConduitGradeEnum.AUTHORITATIVE);
        LOG.debug(".readResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     * This is the UPDATE (PUT) Function for the Organization Resource --> updating the
     * content within a FHIRPlace instance.
     *
     * This function, after invoking the default FHIRPlace update/put function, adds the
     * UPDATE_FULL_COVERAGE response - since, within the context of a Organization
     * resource, FHIRPlace is the sole persistence framework (as of Release 1.0.0).
     *
     * @param resourceToUpdate The Organization (Resource) to be updated
     * @return A Response/Outcome of the operation, including a copy of the Resource.
     */
    @Override
    public ResourceSoTConduitActionResponse updateResource(Resource resourceToUpdate) {
        LOG.debug(".updateResource(): Entry, resourceToUpdate --> {}", resourceToUpdate);
        ResourceSoTConduitActionResponse outcome = standardUpdateResource(resourceToUpdate);
        outcome.setResponseResourceGrade(ResourceGradeEnum.THOROUGH);
        outcome.setSoTGrade(SoTConduitGradeEnum.PARTIALLY_AUTHORITATIVE);
        LOG.debug(".updateResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    @Override
    public ResourceSoTConduitActionResponse deleteResource(Resource resourceToDelete) {
        return null;
    }

    @Override
    public List<ResourceSoTConduitSearchResponseElement> getResourcesViaSearchCriteria(ResourceType resourceType, Property attributeName, Element atributeValue) {
        return null;
    }

    @Override
    public List<ResourceSoTConduitSearchResponseElement> getResourcesViaSearchCriteria(ResourceType resourceType, Map<Property, Serializable> parameterSet) {
        return null;
    }

    @Override
    public boolean supportiveOfSearchCritiera(ResourceType resourceType, Map<Property, Serializable> parameterSet) {
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

}