package com.example.mdbspringboot;

import org.hl7.fhir.r5.model.Patient;
//import com.example.mdbspringboot.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PatFHIRRepo  extends MongoRepository<Patient, String> {
}
