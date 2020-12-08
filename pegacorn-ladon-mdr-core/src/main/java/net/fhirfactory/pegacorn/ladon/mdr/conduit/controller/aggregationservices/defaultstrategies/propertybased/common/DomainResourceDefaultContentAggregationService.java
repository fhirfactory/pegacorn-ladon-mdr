package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.propertybased.common;

import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Resource;

abstract public class DomainResourceDefaultContentAggregationService extends PerPropertyBasedContentAggregationServiceBase {

    protected void aggregateResourceSuperClassByAttribute(ResourceSoTConduitActionResponse baseResponse, ResourceSoTConduitActionResponse additiveResponse) {
        getLogger().debug(".aggregateResourceSuperClassByAttribute(): Entry");
        Resource additiveResource = (Resource) additiveResponse.getResource();
        Resource baseResource = (Resource) baseResponse.getResource();

        // The id property
        if(!baseHasPrecedence("id", baseResponse, additiveResponse)){
            baseResource.setId(additiveResource.getId());
        }
        // The meta property
        if(!baseHasPrecedence("meta",baseResponse, additiveResponse)) {
            baseResource.setMeta(additiveResource.getMeta());
        }
        // The implicitRules property
        if(!baseHasPrecedence("implicitRules",baseResponse, additiveResponse)) {
            baseResource.setImplicitRules(additiveResource.getImplicitRules());
        }
        // The language property
        if(!baseHasPrecedence("language",baseResponse, additiveResponse)) {
            baseResource.setLanguage(additiveResource.getLanguage());
        }
        getLogger().debug(".aggregateResourceSuperClassByAttribute(): Exit");
    }

    protected void aggregateDomainResourceSuperClassByAttribute(ResourceSoTConduitActionResponse baseResponse, ResourceSoTConduitActionResponse additiveResponse) {
        getLogger().debug(".aggregateDomainResourceSuperClassByAttribute(): Entry");
        DomainResource additiveDomainResource = (DomainResource) additiveResponse.getResource();
        DomainResource baseDomainResource = (DomainResource) baseResponse.getResource();

        // The text property
        if(!baseHasPrecedence("text", baseResponse, additiveResponse)){
            baseDomainResource.setText(additiveDomainResource.getText());
        }
        // The contained property
        if(additiveDomainResource.hasContained()){
            for(Resource currentResource: additiveDomainResource.getContained()){
                baseDomainResource.addContained(currentResource);
            }
        }
        // The extension property
        if(additiveDomainResource.hasExtension()){
            for(Extension currentExtension: additiveDomainResource.getExtension()){
                if(baseDomainResource.hasExtension()){
                    if(!baseDomainResource.hasExtension(currentExtension.getUrl())){
                        baseDomainResource.addExtension(currentExtension);
                    }
                }else {
                    baseDomainResource.addExtension(currentExtension);
                }
            }
        }
        // The modifierExtension property
        if(additiveDomainResource.hasModifierExtension()){
            for(Extension currentExtension: additiveDomainResource.getModifierExtension()){
                baseDomainResource.addExtension(currentExtension);
            }
        }
        getLogger().debug(".aggregateDomainResourceSuperClassByAttribute(): Exit");
    }
}
