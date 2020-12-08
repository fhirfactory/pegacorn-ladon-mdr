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
public class PatientDefaultPropertyBasedContentAggregationService extends DomainResourceDefaultContentAggregationService {
    private static final Logger LOG = LoggerFactory.getLogger(PatientDefaultPropertyBasedContentAggregationService.class);

    @Override
    protected Logger getLogger(){return(LOG);}

    @Override
    protected String getAggregationServiceName() {
        return ("PatientDefaultResourceContentAggregationService");
    }

    @Override
    protected Identifier getBestIdentifier(Resource resource) {
        if(resource != null){
            Patient resourceSubclass = (Patient)resource;
            Identifier bestIdentifier = getIdentifierPicker().getBestIdentifier(resourceSubclass.getIdentifier());
            return(bestIdentifier);
        }
        return(null);
    }

    @Override
    protected void aggregateIntoBasePropertyByProperty(ResourceSoTConduitActionResponse baseResponse, ResourceSoTConduitActionResponse additiveResponse) {
        Patient basePatientResource = (Patient)baseResponse.getResource();
        Patient additivePatientResource = (Patient)baseResponse.getResource();
        // Merge "identifiers" (Identifier)
        getMergeHelpers().mergeIdentifiers(baseResponse, basePatientResource.getIdentifier(), additiveResponse, additivePatientResource.getIdentifier());
        // Merge "active" (Set the Active Flag: note, if any are Active, then make this one Active)
        if(basePatientResource.hasActive() && additivePatientResource.hasActive()){
            if(!basePatientResource.getActive() && additivePatientResource.getActive()){
                basePatientResource.setActive(true);
            }
        }
        if(!basePatientResource.hasActive()) {
            if (additivePatientResource.hasActive()) {
                if (additivePatientResource.getActive()) {
                    basePatientResource.setActive(true);
                }
            }
        }
        // Merge "name" (HumanName)
        getMergeHelpers().mergeHumanNames(baseResponse, basePatientResource.getName(), additiveResponse, additivePatientResource.getName());
        // Merge "telecom" (ContactPoint)
        getMergeHelpers().mergeContactPoints(baseResponse, basePatientResource.getTelecom(), additiveResponse, additivePatientResource.getTelecom());
        // Merge "address" (Address)
        getMergeHelpers().mergeAddress(baseResponse, basePatientResource.getAddress(), additiveResponse, additivePatientResource.getAddress());
        // Merge "gender" (Code)
        if(basePatientResource.hasGender() && additivePatientResource.hasGender()){
            if(!baseHasPrecedence("gender", baseResponse, additiveResponse)){
                basePatientResource.setGender(additivePatientResource.getGender());
            }
        }
        // Merge "deceased" (boolean/dateTime)
        if(!basePatientResource.hasDeceased()){
            if(additivePatientResource.hasDeceased()){
                basePatientResource.setDeceased(additivePatientResource.getDeceased());
            }
        }
        // Merge "birthDate" (date)
        if(!basePatientResource.hasBirthDate()){
            if(additivePatientResource.hasBirthDate()){
                basePatientResource.setBirthDate(additivePatientResource.getBirthDate());
            }
        }
        // Merge "maritalStatus" (CodeableConcept)
        if(!basePatientResource.hasMaritalStatus()){
            if(additivePatientResource.hasMaritalStatus()){
                basePatientResource.setMaritalStatus(additivePatientResource.getMaritalStatus());
            }
        }
        // Merge "multiBirth" (boolean/integer)
        if(!basePatientResource.hasMultipleBirth()){
            if(additivePatientResource.hasMultipleBirth()){
                basePatientResource.setMultipleBirth(additivePatientResource.getMaritalStatus());
            }
        }
        // Merge "photo" (Attachment)
        getMergeHelpers().mergeAttachments("photo", baseResponse, basePatientResource.getPhoto(), additiveResponse, additivePatientResource.getPhoto());
        // Merge "contacts" (BackboneElement)
        mergeContacts("contacts", baseResponse, basePatientResource.getContact(), additiveResponse, additivePatientResource.getContact());
        // Merge "communications" (BackboneElement)
        mergeCommunications("communication", baseResponse, basePatientResource.getCommunication(), additiveResponse, additivePatientResource.getCommunication());
        // Merge "generalPractitioner" (Reference)
        getMergeHelpers().mergeReferences(baseResponse, basePatientResource.getGeneralPractitioner(), additiveResponse, additivePatientResource.getGeneralPractitioner());
        // Merge "managingOrganization" (Reference)
        if(!basePatientResource.hasManagingOrganization()){
            if(additivePatientResource.hasManagingOrganization()){
                basePatientResource.setManagingOrganization(additivePatientResource.getManagingOrganization());
            }
        }

    }

