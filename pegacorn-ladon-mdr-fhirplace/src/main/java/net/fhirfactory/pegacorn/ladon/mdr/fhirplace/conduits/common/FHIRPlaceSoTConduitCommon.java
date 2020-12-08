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

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.fhirfactory.pegacorn.datasets.fhir.r4.internal.systems.DeploymentInstanceDetailInterface;
import net.fhirfactory.pegacorn.deployment.names.PegacornFHIRPlaceMDRComponentNames;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponseFactory;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.SoTResourceConduit;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionTypeEnum;
import net.fhirfactory.pegacorn.petasos.model.itops.PegacornFunctionStatusEnum;
import net.fhirfactory.pegacorn.platform.restfulapi.PegacornInternalFHIRClientServices;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import javax.inject.Inject;

public abstract class FHIRPlaceSoTConduitCommon extends SoTResourceConduit {

    @Inject
    private PegacornFHIRPlaceMDRComponentNames pegacornFHIRPlaceMDRComponentNames;

    @Inject
    private DeploymentInstanceDetailInterface deploymentInstanceDetailInterface;

    @Inject
    private ResourceSoTConduitActionResponseFactory sotConduitOutcomeFactory;

    @Inject
    FHIRContextUtility fhirContextUtility;

    @Override
    protected void doSubclassInitialisations(){
        getFHIRServiceAccessor().initialise();
    }

    protected IGenericClient getFHIRPlaceShardClient(){
        return(getFHIRServiceAccessor().getClient());
    }

    abstract protected PegacornInternalFHIRClientServices specifySecureAccessor();

    protected PegacornInternalFHIRClientServices getFHIRServiceAccessor(){
        return(specifySecureAccessor());
    }

    public PegacornFHIRPlaceMDRComponentNames getPegacornFHIRPlaceMDRComponentNames() {
        return pegacornFHIRPlaceMDRComponentNames;
    }

    @Override
    protected String specifySourceOfTruthOwningOrganization() {
        return (deploymentInstanceDetailInterface.getDeploymentInstanceOrganizationName());
    }

    @Override
    public String getConduitName(){
        return(pegacornFHIRPlaceMDRComponentNames.getPegacornFHIRPlaceMDRName());
    }

    @Override
    public String getConduitVersion(){
        return(pegacornFHIRPlaceMDRComponentNames.getPegacornFHIRPlaceMDRVersion());
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
        ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse( getSourceOfTruthOwningOrganization(), getSourceOfTruthEndpoint(), VirtualDBActionTypeEnum.CREATE, bestIdentifier, callOutcome);
        getLogger().debug(".standardCreateResource(): Exit, outcome --> {}", outcome);
        return(outcome);
    }

    /**
     *
     * @param resourceClass
     * @param identifier
     * @return
     */

    public ResourceSoTConduitActionResponse standardGetResourceViaIdentifier(Class <? extends IBaseResource> resourceClass, Identifier identifier){
        getLogger().debug(".standardGetResourceViaIdentifier(): Entry, identifier --> {}", identifier);
        if(getLogger().isDebugEnabled()) {
            getLogger().debug(".standardGetResourceViaIdentifier(): Entry identifier.type.system --> {}", identifier.getType().getCodingFirstRep().getSystem());
            getLogger().debug(".standardGetResourceViaIdentifier(): Entry, identifier.type.code --> {}", identifier.getType().getCodingFirstRep().getCode());
            getLogger().debug(".standardGetResourceViaIdentifier(): Entry, identifier.value --> {}", identifier.getValue());
        }
        String activityLocation = resourceClass.getSimpleName() + "SoTResourceConduit::standardGetResourceViaIdentifier()";
        Resource retrievedResource = (Resource)getFHIRServiceAccessor().findResourceByIdentifier(resourceClass.getSimpleName(), identifier);
        if (retrievedResource == null){
            // There was no response to the query or it was in error....
            getLogger().trace(".standardGetResourceViaIdentifier(): There was no response to the query or it was in error....");
            ResourceSoTConduitActionResponse outcome = sotConduitOutcomeFactory.createResourceConduitActionResponse(
                    getSourceOfTruthEndpointName(), PegacornFunctionStatusEnum.FUNCTION_STATUS_OK, null, null, VirtualDBActionStatusEnum.REVIEW_FAILURE, activityLocation);
            outcome.setIdentifier(identifier);
            return(outcome);
        }
        if(getLogger().isTraceEnabled()) {
            IParser iParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
            getLogger().trace(".standardGetResourceViaIdentifier(): Resource has been retrieved, value --> {}", iParser.encodeResourceToString(retrievedResource));
        }
        getLogger().trace(".standardGetResourceViaIdentifier(): There one and only one Resource with that Identifier....");
        ResourceSoTConduitActionResponse outcome = sotConduitOutcomeFactory.createResourceConduitActionResponse(
                getSourceOfTruthEndpointName(),
                PegacornFunctionStatusEnum.FUNCTION_STATUS_OK,
                retrievedResource,
                null,
                VirtualDBActionStatusEnum.REVIEW_FINISH,
                activityLocation);
        outcome.setIdentifier(identifier);
        getLogger().debug(".standardGetResourceViaIdentifier(): Exit, outcome --> {}", outcome);
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
        String activityLocation = resourceClass.getSimpleName() + "SoTResourceConduit::standardGetResourceViaIdentifier()";
        if(retrievedResource == null){
            // There was no Resource with that Identifier....
            getLogger().trace(".standardGetResourceViaIdentifier(): There was no Resource with that Identifier....");
            ResourceSoTConduitActionResponse outcome = sotConduitOutcomeFactory.createResourceConduitActionResponse(
                    getSourceOfTruthEndpointName(),
                    PegacornFunctionStatusEnum.FUNCTION_STATUS_OK,
                    null,
                    id,
                    VirtualDBActionStatusEnum.REVIEW_FAILURE,
                    activityLocation);
            return(outcome);
        } else {
            ResourceSoTConduitActionResponse outcome = sotConduitOutcomeFactory.createResourceConduitActionResponse(
                    getSourceOfTruthEndpointName(),
                    PegacornFunctionStatusEnum.FUNCTION_STATUS_OK,
                    retrievedResource,
                    null,
                    VirtualDBActionStatusEnum.REVIEW_FINISH,
                    activityLocation);
            getLogger().debug(".standardReviewResource(): Exit, outcome --> {}", outcome);
            return (outcome);
        }
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
        ResourceSoTConduitActionResponse outcome = new ResourceSoTConduitActionResponse(getSourceOfTruthOwningOrganization(), getSourceOfTruthEndpoint(), VirtualDBActionTypeEnum.UPDATE, bestIdentifier, callOutcome);
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
