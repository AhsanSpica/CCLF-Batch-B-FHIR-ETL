package com.example.mdbspringboot;
import org.hl7.fhir.r5.model.Patient;
//import com.example.mdbspringboot.model.Patient;
//import ca.uhn.fhir.rest.annotation.IdParam;
//import ca.uhn.fhir.rest.annotation.Read;
//import ca.uhn.fhir.rest.annotation.RequiredParam;
//import ca.uhn.fhir.rest.annotation.Search;
//import ca.uhn.fhir.rest.param.StringParam;
//import ca.uhn.fhir.rest.server.IResourceProvider;
//import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
//import org.hl7.fhir.dstu2.model.IdType;
//import org.hl7.fhir.instance.model.api.IBaseResource;

//import org.hl7.fhir.r5.model.Patient;

import org.springframework.data.mongodb.repository.MongoRepository;

    public interface PatientRepo extends MongoRepository<Patient, String> {
    }

