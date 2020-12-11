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
package net.fhirfactory.pegacorn.ladon.mdr.fhirplace.accessor;

import net.fhirfactory.pegacorn.deployment.names.PegacornFHIRPlaceMDRComponentNames;
import net.fhirfactory.pegacorn.platform.restfulapi.PegacornInternalFHIRClientServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FHIRPlaceFoundationDocumentsMDRAccessor extends PegacornInternalFHIRClientServices {
    private static final Logger LOG = LoggerFactory.getLogger(FHIRPlaceFoundationDocumentsMDRAccessor.class);

    public FHIRPlaceFoundationDocumentsMDRAccessor(){
        super();
    }

    @Override
    protected Logger getLogger(){return(LOG);}

    @Inject
    private PegacornFHIRPlaceMDRComponentNames pegacornMDRComponentNames;

    @Override
    protected String specifyFHIRServerService() {
        return (pegacornMDRComponentNames.getFoundationDocumentsPegacornMDRService());
    }

    @Override
    protected String specifyFHIRServerProcessingPlant() {
        return (pegacornMDRComponentNames.getFoundationDocumentsPegacornMDRProcessingPlant());
    }

    @Override
    protected String specifyFHIRServerSubsystemName() {
        return (pegacornMDRComponentNames.getFoundationDocumentsPegacornMDRSubsystem());
    }

    @Override
    protected String specifyFHIRServerSubsystemVersion() {
        return (pegacornMDRComponentNames.getFoundationDocumentsPegacornMDRSubsystemVersion());
    }

    @Override
    protected String specifyFHIRServerServerEndpointName() {
        return (pegacornMDRComponentNames.getFoundationDocumentsPegacornMDREndpointFhirApi());
    }
}
