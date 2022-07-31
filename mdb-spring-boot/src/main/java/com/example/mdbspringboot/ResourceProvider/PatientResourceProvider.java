package com.example.mdbspringboot.ResourceProvider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.example.mdbspringboot.PatFHIRRepo;
import com.fasterxml.jackson.annotation.JsonAnySetter;
//import com.sun.tracing.ProviderName;
import org.hl7.fhir.dstu2.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.PostUpdate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@ProviderName("patientProvider")
public class PatientResourceProvider implements IResourceProvider {

    	@Autowired
        PatFHIRRepo patientRepository;

    public PatientResourceProvider() {
        insertdummypatient();
    }
    private void insertdummypatient() {
        Patient patient = new Patient();

        patient.setId("1");

        patient.addIdentifier().setSystem("http://optum.com/MRNs").setValue("007");

        patient.addName().setFamily("Chakravarty").addGiven("Mithun").addGiven("A");

        patient.addAddress().addLine("Address Line 1");

        patient.addAddress().setCity("Mumbai");

        patient.addAddress().setCountry("India");

        patient.addTelecom().setValue("111-111-1111");

        patientRepository.save(patient);
    }

    private Map<String, Patient> patientMap = new HashMap<String, Patient>();


    @Override

    public Class<? extends IBaseResource> getResourceType() {

        return Patient.class;

    }

    @GetMapping("/test")
    public ResponseEntity testPatient() {
        return ResponseEntity.ok("Test ok reached Patient controller");
    }


        @PostUpdate
        @RequestMapping("/save")
      public Patient save (@RequestBody Patient patient)
      {
          patientRepository.insert(patient);

          return patient;
      }

    @Search
    public List<Patient> search(@RequiredParam(name = Patient.SP_FAMILY) StringParam theParam) {

        List<Patient> patients = new ArrayList<Patient>();

        patients = this.searchByFamilyName(theParam.getValue());

        return patients;

    }

    @Read()

    public Patient read(@IdParam IdType theId) {

        loadDummyPatients();

        Patient retVal = patientMap.get(theId.getIdPart());

        if (retVal == null) {

            throw new ResourceNotFoundException(theId);

        }

        return retVal;

    }



    private List<Patient> searchByFamilyName(String familyName){

        List<Patient> retPatients;

        loadDummyPatients();

        // Encode the output, including the narrative - see below

        // Loop through the patients looking for matches

        retPatients = patientMap.values()

                .stream()

                .filter(next -> familyName.toLowerCase().equals(next.getNameFirstRep().getFamily().toLowerCase()))

                .collect(Collectors.toList());

        return retPatients;

    }




    private void loadDummyPatients() {

        Patient patient = new Patient();


        patient.setId("1");

        patient.addIdentifier().setSystem("http://optum.com/MRNs").setValue("007");

        patient.addName().setFamily("Chakravarty").addGiven("Mithun").addGiven("A");

        patient.addAddress().addLine("Address Line 1");

        patient.addAddress().setCity("Mumbai");

        patient.addAddress().setCountry("India");

        patient.addTelecom().setValue("111-111-1111");




        this.patientMap.put("1", patient);


        for (int i = 2; i < 5; i++) {

            patient = new Patient();

            patient.setId(Integer.toString(i));

            patient.addIdentifier().setSystem("http://optum.com/MRNs").setValue("007" + i);

            patient.addName().setFamily("Bond" + i).addGiven("James").addGiven("J");

            patient.addAddress().addLine("House Line " + i);

            patient.addAddress().setCity("Your City");

            patient.addAddress().setCountry("USA");

            this.patientMap.put(Integer.toString(i), patient);

        }

    }


}
