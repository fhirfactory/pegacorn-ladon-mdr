package net.fhirfactory.pegacorn.ladon.mdr.conduit.aggregationservices.common;

import net.fhirfactory.pegacorn.ladon.model.virtualdb.businesskey.VirtualDBKeyManagement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.*;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;

public abstract class ResourceContentAggregationServiceBase {

    protected abstract Logger getLogger();
    protected abstract String getAggregationServiceName();
    protected abstract Identifier getBestIdentifier(Resource resource);

    @Inject
    private VirtualDBKeyManagement VirtualDBKeyHelpers;

    protected VirtualDBKeyManagement getIdentifierPicker(){return(VirtualDBKeyHelpers);}

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
 }
