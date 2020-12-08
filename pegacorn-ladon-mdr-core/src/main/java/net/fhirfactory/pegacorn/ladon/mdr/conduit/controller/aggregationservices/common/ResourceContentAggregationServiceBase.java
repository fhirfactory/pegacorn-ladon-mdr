package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.common;

import net.fhirfactory.pegacorn.datasets.fhir.r4.codesystems.PegacornIdentifierCodeEnum;
import net.fhirfactory.pegacorn.datasets.fhir.r4.codesystems.PegacornIdentifierCodeSystemFactory;
import net.fhirfactory.pegacorn.datasets.fhir.r4.common.SourceOfTruthRIDIdentifierBuilder;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.businesskey.VirtualDBKeyManagement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.*;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;

public abstract class ResourceContentAggregationServiceBase {

    protected abstract Logger getLogger();
    protected abstract String getAggregationServiceName();
    protected abstract Identifier getBestIdentifier(Resource resource);
    protected abstract void addIdentifier(Resource resource, Identifier ridIdentifier);
    protected abstract List<Identifier> getIdentifiers(ResourceSoTConduitActionResponse actionResponse);

    @Inject
    private VirtualDBKeyManagement VirtualDBKeyHelpers;

    @Inject
    private PegacornIdentifierCodeSystemFactory pegacornIdentifierCodeSystemFactory;

    @Inject
    private SourceOfTruthRIDIdentifierBuilder sourceOfTruthRIDIdentifierBuilder;

    @Inject
    private FHIRContextUtility fhirContextUtility;

    protected FHIRContextUtility getFhirContextUtility(){
        return(fhirContextUtility);
    }

    protected VirtualDBKeyManagement getIdentifierPicker(){return(VirtualDBKeyHelpers);}

    public PegacornIdentifierCodeSystemFactory getPegacornIdentifierCodeSystemFactory() {
        return pegacornIdentifierCodeSystemFactory;
    }

    public SourceOfTruthRIDIdentifierBuilder getSourceOfTruthRIDIdentifierBuilder() {
        return sourceOfTruthRIDIdentifierBuilder;
    }

    //
    // Create Aggregation Methods
    //
    public abstract VirtualDBMethodOutcome aggregateCreateResponseSet(List<ResourceSoTConduitActionResponse> responseSet);
    //
    // Review / Get Aggregation Methods
    //
    public abstract VirtualDBMethodOutcome aggregateGetResponseSet(List<ResourceSoTConduitActionResponse> responseSet);
    //
    // Update Aggregation Methods
    //
    public abstract VirtualDBMethodOutcome aggregateUpdateResponseSet(List<ResourceSoTConduitActionResponse> responseSet);
    //
    // Delete Aggregation Methods
    //
    public abstract VirtualDBMethodOutcome aggregateDeleteResponseSet(List<ResourceSoTConduitActionResponse> responseSet);
    //
    // Search Result Aggregation
    //
    public abstract VirtualDBMethodOutcome aggregateSearchResultSet(List<ResourceSoTConduitSearchResponseElement> responseSet);

    protected void mapIdToIdentifier(ResourceSoTConduitActionResponse actionResponse){
        List<Identifier> identifierList = getIdentifiers(actionResponse);
        if(identifierList.isEmpty()){
            return;
        }
        CodeableConcept sotRIDCode = pegacornIdentifierCodeSystemFactory.buildIdentifierType(PegacornIdentifierCodeEnum.IDENTIFIER_CODE_SOURCE_OF_TRUTH_RECORD_ID);
        for(Identifier currentIdentifier: identifierList){
            if(currentIdentifier.getType().equalsDeep(sotRIDCode)){
                return;
            }
        }
        Resource responseResource = (Resource) actionResponse.getResource();
        if(!responseResource.hasId()){
            return;
        }
        Identifier sotRIDIdentifier = sourceOfTruthRIDIdentifierBuilder.constructRIDIdentifier(actionResponse.getSourceOfTruthEndpoint().getIdentifier().getValue(),responseResource.getId());
        addIdentifier(responseResource, sotRIDIdentifier);
    }
 }
