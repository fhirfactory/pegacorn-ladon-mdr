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
package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.propertybased;

import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.propertybased.common.DomainResourceDefaultContentAggregationService;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PractitionerRoleDefaultPropertyBasedContentAggregationService extends DomainResourceDefaultContentAggregationService {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerRoleDefaultPropertyBasedContentAggregationService.class);

    @Override
    protected Logger getLogger(){return(LOG);}

    @Override
    protected String getAggregationServiceName() {
        return ("PractitionerRoleDefaultResourceContentAggregationService");
    }

    @Override
    protected Identifier getBestIdentifier(Resource resource) {
        if(resource != null){
            PractitionerRole resourceSubclass = (PractitionerRole)resource;
            Identifier bestIdentifier = getIdentifierPicker().getBestIdentifier(resourceSubclass.getIdentifier());
            return(bestIdentifier);
        }
        return(null);
    }

    @Override
    protected void addIdentifier(Resource resource, Identifier ridIdentifier) {
        if(resource == null){
            return;
        }
        PractitionerRole resourceSubClass = (PractitionerRole) resource;
        resourceSubClass.addIdentifier(ridIdentifier);
    }

    @Override
    protected void aggregateIntoBasePropertyByProperty(ResourceSoTConduitActionResponse baseResponse, ResourceSoTConduitActionResponse additiveResponse) {
        PractitionerRole basePractitionerRoleResource = (PractitionerRole)baseResponse.getResource();
        PractitionerRole additivePractitionerRoleResource = (PractitionerRole)baseResponse.getResource();
        // Merge "identifiers" (Identifier)
        getMergeHelpers().mergeIdentifiers(baseResponse, basePractitionerRoleResource.getIdentifier(), additiveResponse, additivePractitionerRoleResource.getIdentifier());
        // Merge "active" (Set the Active Flag: note, if any are Active, then make this one Active)
        if(basePractitionerRoleResource.hasActive() && additivePractitionerRoleResource.hasActive()){
            if(!basePractitionerRoleResource.getActive() && additivePractitionerRoleResource.getActive()){
                basePractitionerRoleResource.setActive(true);
            }
        }
        if(!basePractitionerRoleResource.hasActive()){
            if(additivePractitionerRoleResource.hasActive()){
                if(additivePractitionerRoleResource.getActive()){
                    basePractitionerRoleResource.setActive(true);
                }
            }
        }
        // Merge "period" (Period)
        if(basePractitionerRoleResource.hasPeriod() && additivePractitionerRoleResource.hasPeriod()){
            if(!baseHasPrecedence("period", baseResponse, additiveResponse)){
                basePractitionerRoleResource.setPeriod(additivePractitionerRoleResource.getPeriod());
            }
        }
        if(!basePractitionerRoleResource.hasPeriod()){
            if(additivePractitionerRoleResource.hasPeriod()){
                basePractitionerRoleResource.setPeriod(additivePractitionerRoleResource.getPeriod());
            }
        }
        // Merge practitioner (Reference)
        if(basePractitionerRoleResource.hasPractitioner() && additivePractitionerRoleResource.hasPractitioner()){
            if(!baseHasPrecedence("practitioner", baseResponse, additiveResponse)){
                basePractitionerRoleResource.setPractitioner(additivePractitionerRoleResource.getPractitioner());
            }
        }
        if(!basePractitionerRoleResource.hasPractitioner()){
            if(additivePractitionerRoleResource.hasPractitioner()){
                basePractitionerRoleResource.setPractitioner(additivePractitionerRoleResource.getPractitioner());
            }
        }
        // Merge organization (Reference)
        if(basePractitionerRoleResource.hasOrganization() && additivePractitionerRoleResource.hasOrganization()){
            if(!baseHasPrecedence("organization", baseResponse, additiveResponse)){
                basePractitionerRoleResource.setOrganization(additivePractitionerRoleResource.getOrganization());
            }
        }
        if(!basePractitionerRoleResource.hasOrganization()){
            if(additivePractitionerRoleResource.hasOrganization()){
                basePractitionerRoleResource.setOrganization(additivePractitionerRoleResource.getOrganization());
            }
        }
        // Merge "code" (Code)
        getMergeHelpers().mergeCodeableConcept(basePractitionerRoleResource.getCode(), additivePractitionerRoleResource.getCode());
        // Merge "specialty
        getMergeHelpers().mergeCodeableConcept(basePractitionerRoleResource.getSpecialty(), additivePractitionerRoleResource.getSpecialty());
        // Merge "location" (Reference)
        getMergeHelpers().mergeReferences(baseResponse, basePractitionerRoleResource.getLocation(), additiveResponse, additivePractitionerRoleResource.getLocation() );
        // Merge "healthcareService" (HealthcareService)
        getMergeHelpers().mergeReferences(baseResponse, basePractitionerRoleResource.getHealthcareService(), additiveResponse, additivePractitionerRoleResource.getHealthcareService() );
        // Merge "telecom" (ContactPoint)
        getMergeHelpers().mergeContactPoints(baseResponse, basePractitionerRoleResource.getTelecom(), additiveResponse, additivePractitionerRoleResource.getTelecom());
        // Merge "availableTime" (BackboneElement: PractitionerRole.PractitionerRoleAvailableTimeComponent)
        // TODO Need to discuss the aggregation of "PractitionerRole.availableTime" with business owner
        // Merge "notAvailable" (BackboneElement: PractitionerRole.PractitionerRoleNotAvailableComponent)
        // TODO Need to discuss the aggregation of "PractitionerRole.notAvailable" with business owner
        // Merge "endpoint" (Reference)
        getMergeHelpers().mergeReferences(baseResponse, basePractitionerRoleResource.getEndpoint(), additiveResponse, additivePractitionerRoleResource.getEndpoint());
    }

    @Override
    protected List<Identifier> getIdentifiers(ResourceSoTConduitActionResponse actionResponse) {
        if(actionResponse == null){
            return(new ArrayList<>());
        }
        PractitionerRole actionResponseResource = (PractitionerRole) actionResponse.getResource();
        if(actionResponseResource.hasIdentifier()){
            return(actionResponseResource.getIdentifier());
        }
        return(new ArrayList<>());
    }
}