    @Override
    protected void addIdentifier(Resource resource, Identifier ridIdentifier) {
        if(resource == null){
            return;
        }
        Patient resourceSubClass = (Patient) resource;
        resourceSubClass.addIdentifier(ridIdentifier);
    }

    @Override
    protected List<Identifier> getIdentifiers(ResourceSoTConduitActionResponse actionResponse) {
        if(actionResponse == null){
            return(new ArrayList<>());
        }
        Patient actionResponseResource = (Patient) actionResponse.getResource();
        if(actionResponseResource.hasIdentifier()){
            return(actionResponseResource.getIdentifier());
        }
        return(new ArrayList<>());
    }

    // Patient Centric Merge Helpers

    private void mergeContacts(String propertyName, ResourceSoTConduitActionResponse baseResponse, List<Patient.ContactComponent> baseContactSet, ResourceSoTConduitActionResponse additiveResponse, List<Patient.ContactComponent> additiveContactSet ){
        if(!propertyName.contentEquals("contact")){
            return;
        }
        if( additiveContactSet == null){
            return;
        }
        if( additiveContactSet.isEmpty()){
            return;
        }
        if( baseContactSet == null){
            baseContactSet = new ArrayList<Patient.ContactComponent>();
        }
        if( baseContactSet.isEmpty()){
            baseContactSet.addAll(additiveContactSet);
            return;
        }
        for(Patient.ContactComponent currentAdditiveContact: additiveContactSet){
            if(!contactAlreadyExistsInBase(baseContactSet, currentAdditiveContact)){
                baseContactSet.add(currentAdditiveContact);
            }
        }
    }

    private boolean contactAlreadyExistsInBase(List<Patient.ContactComponent> baseContactSet,Patient.ContactComponent additiveContact ){
        if(baseContactSet.isEmpty()){
            return(false);
        }
        if(additiveContact == null){
            return(true); // This isn't technically true, but our intent is for the calling code to just leave this instance alone
        }
        for(Patient.ContactComponent currentContact:  baseContactSet){
            boolean sameName = currentContact.getName().equalsShallow(additiveContact.getName());
            boolean sameRelationship = currentContact.getRelationship().equals(additiveContact.getRelationship());
            if(sameName && sameRelationship){
                return(true);
            }
        }
        return(false);
    }

    private void mergeCommunications(String propertyName, ResourceSoTConduitActionResponse baseResponse, List<Patient.PatientCommunicationComponent> baseCommunicationSet, ResourceSoTConduitActionResponse additiveResponse, List<Patient.PatientCommunicationComponent> additiveCommunicationSet ){
        if(!propertyName.equals("communication")){
            return;
        }
        if(additiveCommunicationSet == null){
            return;
        }
        if(additiveCommunicationSet.isEmpty()){
            return;
        }
        if(baseCommunicationSet.isEmpty()){
            baseCommunicationSet.addAll(additiveCommunicationSet);
        }
        for(Patient.PatientCommunicationComponent currentAdditiveCommunication: additiveCommunicationSet){
            if(!communicationAlreadyExistsInBase(baseCommunicationSet, currentAdditiveCommunication)){
                baseCommunicationSet.add(currentAdditiveCommunication);
            }
        }
    }
    private boolean communicationAlreadyExistsInBase(List<Patient.PatientCommunicationComponent> baseCommunicationSet,Patient.PatientCommunicationComponent additiveCommunication ){
        if(baseCommunicationSet.isEmpty()){
            return(false);
        }
        if(additiveCommunication == null){
            return(true); // This isn't technically true, but our intent is for the calling code to just leave this instance alone
        }
        for(Patient.PatientCommunicationComponent currentCommunication:  baseCommunicationSet){
            boolean sameLanguage = currentCommunication.getLanguage().equalsShallow(additiveCommunication.getLanguage());
            if(sameLanguage){
                return(true);
            }
        }
        return(false);
    }

}
