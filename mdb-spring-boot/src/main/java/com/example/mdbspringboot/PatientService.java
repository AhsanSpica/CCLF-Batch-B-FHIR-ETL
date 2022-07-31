package com.example.mdbspringboot;

import org.hl7.fhir.r5.model.Patient;
//import com.example.mdbspringboot.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.*;



import java.util.Optional;

@Service
//@RequiredArgsConstructor
@Transactional
public class PatientService {
    private final PatientRepo patientRepo;
    @Autowired
   public PatientService(PatientRepo patientRepo) {

        this.patientRepo = patientRepo;
    }

    public void updatePatient(Patient patient) {
        patientRepo.save(patient);
    }

    public void addPatient(Patient patient) {
       try{ patientRepo.insert(patient);
    }
       catch(Exception ex)
       {
           String.format("Exception: "+ex.getCause()+", the exception msg: "+ex.getMessage());
       }
    }

    public void deletePatient(String Pid) {
        try{ patientRepo.deleteById(Pid);}
        catch(Exception ex)
        {
            String.format("Exception: "+ex.getCause()+", the exception msg: "+ex.getMessage());
        }
    }

    public Optional<Patient> getPatient(String Pid) {
        Optional<Patient> patient = Optional.of(new Patient());
        try{
            patient = patientRepo.findById(Pid);
           }

       // catch(DuplicateKeyException dexp) {
        catch(Exception ex) {

            String.format("Exception: "+ex.getCause()+", the exception msg: "+ex.getMessage());
        }

        return patient;
    }
    // Here is where we place and connect patient repository
 //   public void addPatient(Patient patient) {patientRepository.insert(patient);}

}