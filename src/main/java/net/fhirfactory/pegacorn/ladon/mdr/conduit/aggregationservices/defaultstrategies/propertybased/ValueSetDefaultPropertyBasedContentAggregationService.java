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
package net.fhirfactory.pegacorn.ladon.mdr.conduit.aggregationservices.defaultstrategies.propertybased;

import net.fhirfactory.pegacorn.ladon.mdr.conduit.aggregationservices.defaultstrategies.propertybased.common.DomainResourceDefaultContentAggregationService;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ValueSetDefaultPropertyBasedContentAggregationService extends DomainResourceDefaultContentAggregationService {
    private static final Logger LOG = LoggerFactory.getLogger(ValueSetDefaultPropertyBasedContentAggregationService.class);

    @Override
    protected Logger getLogger(){return(LOG);}

    @Override
    protected String getAggregationServiceName() {
        return ("ValueSetDefaultResourceContentAggregationService");
    }

    @Override
    protected Identifier getBestIdentifier(Resource resource) {
        if(resource != null){
            ValueSet resourceSubclass = (ValueSet)resource;
            Identifier bestIdentifier = getIdentifierPicker().getBestIdentifier(resourceSubclass.getIdentifier());
            return(bestIdentifier);
        }
        return(null);
    }

    @Override
    protected void aggregateIntoBasePropertyByProperty(ResourceSoTConduitActionResponse baseResource, ResourceSoTConduitActionResponse additiveResource) {

    }

    @Override
    protected List<Identifier> getIdentifiers(ResourceSoTConduitActionResponse actionResponse) {
        if(actionResponse == null){
            return(new ArrayList<>());
        }
        ValueSet actionResponseResource = (ValueSet) actionResponse.getResource();
        if(actionResponseResource.hasIdentifier()){
            return(actionResponseResource.getIdentifier());
        }
        return(new ArrayList<>());
    }
}
