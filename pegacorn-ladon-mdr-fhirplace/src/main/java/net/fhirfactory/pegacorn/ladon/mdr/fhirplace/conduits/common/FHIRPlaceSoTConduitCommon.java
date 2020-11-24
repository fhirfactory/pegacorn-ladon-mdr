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
package net.fhirfactory.pegacorn.ladon.mdr.fhirplace.conduits.common;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.fhirfactory.pegacorn.platform.hapifhir.clients.JPAServerSecureAccessor;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.SoTResourceConduit;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionTypeEnum;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

public abstract class FHIRPlaceSoTConduitCommon extends SoTResourceConduit {

    @Override
    protected void doSubclassInitialisations(){
        getJPAServerSecureAccessor().initialise();
    }

    protected IGenericClient getFHIRPlaceShardClient(){
        return(getJPAServerSecureAccessor().getClient());
    }

    abstract protected JPAServerSecureAccessor specifyJPAServerSecureAccessor();

    protected JPAServerSecureAccessor getJPAServerSecureAccessor(){
        return(specifyJPAServerSecureAccessor());
    }
    /**
     *
     * @param resourceToCreate
     * @return
     */

    public ResourceSoTConduitActionResponse standardCreateResource(Resource resourceToCreate) {
        getLogger().debug(".standardCreateResource(): Entry, resourceToCreate --> {}", resourceToCreate);
        MethodOutcome callOutcome = getFHIRPlaceShardClient()
                .create()
                .resource(resourceToCreate)
                .prettyPrint()
                .encodedJson()
                .execute();
        if(!callOutcome.getCreated()) {
            getLogger().error(".writeResource(): Can't create Resource {}, error --> {}", callOutcome.getOperationOutcome());
        }
        Identifier bestIdentifier = getBestIdentifier(callOutcome);
        ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse(VirtualDBActionTypeEnum.CREATE, bestIdentifier, callOutcome);
        getLogger().debug(".standardCreateResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     *
     * @param resourceClass
     * @param identifier
     * @return
     */

    public ResourceSoTConduitActionResponse standardReviewResource(Class <? extends IBaseResource> resourceClass, Identifier identifier){
        return(standardGetResource(resourceClass, identifier));
    }

    /**
     *
     * @param resourceClass
     * @param identifier
     * @return
     */

    public ResourceSoTConduitActionResponse standardGetResource(Class <? extends IBaseResource> resourceClass, Identifier identifier){
        getLogger().debug(".standardGetResource(): Entry, identifier --> {}", identifier);
        Bundle outputBundle = getFHIRPlaceShardClient()
                .search()
                .forResource(resourceClass)
                .where(Patient.IDENTIFIER.exactly().systemAndValues(identifier.getSystem(), identifier.getValue()))
                .returnBundle(Bundle.class)
                .execute();
        boolean hasOutcome = (outputBundle != null);
        if(hasOutcome){
            hasOutcome = (outputBundle.getTotal() > 0);
        }
        if(!hasOutcome){
            // There was no Resource with that Identifier....
            ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse();
            outcome.setCreated(false);
            outcome.setCausalAction(VirtualDBActionTypeEnum.REVIEW);
            outcome.setStatusEnum(VirtualDBActionStatusEnum.REVIEW_FAILURE);
            CodeableConcept details = new CodeableConcept();
            Coding detailsCoding = new Coding();
            detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html");
            detailsCoding.setCode("MSG_NO_MATCH");
            detailsCoding.setDisplay("No Resource found matching the query: " + identifier);
            details.setText("No Resource found matching the query: " + identifier);
            details.addCoding(detailsCoding);
            OperationOutcome opOutcome = new OperationOutcome();
            OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
            newOutcomeComponent.setDiagnostics("standardReviewResource()" + "::" + "REVIEW");
            newOutcomeComponent.setDetails(details);
            newOutcomeComponent.setCode(OperationOutcome.IssueType.NOTFOUND);
            newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.WARNING);
            opOutcome.addIssue(newOutcomeComponent);
            outcome.setOperationOutcome(opOutcome);
            outcome.setIdentifier(identifier);
            return(outcome);
        }
        if(outputBundle.getTotal() > 1){
            // There should only be one!
            ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse();
            outcome.setCreated(false);
            outcome.setCausalAction(VirtualDBActionTypeEnum.REVIEW);
            outcome.setStatusEnum(VirtualDBActionStatusEnum.REVIEW_FAILURE);
            CodeableConcept details = new CodeableConcept();
            Coding detailsCoding = new Coding();
            detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html");
            detailsCoding.setCode("MSG_MULTIPLE_MATCH");
            detailsCoding.setDisplay("Multiple Resources found matching the query: " + identifier);
            details.setText("Multiple Resources found matching the query: " + identifier);
            details.addCoding(detailsCoding);
            OperationOutcome opOutcome = new OperationOutcome();
            OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
            newOutcomeComponent.setDiagnostics("standardReviewResource()" + "::" + "REVIEW");
            newOutcomeComponent.setDetails(details);
            newOutcomeComponent.setCode(OperationOutcome.IssueType.MULTIPLEMATCHES);
            newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            opOutcome.addIssue(newOutcomeComponent);
            outcome.setOperationOutcome(opOutcome);
            outcome.setIdentifier(identifier);
            return(outcome);
        }
        // There is only be one!
        Bundle.BundleEntryComponent bundleEntry = outputBundle.getEntryFirstRep();
        Resource retrievedResource = bundleEntry.getResource();
        ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse();
        outcome.setCreated(false);
        outcome.setCausalAction(VirtualDBActionTypeEnum.REVIEW);
        outcome.setStatusEnum(VirtualDBActionStatusEnum.REVIEW_FINISH);
        CodeableConcept details = new CodeableConcept();
        Coding detailsCoding = new Coding();
        detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html"); // TODO Pegacorn specific encoding --> need to check validity
        detailsCoding.setCode("MSG_RESOURCE_RETRIEVED"); // TODO Pegacorn specific encoding --> need to check validity
        detailsCoding.setDisplay("Resource Id ("+ identifier +") has been retrieved");
        details.setText("Resource Id ("+ identifier +") has been retrieved");
        details.addCoding(detailsCoding);
        OperationOutcome opOutcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
        newOutcomeComponent.setDiagnostics("standardReviewResource()" + "::" + "REVIEW");
        newOutcomeComponent.setDetails(details);
        newOutcomeComponent.setCode(OperationOutcome.IssueType.INFORMATIONAL);
        newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
        opOutcome.addIssue(newOutcomeComponent);
        outcome.setOperationOutcome(opOutcome);
        outcome.setResource(retrievedResource);
        outcome.setId(retrievedResource.getIdElement());
        outcome.setIdentifier(identifier);
        getLogger().debug(".standardReviewResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     *
     * @param resourceClass
     * @param id
     * @return
     */

    public ResourceSoTConduitActionResponse standardReviewResource(Class <? extends IBaseResource> resourceClass, IdType id){
        return(standardGetResource(resourceClass, id));
    }

    /**
     *
     * @param resourceClass
     * @param id
     * @return
     */

    public ResourceSoTConduitActionResponse standardGetResource(Class <? extends IBaseResource> resourceClass, IdType id){
        getLogger().debug(".standardGetResource(): Entry, identifier --> {}", id);
        Resource retrievedResource = (Resource)getFHIRPlaceShardClient()
                .read()
                .resource(resourceClass.getName())
                .withId(id)
                .execute();
        boolean hasOutcome = (retrievedResource != null);
        if(!hasOutcome){
            // There was no Resource with that Identifier....
            ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse();
            outcome.setCreated(false);
            outcome.setCausalAction(VirtualDBActionTypeEnum.REVIEW);
            outcome.setStatusEnum(VirtualDBActionStatusEnum.REVIEW_FAILURE);
            CodeableConcept details = new CodeableConcept();
            Coding detailsCoding = new Coding();
            detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html");
            detailsCoding.setCode("MSG_NO_EXIST");
            String text = "Resource Id " + id.toString() + "does not exist";
            detailsCoding.setDisplay(text);
            details.setText(text);
            details.addCoding(detailsCoding);
            OperationOutcome opOutcome = new OperationOutcome();
            OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
            newOutcomeComponent.setDiagnostics("standardReviewResource()" + "::" + "REVIEW");
            newOutcomeComponent.setDetails(details);
            newOutcomeComponent.setCode(OperationOutcome.IssueType.NOTFOUND);
            newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.WARNING);
            opOutcome.addIssue(newOutcomeComponent);
            outcome.setOperationOutcome(opOutcome);
            outcome.setId(id);
            return(outcome);
        }
        ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse();
        outcome.setCreated(false);
        outcome.setCausalAction(VirtualDBActionTypeEnum.REVIEW);
        outcome.setStatusEnum(VirtualDBActionStatusEnum.REVIEW_FINISH);
        CodeableConcept details = new CodeableConcept();
        Coding detailsCoding = new Coding();
        detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html"); // TODO Pegacorn specific encoding --> need to check validity
        detailsCoding.setCode("MSG_RESOURCE_RETRIEVED"); // TODO Pegacorn specific encoding --> need to check validity
        detailsCoding.setDisplay("Resource Id ("+ id +") has been retrieved");
        details.setText("Resource Id ("+ id +") has been retrieved");
        details.addCoding(detailsCoding);
        OperationOutcome opOutcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
        newOutcomeComponent.setDiagnostics("standardReviewResource()" + "::" + "REVIEW");
        newOutcomeComponent.setDetails(details);
        newOutcomeComponent.setCode(OperationOutcome.IssueType.INFORMATIONAL);
        newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
        opOutcome.addIssue(newOutcomeComponent);
        outcome.setOperationOutcome(opOutcome);
        outcome.setResource(retrievedResource);
        outcome.setId(retrievedResource.getIdElement());
        getLogger().debug(".standardReviewResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     *
     * @param resourceToUpdate
     * @return
     */

    public ResourceSoTConduitActionResponse standardUpdateResource(Resource resourceToUpdate) {
        getLogger().debug(".standardUpdateResource(): Entry, resourceToUpdate --> {}", resourceToUpdate);
        MethodOutcome callOutcome = getFHIRPlaceShardClient()
                .update()
                .resource(resourceToUpdate)
                .prettyPrint()
                .encodedJson()
                .execute();
        if(!callOutcome.getCreated()) {
            getLogger().error(".writeResource(): Can't update Resource {}, error --> {}", callOutcome.getOperationOutcome());
        }
        Identifier bestIdentifier = getBestIdentifier(callOutcome);
        ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse(VirtualDBActionTypeEnum.UPDATE, bestIdentifier, callOutcome);
        getLogger().debug(".standardUpdateResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     *
     * @param resourceToDelete
     * @return
     */

    public ResourceSoTConduitActionResponse standardDeleteResource(Resource resourceToDelete) {
        return null;
    }
}
