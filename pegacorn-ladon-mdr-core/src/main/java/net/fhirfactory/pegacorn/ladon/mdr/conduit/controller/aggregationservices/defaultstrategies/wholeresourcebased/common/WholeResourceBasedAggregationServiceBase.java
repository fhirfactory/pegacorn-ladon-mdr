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
package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.wholeresourcebased.common;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.common.DefaultResourceContentAggregationServiceBase;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitSearchResponseElement;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionTypeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;

public abstract class WholeResourceBasedAggregationServiceBase extends DefaultResourceContentAggregationServiceBase {

    //
    //
    // Default Aggregation Methods
    //
    //

    protected VirtualDBMethodOutcome defaultActionOutcomeAggregationService(VirtualDBActionTypeEnum action, List<ResourceSoTConduitActionResponse> outcomeList){
        if(outcomeList == null){
            VirtualDBMethodOutcome aggregatedOutcome = generateBadAttributeOutcome("defaultCreateActionOutcomeAggregation()", action, "Empty Outcome List!!!");
        }
        if(outcomeList.isEmpty()){
            VirtualDBMethodOutcome aggregatedOutcome = generateBadAttributeOutcome("defaultCreateActionOutcomeAggregation()", action, "Empty Outcome List!!!");
        }
        boolean hasFailure = false;
        VirtualDBMethodOutcome failedOutcome = null;
        for(ResourceSoTConduitActionResponse currentOutcome: outcomeList) {
            getLogger().info(".defaultActionOutcomeAggregationService(): currentOutcome.sourceOfTruthEndpoint --> {}", currentOutcome.getSourceOfTruthName());
//            getLogger().info(".defaultActionOutcomeAggregationService(): currentOutcome.resource --> {}", currentOutcome.getResource().getIdElement());
            if(!successfulCompletion(currentOutcome.getStatusEnum())){
                getLogger().debug(".defaultActionOutcomeAggregationService(): Exit, failed retrieval occured");
                return(currentOutcome);
            }
        }
        // Sort the List in terms of Precedence
        Collections.sort(outcomeList);
        // Now, return instance that has Precedence
        ResourceSoTConduitActionResponse outcome = outcomeList.get(0);
//        this.mapIdToIdentifier(outcome);
        if(getLogger().isTraceEnabled()){
            if(outcome.getResource() != null){
                IParser r4Parser = getFhirContextUtility().getJsonParser().setPrettyPrint(true);
                getLogger().trace(".defaultActionOutcomeAggregationService(): Selected Resource --> {}", r4Parser.encodeResourceToString(outcome.getResource()));
            } else {
                getLogger().trace(".defaultActionOutcomeAggregationService(): No resource in response!!!!");
            }
        }
        getLogger().debug(".defaultActionOutcomeAggregationService(): Exit, returning suitable candidate resource");
        return(outcome);
    }

    protected VirtualDBMethodOutcome defaultSearchOutcomeAggregationService(List<ResourceSoTConduitSearchResponseElement> searchOutcomeList){
        if(searchOutcomeList == null){
            VirtualDBMethodOutcome methodOutcome = generateBadAttributeOutcome("defaultSearchOutcomeAggregationService()", VirtualDBActionTypeEnum.SEARCH, "searchOutcomeList is null");
            return(methodOutcome);
        }
        if(searchOutcomeList.isEmpty() ){
            VirtualDBMethodOutcome methodOutcome = generateBadAttributeOutcome("defaultSearchOutcomeAggregationService()", VirtualDBActionTypeEnum.SEARCH, "searchOutcomeList is empty, no conduit has provided feedback/responses");
            return(methodOutcome);
        }
        for(ResourceSoTConduitSearchResponseElement responseElement: searchOutcomeList){
            if(responseElement.getStatusEnum() == VirtualDBActionStatusEnum.SEARCH_FAILURE){
                VirtualDBMethodOutcome outcome = createFailedSearchOutcome(responseElement.getConduitName(), responseElement.getErrorMessage());
            }
        }
        Bundle searchResult = assembleSearchResultBundle(searchOutcomeList);
        VirtualDBMethodOutcome searchOutcome = new VirtualDBMethodOutcome();
        searchOutcome.setCreated(false);
        searchOutcome.setCausalAction(VirtualDBActionTypeEnum.SEARCH);
        searchOutcome.setStatusEnum(VirtualDBActionStatusEnum.SEARCH_FINISHED);
        CodeableConcept details = new CodeableConcept();
        Coding detailsCoding = new Coding();
        detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html");
        detailsCoding.setCode("MSG_SEARCH_SUCCESFUL"); // TODO this is not a valid entry, may need to change.
        String text = "Search completed, found " + searchResult.getTotal() + " matches";
        detailsCoding.setDisplay(text);
        details.setText(text);
        details.addCoding(detailsCoding);
        OperationOutcome opOutcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
        newOutcomeComponent.setDetails(details);
        newOutcomeComponent.setCode(OperationOutcome.IssueType.INFORMATIONAL);
        newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
        opOutcome.addIssue(newOutcomeComponent);
        searchOutcome.setOperationOutcome(opOutcome);
        searchOutcome.setResource(searchResult);
        return(searchOutcome);
    }
}
