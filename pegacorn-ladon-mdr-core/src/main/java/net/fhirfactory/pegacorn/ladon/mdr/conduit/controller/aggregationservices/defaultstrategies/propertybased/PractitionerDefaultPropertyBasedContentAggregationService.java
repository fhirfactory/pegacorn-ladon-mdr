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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PractitionerDefaultPropertyBasedContentAggregationService extends DomainResourceDefaultContentAggregationService {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerDefaultPropertyBasedContentAggregationService.class);

    @Override
    protected Logger getLogger(){return(LOG);}

    @Override
    protected String getAggregationServiceName() {
        return ("PractitionerDefaultResourceContentAggregationService");
    }

    @Override
    protected Identifier getBestIdentifier(Resource resource) {
        if(resource != null){
            Practitioner resourceSubclass = (Practitioner)resource;
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
        Practitioner resourceSubClass = (Practitioner) resource;
        resourceSubClass.addIdentifier(ridIdentifier);
    }

    @Override
    protected void aggregateIntoBasePropertyByProperty(ResourceSoTConduitActionResponse baseResponse, ResourceSoTConduitActionResponse additiveResponse) {
        Practitioner basePractitionerResource = (Practitioner)baseResponse.getResource();
        Practitioner additivePractitionerResource = (Practitioner)baseResponse.getResource();
        // Merge "identifiers" (Identifier)
        getMergeHelpers().mergeIdentifiers(baseResponse, basePractitionerResource.getIdentifier(), additiveResponse, additivePractitionerResource.getIdentifier());
        // Merge "active" (Set the Active Flag: note, if any are Active, then make this one Active)
        if(basePractitionerResource.hasActive() && additivePractitionerResource.hasActive()){
            if(!basePractitionerResource.getActive() && additivePractitionerResource.getActive()){
                basePractitionerResource.setActive(true);
            }
        }
        if(!basePractitionerResource.hasActive()){
            if(additivePractitionerResource.hasActive()){
                if(additivePractitionerResource.getActive()){
                    basePractitionerResource.setActive(true);
                }
            }
        }
        // Merge "name" (HumanName)
        getMergeHelpers().mergeHumanNames(baseResponse, basePractitionerResource.getName(), additiveResponse, additivePractitionerResource.getName());
        // Merge "telecom" (ContactPoint)
        getMergeHelpers().mergeContactPoints(baseResponse, basePractitionerResource.getTelecom(), additiveResponse, additivePractitionerResource.getTelecom());
        // Merge "address" (Address)
        getMergeHelpers().mergeAddress(baseResponse, basePractitionerResource.getAddress(), additiveResponse, additivePractitionerResource.getAddress());
        // Merge "gender" (Code)
        if(basePractitionerResource.hasGender() && additivePractitionerResource.hasGender()){
            if(!baseHasPrecedence("gender", baseResponse, additiveResponse)){
                basePractitionerResource.setGender(additivePractitionerResource.getGender());
            }
        }
        if(!basePractitionerResource.hasGender()){
            if(additivePractitionerResource.hasGender()){
                basePractitionerResource.setGender(additivePractitionerResource.getGender());
            }
        }
        // Merge "birthDate" (Date)
        if(basePractitionerResource.hasBirthDate() && additivePractitionerResource.hasBirthDate()){
            if(!baseHasPrecedence("birthDate", baseResponse, additiveResponse)){
                basePractitionerResource.setBirthDate(additivePractitionerResource.getBirthDate());
            }
        }
        if(!basePractitionerResource.hasBirthDate()){
            if(additivePractitionerResource.hasBirthDate()){
                basePractitionerResource.setBirthDate(additivePractitionerResource.getBirthDate());
            }
        }
        // Merge "photo" (Attachment)
        getMergeHelpers().mergeAttachments("photo", baseResponse, basePractitionerResource.getPhoto(), additiveResponse, additivePractitionerResource.getPhoto());
        // Merge  "qualification" (BackboneElement: Practitioner.PractitionerQualificationComponent)
        mergeQualifications(basePractitionerResource.getQualification(), additivePractitionerResource.getQualification());
        // Merge "communication" (CodeableConcept)
        getMergeHelpers().mergeCodeableConcept(basePractitionerResource.getCommunication(), additivePractitionerResource.getCommunication());
    }

    private void mergeQualifications(List<Practitioner.PractitionerQualificationComponent> baseResourceQualificationSet, List<Practitioner.PractitionerQualificationComponent> additiveResourceQualificationSet){
        if(baseResourceQualificationSet.isEmpty()){
            baseResourceQualificationSet.addAll(additiveResourceQualificationSet);
            return;
        }
        if(additiveResourceQualificationSet.isEmpty()){
            return;
        }
        for(Practitioner.PractitionerQualificationComponent currentAdditiveQualification: additiveResourceQualificationSet){
            boolean isFound = false;
            for(Practitioner.PractitionerQualificationComponent currentBaseQualification: baseResourceQualificationSet){
                if(currentAdditiveQualification.equalsDeep(currentBaseQualification)){
                    isFound = true;
                    break;
                }
            }
            if(!isFound){
                baseResourceQualificationSet.add(currentAdditiveQualification);
            }
        }
    }

    @Override
    protected List<Identifier> getIdentifiers(ResourceSoTConduitActionResponse actionResponse) {
        if(actionResponse == null){
            return(new ArrayList<>());
        }
        Practitioner actionResponseResource = (Practitioner) actionResponse.getResource();
        if(actionResponseResource.hasIdentifier()){
            return(actionResponseResource.getIdentifier());
        }
        return(new ArrayList<>());
    }
}
