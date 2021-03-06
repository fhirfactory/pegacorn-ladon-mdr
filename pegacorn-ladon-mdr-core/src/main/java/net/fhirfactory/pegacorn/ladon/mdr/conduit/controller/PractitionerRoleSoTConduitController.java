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
package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller;

import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.common.ResourceContentAggregationServiceBase;
import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.propertybased.PractitionerRoleDefaultPropertyBasedContentAggregationService;
import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.common.ResourceSoTConduitController;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PractitionerRoleSoTConduitController extends ResourceSoTConduitController {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerRoleSoTConduitController.class);

    @Inject
    PractitionerRoleDefaultPropertyBasedContentAggregationService aggregationService;

    @Override
    protected Logger getLogger(){return(LOG);}

    @Override
    protected ResourceType specifyResourceType() {
        return (ResourceType.PractitionerRole);
    }

    @Override
    protected ResourceContentAggregationServiceBase specifyAggregationService(){
        return(aggregationService);
    }


}
