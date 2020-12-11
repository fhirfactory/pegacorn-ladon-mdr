package net.fhirfactory.pegacorn.ladon.mdr.conduit.core;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponseFactory;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.SoTResourceConduit;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.petasos.model.itops.PegacornFunctionStatusEnum;
import net.fhirfactory.pegacorn.platform.restfulapi.PegacornInternalFHIRClientServices;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;

import javax.inject.Inject;

public abstract class SoTResourceConduitFunctionBase extends SoTResourceConduit {
    @Inject
    FHIRContextUtility fhirContextUtility;
    @Inject
    private ResourceSoTConduitActionResponseFactory sotConduitOutcomeFactory;

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

    protected ResourceSoTConduitActionResponseFactory getSotConduitOutcomeFactory(){
        return(sotConduitOutcomeFactory);
    }


    @Override
    public ResourceSoTConduitActionResponse getResourceViaIdentifier(Identifier identifier) {
        return (standardGetResourceViaIdentifier(getResourceType().toString(), identifier));
    }

    /**
     *
     * @param resourceName
     * @param identifier
     * @return
     */

    public ResourceSoTConduitActionResponse standardGetResourceViaIdentifier(String resourceName, Identifier identifier){
        getLogger().debug(".standardGetResourceViaIdentifier(): Entry, identifier --> {}", identifier);
        if(getLogger().isDebugEnabled()) {
            getLogger().debug(".standardGetResourceViaIdentifier(): Entry identifier.type.system --> {}", identifier.getType().getCodingFirstRep().getSystem());
            getLogger().debug(".standardGetResourceViaIdentifier(): Entry, identifier.type.code --> {}", identifier.getType().getCodingFirstRep().getCode());
            getLogger().debug(".standardGetResourceViaIdentifier(): Entry, identifier.value --> {}", identifier.getValue());
        }
        String activityLocation = resourceName + "SoTResourceConduit::standardGetResourceViaIdentifier()";
        Resource retrievedResource = (Resource)getFHIRServiceAccessor().findResourceByIdentifier(resourceName, identifier);
        if (retrievedResource == null){
            // There was no response to the query or it was in error....
            getLogger().trace(".standardGetResourceViaIdentifier(): There was no response to the query or it was in error....");
            ResourceSoTConduitActionResponse outcome = sotConduitOutcomeFactory.createResourceConduitActionResponse(
                    getSourceOfTruthEndpointName(), PegacornFunctionStatusEnum.FUNCTION_STATUS_OK, null, null, VirtualDBActionStatusEnum.REVIEW_FAILURE, activityLocation);
            outcome.setIdentifier(identifier);
            outcome.setResource(null);
            getLogger().debug(".standardGetResourceViaIdentifier(): Exit, Returning \"failed\" outcome....");
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
        return(standardGetResource(resourceClass.getSimpleName(), id));
    }



    @Override
    public ResourceSoTConduitActionResponse reviewResource(IdType id) {
        ResourceSoTConduitActionResponse outcome = standardGetResource(getResourceType().toString(), id);
        return(outcome);
    }

    /**
     *
     * @param resourceName
     * @param id
     * @return
     */

    public ResourceSoTConduitActionResponse standardGetResource(String resourceName, IdType id){
        getLogger().debug(".standardGetResource(): Entry, identifier --> {}", id);
        Resource retrievedResource = (Resource)getFHIRPlaceShardClient()
                .read()
                .resource(resourceName)
                .withId(id)
                .execute();
        String activityLocation = resourceName + "SoTResourceConduit::standardGetResourceViaIdentifier()";
        if(retrievedResource == null){
            // There was no Resource with that Identifier....
            getLogger().trace(".standardGetResourceViaIdentifier(): There was no Resource with that Identifier....");
            ResourceSoTConduitActionResponse outcome = getSotConduitOutcomeFactory().createResourceConduitActionResponse(
                    getSourceOfTruthEndpointName(),
                    PegacornFunctionStatusEnum.FUNCTION_STATUS_OK,
                    null,
                    id,
                    VirtualDBActionStatusEnum.REVIEW_FAILURE,
                    activityLocation);
            return(outcome);
        } else {
            ResourceSoTConduitActionResponse outcome = getSotConduitOutcomeFactory().createResourceConduitActionResponse(
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
}
