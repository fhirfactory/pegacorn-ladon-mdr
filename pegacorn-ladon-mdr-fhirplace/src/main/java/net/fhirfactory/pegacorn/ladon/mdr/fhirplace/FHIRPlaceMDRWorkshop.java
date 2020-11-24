package net.fhirfactory.pegacorn.ladon.mdr.fhirplace;

import net.fhirfactory.pegacorn.ladon.mdr.fhirplace.conduits.DocumentReferenceSoTResourceConduit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FHIRPlaceMDRWorkshop {

    @Inject
    private DocumentReferenceSoTResourceConduit documentReferenceSoTResourceConduit;



    @PostConstruct
    public void initialise(){
        documentReferenceSoTResourceConduit.initialise();
    }

}
