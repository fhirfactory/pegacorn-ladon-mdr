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
package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.common;

import net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.common.ResourceContentAggregationServiceBase;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.*;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionStatusEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBActionTypeEnum;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.operations.VirtualDBMethodOutcome;
import org.hl7.fhir.r4.model.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public abstract class DefaultResourceContentAggregationServiceBase extends ResourceContentAggregationServiceBase {



    //
    // Shared Methods
    //

    protected VirtualDBMethodOutcome generateBadAttributeOutcome(String method, VirtualDBActionTypeEnum action, String text){
        VirtualDBMethodOutcome vdbOutcome = new VirtualDBMethodOutcome();
        vdbOutcome.setCreated(false);
        vdbOutcome.setCausalAction(action);
        switch(action){
            case CREATE:
                vdbOutcome.setStatusEnum(VirtualDBActionStatusEnum.CREATION_FAILURE);
                break;
            case REVIEW:
                vdbOutcome.setStatusEnum(VirtualDBActionStatusEnum.REVIEW_FAILURE);
                break;
            case UPDATE:
                vdbOutcome.setStatusEnum(VirtualDBActionStatusEnum.UPDATE_FAILURE);
                break;
            case DELETE:
                vdbOutcome.setStatusEnum(VirtualDBActionStatusEnum.DELETE_FAILURE);
                break;
            case SEARCH:
                vdbOutcome.setStatusEnum(VirtualDBActionStatusEnum.SEARCH_FAILURE);
                break;
            case SYNC:
                vdbOutcome.setStatusEnum(VirtualDBActionStatusEnum.SYNC_FAILURE);
        }
        CodeableConcept details = new CodeableConcept();
        Coding detailsCoding = new Coding();
        detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html");
        detailsCoding.setCode("MSG_PARAM_INVALID");
        detailsCoding.setDisplay(text);
        details.setText(text);
        details.addCoding(detailsCoding);
        OperationOutcome opOutcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
        newOutcomeComponent.setDiagnostics(getAggregationServiceName() + "::" + method);
        newOutcomeComponent.setDetails(details);
        newOutcomeComponent.setCode(OperationOutcome.IssueType.INVALID);
        newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        opOutcome.addIssue(newOutcomeComponent);
        vdbOutcome.setOperationOutcome(opOutcome);
        return(vdbOutcome);
    }

    protected VirtualDBMethodOutcome createFailedSearchOutcome(String conduitName, String failedMethodName){
        VirtualDBMethodOutcome failedSearchOutcome = new VirtualDBMethodOutcome();
        failedSearchOutcome.setCreated(false);
        failedSearchOutcome.setCausalAction(VirtualDBActionTypeEnum.SEARCH);
        failedSearchOutcome.setStatusEnum(VirtualDBActionStatusEnum.SEARCH_FAILURE);
        CodeableConcept details = new CodeableConcept();
        Coding detailsCoding = new Coding();
        detailsCoding.setSystem("https://www.hl7.org/fhir/codesystem-operation-outcome.html");
        detailsCoding.setCode("MSG_PARAM_INVALID");
        String text = "Search failed in Source-of-Truth/Master-Data-Repository Conduit --> " + conduitName;
        detailsCoding.setDisplay(text);
        details.setText(text);
        details.addCoding(detailsCoding);
        OperationOutcome opOutcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent newOutcomeComponent = new OperationOutcome.OperationOutcomeIssueComponent();
        newOutcomeComponent.setDiagnostics(getAggregationServiceName() + "::" + failedMethodName);
        newOutcomeComponent.setDetails(details);
        newOutcomeComponent.setCode(OperationOutcome.IssueType.INVALID);
        newOutcomeComponent.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        opOutcome.addIssue(newOutcomeComponent);
        failedSearchOutcome.setOperationOutcome(opOutcome);
        return(failedSearchOutcome);
    }

    protected boolean successfulCompletion(VirtualDBActionStatusEnum status){
        switch(status){
            case CREATION_FINISH:
            case REVIEW_FINISH:
            case DELETE_FINISH:
            case UPDATE_FINISH:
            case SYNC_FINISHED:
            case SEARCH_FINISHED:
                return(true);
            default:
                return(false);
        }
    }

    protected Bundle assembleSearchResultBundle(List<ResourceSoTConduitSearchResponseElement> searchOutcomeList){
        Bundle searchResultBundle = new Bundle();
        searchResultBundle.setType(Bundle.BundleType.SEARCHSET);
        searchResultBundle.setTimestamp(Date.from(Instant.now()));
        int entryCount = 0;
        for(ResourceSoTConduitSearchResponseElement searchResponse: searchOutcomeList){
            for(Resource resource: searchResponse.getResources()){
                Bundle.BundleEntryComponent newBundleEntry = new Bundle.BundleEntryComponent();
                newBundleEntry.setResource(resource);
                Bundle.BundleEntrySearchComponent searchComponent = new Bundle.BundleEntrySearchComponent();
                searchComponent.setMode(Bundle.SearchEntryMode.MATCH);
                searchComponent.setScore(1);
                newBundleEntry.setSearch(searchComponent);
                searchResultBundle.addEntry(newBundleEntry);
                entryCount += 1;
            }
        }
        searchResultBundle.setTotal(entryCount);
        return(searchResultBundle);
    }
}
