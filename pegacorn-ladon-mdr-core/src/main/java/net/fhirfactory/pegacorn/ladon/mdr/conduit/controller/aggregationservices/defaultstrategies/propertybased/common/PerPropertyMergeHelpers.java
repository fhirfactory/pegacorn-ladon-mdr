package net.fhirfactory.pegacorn.ladon.mdr.conduit.controller.aggregationservices.defaultstrategies.propertybased.common;

import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.ResourceSoTConduitActionResponse;
import net.fhirfactory.pegacorn.ladon.model.virtualdb.mdr.SoTConduitGradeEnum;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class PerPropertyMergeHelpers {
    private static final Logger LOG = LoggerFactory.getLogger(PerPropertyMergeHelpers.class);

    // Identifier Merge

    /**
     * This function merges the (set of) Identifiers from the baseResponse and additiveResponse elements. If it
     * detects that an Identifier in one response has the same (Use, Type, System) and are both "current" it will
     * apply the precedence algorithm to the Property to ascertain which to keep.
     *
     * @param baseResponse     The "base" response to be enhanced
     * @param additiveResponse The "additive" response to be used to extract and add content to the "base"
     */
    public void mergeIdentifiers(ResourceSoTConduitActionResponse baseResponse, List<Identifier> baseIdentifierSet, ResourceSoTConduitActionResponse additiveResponse, List<Identifier> additiveIdentifierSet) {

        if (baseIdentifierSet.isEmpty()) {
            baseIdentifierSet.addAll(additiveIdentifierSet);
        } else {
            for (Identifier currentBaseResourceIdentifier : baseIdentifierSet) {
                for (Identifier currentAdditiveResourceIdentifier : additiveIdentifierSet) {
                    boolean sameUse = currentBaseResourceIdentifier.getUse().equals(currentAdditiveResourceIdentifier.getUse());
                    boolean sameCoding = currentBaseResourceIdentifier.getType().equalsDeep(currentAdditiveResourceIdentifier.getType());
                    boolean sameSystem = currentBaseResourceIdentifier.getSystem().equals(currentAdditiveResourceIdentifier.getSystem());
                    boolean baseIdentifierIsCurrent = isAttributeCurrent(currentBaseResourceIdentifier.getPeriod());
                    boolean additiveIdentifierIsCurrent = isAttributeCurrent(currentAdditiveResourceIdentifier.getPeriod());
                    if (sameUse && sameCoding && sameSystem && baseIdentifierIsCurrent && additiveIdentifierIsCurrent) {
                        if (!baseHasPrecedence("identifier", baseResponse, additiveResponse)) {
                            baseIdentifierSet.remove(currentBaseResourceIdentifier);
                            baseIdentifierSet.add(currentAdditiveResourceIdentifier);
                        }
                    } else {
                        baseIdentifierSet.add(currentAdditiveResourceIdentifier);
                    }
                }
            }
        }
    }

    /**
     * This function merges the (set of) References from the baseResponse and additiveResponse elements. If it
     * detects that a Reference in one response has the same (type, reference, identifier::use, identifier::type, identifier::system) it will
     * apply the precedence algorithm to the Property to ascertain which to keep.
     *
     * @param baseResponse     The "base" response to be enhanced
     * @param additiveResponse The "additive" response to be used to extract and add content to the "base"
     */
    public void mergeReferences(ResourceSoTConduitActionResponse baseResponse, List<Reference> baseReferenceSet, ResourceSoTConduitActionResponse additiveResponse, List<Reference> additiveReferenceSet) {

        if (baseReferenceSet.isEmpty()) {
            baseReferenceSet.addAll(additiveReferenceSet);
        } else {
            for (Reference currentBaseResourceReference : baseReferenceSet) {
                for (Reference currentAdditiveResourceReference : additiveReferenceSet) {
                    boolean sameReference = currentBaseResourceReference.getReference().contentEquals(currentAdditiveResourceReference.getReference());
                    boolean sameType = currentBaseResourceReference.getType().equals(currentAdditiveResourceReference.getType());
                    boolean sameIdentifierUse = currentBaseResourceReference.getIdentifier().getUse().equals(currentAdditiveResourceReference.getIdentifier().getUse());
                    boolean sameIdentifierType = currentAdditiveResourceReference.getIdentifier().getType().equalsDeep(currentAdditiveResourceReference.getIdentifier().getType());
                    boolean sameIdentifierSystem = currentAdditiveResourceReference.getIdentifier().getSystem().equals(currentAdditiveResourceReference.getIdentifier().getSystem());
                    if (sameReference && sameType && sameIdentifierUse && sameIdentifierType && sameIdentifierSystem) {
                        if (!baseHasPrecedence("identifier", baseResponse, additiveResponse)) {
                            baseReferenceSet.remove(currentBaseResourceReference);
                            baseReferenceSet.add(currentAdditiveResourceReference);
                        }
                    } else {
                        baseReferenceSet.add(currentAdditiveResourceReference);
                    }
                }
            }
        }
    }

    // AttributeIsCurrent

    /**
     * This function tests the associated Period to see if the attribte/resource to which it is associated
     * is currently "active". Note: Abscense of a Start or End date is treated as unbounded values (i.e.
     * the Period became "active" at the beginning of time, or it will expire at the end of time).
     *
     * @param attributeActivePeriod The period to be tested against Instant.now()
     * @return Returns true if the Start value is before Instant.now() (or if Start is null) AND End is after
     * Instant.now (or if End is null).
     */
    public boolean isAttributeCurrent(Period attributeActivePeriod) {
        if (attributeActivePeriod == null) {
            return (true);
        }
        boolean itHasNotExpired = true;
        if (attributeActivePeriod.hasEnd()) {
            if (attributeActivePeriod.getEnd().after(Date.from(Instant.now()))) {
                itHasNotExpired = false;
            }
        }
        boolean itHasReachedActiveTime = true;
        if (attributeActivePeriod.hasStart()) {
            if (attributeActivePeriod.getStart().after(Date.from(Instant.now()))) {
                itHasReachedActiveTime = false;
            }
        }
        return (itHasNotExpired && itHasReachedActiveTime);
    }

    // HumanName Merge

    /**
     * This function merges the (set of) HumanNames from the baseResponse and additiveResponse elements. If it
     * detects that a HumanName in one response has the same (Use, FamilyName, GivenNameSet) and are both "current" it will
     * apply the precedence algorithm to the Property to ascertain which to keep.
     *
     * @param baseResponse     The "base" response to be enhanced
     * @param additiveResponse The "additive" response to be used to extract and add content to the "base"
     */
    public void mergeHumanNames(ResourceSoTConduitActionResponse baseResponse, List<HumanName> baseHumanNameSet, ResourceSoTConduitActionResponse additiveResponse, List<HumanName> additiveHumanNameSet) {
        if (baseHumanNameSet.isEmpty()) {
            baseHumanNameSet.addAll(additiveHumanNameSet);
        } else {
            for (HumanName currentBaseHumanName : baseHumanNameSet) {
                for (HumanName currentAdditiveHumanName : additiveHumanNameSet) {
                    boolean sameUse = currentBaseHumanName.getUse().equals(currentAdditiveHumanName.getUse());
                    boolean sameFamilyName = currentBaseHumanName.getFamily().contentEquals(currentAdditiveHumanName.getFamily());
                    boolean sameGivenName = currentBaseHumanName.getGiven().containsAll(currentAdditiveHumanName.getGiven());
                    boolean baseNameIsCurrent = isAttributeCurrent(currentBaseHumanName.getPeriod());
                    boolean additiveNameIsCurrent = isAttributeCurrent(currentAdditiveHumanName.getPeriod());
                    if (sameUse && sameFamilyName && sameGivenName && baseNameIsCurrent && additiveNameIsCurrent) {
                        if (!baseHasPrecedence("name", baseResponse, additiveResponse)) {
                            baseHumanNameSet.remove(currentBaseHumanName);
                            baseHumanNameSet.add(currentAdditiveHumanName);
                        }
                    } else {
                        baseHumanNameSet.add(currentAdditiveHumanName);
                    }
                }
            }
        }
    }

    // HumanName Merge

    /**
     * This function merges the (set of) ContactPoint from the baseResponse and additiveResponse elements. If it
     * detects that a HumanName in one response has the same (Use, System) and are both "current" it will
     * apply the precedence algorithm to the Property to ascertain which to keep.
     *
     * @param baseResponse     The "base" response to be enhanced
     * @param additiveResponse The "additive" response to be used to extract and add content to the "base"
     */
    public void mergeContactPoints(ResourceSoTConduitActionResponse baseResponse, List<ContactPoint> baseContactPointSet, ResourceSoTConduitActionResponse additiveResponse, List<ContactPoint> additiveContactPointSet) {
        if (baseContactPointSet.isEmpty()) {
            baseContactPointSet.addAll(additiveContactPointSet);
        } else {
            for (ContactPoint currentBaseContactPoint : baseContactPointSet) {
                for (ContactPoint currentAdditiveContactPoint : additiveContactPointSet) {
                    boolean sameUse = currentBaseContactPoint.getUse().equals(currentAdditiveContactPoint.getUse());
                    boolean sameSystem = currentBaseContactPoint.getSystem().equals(currentAdditiveContactPoint.getSystem());
                    boolean baseContactPointIsCurrent = isAttributeCurrent(currentBaseContactPoint.getPeriod());
                    boolean additiveContactPointIsCurrent = isAttributeCurrent(currentAdditiveContactPoint.getPeriod());
                    if (sameUse && sameSystem && baseContactPointIsCurrent && additiveContactPointIsCurrent) {
                        if (!baseHasPrecedence("telecom", baseResponse, additiveResponse)) {
                            baseContactPointSet.remove(currentBaseContactPoint);
                            baseContactPointSet.add(currentAdditiveContactPoint);
                        }
                    } else {
                        baseContactPointSet.add(currentAdditiveContactPoint);
                    }
                }
            }
        }
    }

    // Address Merge

    /**
     * This function merges the (set of) Address from the baseResponse and additiveResponse elements. If it
     * detects that a Address in one response has the same (Use, Type) and are both "current" it will
     * apply the precedence algorithm to the Property to ascertain which to keep.
     *
     * @param baseResponse     The "base" response to be enhanced
     * @param additiveResponse The "additive" response to be used to extract and add content to the "base"
     */
    public void mergeAddress(ResourceSoTConduitActionResponse baseResponse, List<Address> baseAddressSet, ResourceSoTConduitActionResponse additiveResponse, List<Address> additiveAddressSet) {
        if (baseAddressSet.isEmpty()) {
            baseAddressSet.addAll(additiveAddressSet);
        } else {
            for (Address currentBaseAddress : baseAddressSet) {
                for (Address currentAdditiveAddress : additiveAddressSet) {
                    boolean sameUse = currentBaseAddress.getUse().equals(currentAdditiveAddress.getUse());
                    boolean sameType = currentBaseAddress.getType().equals(currentAdditiveAddress.getType());
                    boolean baseContactPointIsCurrent = isAttributeCurrent(currentBaseAddress.getPeriod());
                    boolean additiveContactPointIsCurrent = isAttributeCurrent(currentAdditiveAddress.getPeriod());
                    if (sameUse && sameType && baseContactPointIsCurrent && additiveContactPointIsCurrent) {
                        if (!baseHasPrecedence("telecom", baseResponse, additiveResponse)) {
                            baseAddressSet.remove(currentBaseAddress);
                            baseAddressSet.add(currentAdditiveAddress);
                        }
                    } else {
                        baseAddressSet.add(currentAdditiveAddress);
                    }
                }
            }
        }
    }

    // Attachments Merge

    /**
     * This function merges the (set of) Attachments from the baseResponse and additiveResponse elements. If it
     * detects that an Attachment in one response has the same (contentType, language, url, title, creation) and are both "current" it will
     * apply the precedence algorithm to the Property to ascertain which to keep.
     *
     * @param propertyName  The "property" name to which these Attachments are associated within the Resource
     * @param baseResponse     The "base" response to be enhanced
     * @param additiveResponse The "additive" response to be used to extract and add content to the "base"
     */
    public void mergeAttachments(String propertyName, ResourceSoTConduitActionResponse baseResponse, List<Attachment> baseAttachmentSet, ResourceSoTConduitActionResponse additiveResponse, List<Attachment> additiveAttachmentSet) {
        if (baseAttachmentSet.isEmpty()) {
            baseAttachmentSet.addAll(additiveAttachmentSet);
        } else {
            for (Attachment currentBaseAttachment : baseAttachmentSet) {
                for (Attachment currentAdditiveAttachment : additiveAttachmentSet) {
                    boolean sameContentType = false;
                    if (currentBaseAttachment.hasContentType() && currentAdditiveAttachment.hasContentType()) {
                        sameContentType = currentBaseAttachment.getContentType().equals(currentAdditiveAttachment.getContentType());
                    }
                    if (!currentBaseAttachment.hasContentType() && !currentAdditiveAttachment.hasContentType()) {
                        sameContentType = true;
                    }
                    boolean sameLanguageCode = false;
                    if (currentBaseAttachment.hasLanguage() && currentAdditiveAttachment.hasLanguage()) {
                        sameLanguageCode = currentBaseAttachment.getLanguage().equals(currentAdditiveAttachment.getLanguage());
                    }
                    if (!currentBaseAttachment.hasLanguage() && !currentAdditiveAttachment.hasLanguage()) {
                        sameLanguageCode = true;
                    }
                    boolean sameURL = false;
                    if (currentBaseAttachment.hasUrl() && currentAdditiveAttachment.hasUrl()) {
                        sameURL = currentBaseAttachment.getUrl().equals(currentAdditiveAttachment.getUrl());
                    }
                    if (!currentBaseAttachment.hasUrl() && !currentAdditiveAttachment.hasUrl()) {
                        sameURL = true;
                    }
                    boolean sameTitle = false;
                    if (currentBaseAttachment.hasTitle() && currentAdditiveAttachment.hasTitle()) {
                        sameTitle = currentBaseAttachment.getTitle().contentEquals(currentAdditiveAttachment.getTitle());
                    }
                    if (!currentBaseAttachment.hasTitle() && !currentAdditiveAttachment.hasTitle()) {
                        sameTitle = true;
                    }
                    boolean sameCreationDate = false;
                    if (currentBaseAttachment.hasCreation() && currentAdditiveAttachment.hasCreation()) {
                        sameCreationDate = currentBaseAttachment.getCreation().compareTo(currentAdditiveAttachment.getCreation()) == 0;
                    }
                    if (!currentBaseAttachment.hasTitle() && !currentAdditiveAttachment.hasTitle()) {
                        sameCreationDate = true;
                    }
                    if (sameContentType && sameLanguageCode && sameURL && sameTitle && sameCreationDate) {
                        if (!baseHasPrecedence(propertyName, baseResponse, additiveResponse)) {
                            baseAttachmentSet.remove(currentBaseAttachment);
                            baseAttachmentSet.add(currentAdditiveAttachment);
                        }
                    } else {
                        baseAttachmentSet.add(currentAdditiveAttachment);
                    }
                }
            }
        }
    }

    // CodeableConcept (Additive) Merge

    /**
     * This function merges the (set of) CodeableConcepts from the baseResponse and additiveResponse elements. If it
     * detects that an CodeableConcept in one response has the same (coding, text) it will
     * add only one instance to the resulting "base" object
     *
     * @param baseCodeableConceptSet     The "base" CodeableConcept List to be enhanced
     * @param additiveCodeableConceptSet The "additive" CodeableConcept List to be used be examined and add content to the "base"
     */
    public void mergeCodeableConcept(List<CodeableConcept> baseCodeableConceptSet, List<CodeableConcept> additiveCodeableConceptSet) {
        if (baseCodeableConceptSet.isEmpty()) {
            baseCodeableConceptSet.addAll(additiveCodeableConceptSet);
            return;
        }
        if (additiveCodeableConceptSet.isEmpty()) {
            return;
        }
        for (CodeableConcept currentAdditiveCodeableConcept : baseCodeableConceptSet) {
            boolean isNotPresentInBase = true;
            for (CodeableConcept currentBaseCodeableConcept : additiveCodeableConceptSet) {
                if(currentBaseCodeableConcept.equalsDeep(currentAdditiveCodeableConcept)){
                    isNotPresentInBase = false;
                    break;
                }
            }
            if(isNotPresentInBase){
                baseCodeableConceptSet.add(currentAdditiveCodeableConcept);
            }
        }
    }

    //
    // Helper Methods
    //

    public boolean baseHasPrecedence(String propertyName, ResourceSoTConduitActionResponse base, ResourceSoTConduitActionResponse other) {
        LOG.debug(".baseHasPrecedence(): Entry, propertyName --> {}, base --> {}, other --> {}", propertyName, base, other);
        if (base == null && other == null) {
            return (true);
        }
        if (base == null) {
            return (false);
        }
        if (other == null) {
            return (true);
        }
        if (base.getSoTGrade() == SoTConduitGradeEnum.AUTHORITATIVE) {
            if (!base.getImmutableAttributes().contains(propertyName)) {
                if (other.getImmutableAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAuthoritativeAttributes().contains(propertyName)) {
                if (other.getAuthoritativeAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getInformationalAttributes().contains(propertyName)) {
                if (other.getInformationalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAnecdotalAttributes().contains(propertyName)) {
                if (other.getAnecdotalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            return (true);
        }
        if (other.getSoTGrade() == SoTConduitGradeEnum.AUTHORITATIVE) {
            if (!other.getImmutableAttributes().contains(propertyName)) {
                if (base.getImmutableAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAuthoritativeAttributes().contains(propertyName)) {
                if (base.getAuthoritativeAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getInformationalAttributes().contains(propertyName)) {
                if (base.getInformationalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAnecdotalAttributes().contains(propertyName)) {
                if (base.getAnecdotalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            return (false);
        }
        if (base.getSoTGrade() == SoTConduitGradeEnum.PARTIALLY_AUTHORITATIVE) {
            if (!base.getImmutableAttributes().contains(propertyName)) {
                if (other.getImmutableAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAuthoritativeAttributes().contains(propertyName)) {
                if (other.getAuthoritativeAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getInformationalAttributes().contains(propertyName)) {
                if (other.getInformationalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAnecdotalAttributes().contains(propertyName)) {
                if (other.getAnecdotalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            return (true);
        }
        if (other.getSoTGrade() == SoTConduitGradeEnum.PARTIALLY_AUTHORITATIVE) {
            if (!other.getImmutableAttributes().contains(propertyName)) {
                if (base.getImmutableAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAuthoritativeAttributes().contains(propertyName)) {
                if (base.getAuthoritativeAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getInformationalAttributes().contains(propertyName)) {
                if (base.getInformationalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAnecdotalAttributes().contains(propertyName)) {
                if (base.getAnecdotalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            return (false);
        }
        if (base.getSoTGrade() == SoTConduitGradeEnum.INFORMATIVE) {
            if (!base.getImmutableAttributes().contains(propertyName)) {
                if (other.getImmutableAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAuthoritativeAttributes().contains(propertyName)) {
                if (other.getAuthoritativeAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getInformationalAttributes().contains(propertyName)) {
                if (other.getInformationalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAnecdotalAttributes().contains(propertyName)) {
                if (other.getAnecdotalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            return (true);
        }
        if (other.getSoTGrade() == SoTConduitGradeEnum.INFORMATIVE) {
            if (!other.getImmutableAttributes().contains(propertyName)) {
                if (base.getImmutableAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAuthoritativeAttributes().contains(propertyName)) {
                if (base.getAuthoritativeAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getInformationalAttributes().contains(propertyName)) {
                if (base.getInformationalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAnecdotalAttributes().contains(propertyName)) {
                if (base.getAnecdotalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            return (false);
        }
        if (base.getSoTGrade() == SoTConduitGradeEnum.PARTIALLY_INFORMATIVE) {
            if (!base.getImmutableAttributes().contains(propertyName)) {
                if (other.getImmutableAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAuthoritativeAttributes().contains(propertyName)) {
                if (other.getAuthoritativeAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getInformationalAttributes().contains(propertyName)) {
                if (other.getInformationalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAnecdotalAttributes().contains(propertyName)) {
                if (other.getAnecdotalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            return (true);
        }
        if (other.getSoTGrade() == SoTConduitGradeEnum.PARTIALLY_INFORMATIVE) {
            if (!other.getImmutableAttributes().contains(propertyName)) {
                if (base.getImmutableAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAuthoritativeAttributes().contains(propertyName)) {
                if (base.getAuthoritativeAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getInformationalAttributes().contains(propertyName)) {
                if (base.getInformationalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            if (!other.getAnecdotalAttributes().contains(propertyName)) {
                if (base.getAnecdotalAttributes().contains(propertyName)) {
                    return (true);
                }
            }
            return (false);
        }
        if (base.getSoTGrade() == SoTConduitGradeEnum.ANECDOTAL) {
            if (!base.getImmutableAttributes().contains(propertyName)) {
                if (other.getImmutableAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAuthoritativeAttributes().contains(propertyName)) {
                if (other.getAuthoritativeAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getInformationalAttributes().contains(propertyName)) {
                if (other.getInformationalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            if (!base.getAnecdotalAttributes().contains(propertyName)) {
                if (other.getAnecdotalAttributes().contains(propertyName)) {
                    return (false);
                }
            }
            return (true);
        }
        return (false);
    }



}
