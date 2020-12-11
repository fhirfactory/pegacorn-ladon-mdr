package net.fhirfactory.pegacorn.ladon.mdr.fhirplace;

import net.fhirfactory.pegacorn.ladon.mdr.fhirplace.conduits.*;
import net.fhirfactory.pegacorn.processingplatform.common.StandardWorkshop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FHIRPlaceMDRWorkshop  extends StandardWorkshop {
    private static final Logger LOG = LoggerFactory.getLogger(FHIRPlaceMDRWorkshop.class);

    private static String FHIRPLACE_MDR_CONDUIT_WORKSHOP = "FHIRPlaceMDR-Conduit";
    private static String FHIRPLACE_MDR_CONDUIT_WORKSHOP_VERSION = "1.0.0";

    @Inject
    private DocumentReferenceSoTResourceConduit documentReferenceSoTResourceConduit;

    @Inject
    private CareTeamSoTResourceConduit careTeamSoTResourceConduit;

    @Inject
    private CommunicationRequestSoTResourceConduit communicationRequestSoTResourceConduit;

    @Inject
    CommunicationSoTResourceConduit communicationSoTResourceConduit;

//    @Inject
//    EncounterSoTResourceConduit encounterSoTResourceConduit;

    @Inject
    EndpointSoTResourceConduit endpointSoTResourceConduit;

    @Inject
    GroupSoTResourceConduit groupSoTResourceConduit;

//    @Inject
//    HealthcareServiceSoTResourceConduit healthcareServiceSoTResourceConduit;

//    @Inject
//    LocationSoTResourceConduit locationSoTResourceConduit;

//    @Inject
//    OrganizationSoTResourceConduit organizationSoTResourceConduit;

//    @Inject
//    PatientSoTResourceConduit patientSoTResourceConduit;

//    @Inject
//    PractitionerRoleSoTResourceConduit practitionerRoleSoTResourceConduit;

//    @Inject
//    PractitionerSoTResourceConduit practitionerSoTResourceConduit;

//    @Inject
//    ProcedureSoTResourceConduit procedureSoTResourceConduit;

    @Inject
    TaskSoTResourceConduit taskSoTResourceConduit;

    @Inject
    ValueSetSoTResourceConduit valueSetSoTResourceConduit;

    protected void invokePostConstructInitialisation(){
        getLogger().debug(".initialise(): Entry");
        getLogger().trace(".initialise(): initialising DocumentReferenceSoTResourceConduit");
        documentReferenceSoTResourceConduit.initialise();
        getLogger().trace(".initialise(): initialising CareTeamSoTResourceConduit");
        careTeamSoTResourceConduit.initialise();
        getLogger().trace(".initialise(): initialising CommunicationRequestSoTResourceConduit");
        communicationRequestSoTResourceConduit.initialise();
        getLogger().trace(".initialise(): initialising CommunicationSoTResourceConduit");
        communicationSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising EncounterSoTResourceConduit");
//        encounterSoTResourceConduit.initialise();
        getLogger().trace(".initialise(): initialising EndpointSoTResourceConduit");
        endpointSoTResourceConduit.initialise();
        getLogger().trace(".initialise(): initialising GroupSoTResourceConduit");
        groupSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising HealthcareServiceSoTResourceConduit");
//        healthcareServiceSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising LocationSoTResourceConduit");
//        locationSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising OrganizationSoTResourceConduit");
//        organizationSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising PatientSoTResourceConduit");
//        patientSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising PractitionerRoleSoTResourceConduit");
//        practitionerRoleSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising PractitionerSoTResourceConduit");
//        practitionerSoTResourceConduit.initialise();
//        getLogger().trace(".initialise(): initialising ProcedureSoTResourceConduit");
//        procedureSoTResourceConduit.initialise();
        getLogger().trace(".initialise(): initialising TaskSoTResourceConduit");
        taskSoTResourceConduit.initialise();
        getLogger().trace(".initialise(): initialising ValueSetSoTResourceConduit");
        valueSetSoTResourceConduit.initialise();
    }

    @Override
    protected Logger getLogger() {
        return (LOG);
    }

    @Override
    protected String specifyWorkshopName() {
        return (FHIRPLACE_MDR_CONDUIT_WORKSHOP);
    }

    @Override
    protected String specifyWorkshopVersion() {
        return (FHIRPLACE_MDR_CONDUIT_WORKSHOP_VERSION);
    }
}
