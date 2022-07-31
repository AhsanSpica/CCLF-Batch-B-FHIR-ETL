package com.example.mdbspringboot;

import com.example.mdbspringboot.model.Encounter;
import com.example.mdbspringboot.model.Patient;
import com.example.mdbspringboot.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
//@RequiredArgsConstructor
@Transactional
public class PractitionerService {

    private final PractitionerRepo practitionerRepo;
    @Autowired
    public PractitionerService(PractitionerRepo practitionerRepo) {

        this.practitionerRepo = practitionerRepo;
    }
    public void updatePractitioner(Practitioner practitioner) {
        practitionerRepo.save(practitioner);
    }

    public void addPractitioner(Practitioner practitioner) {

       try{ practitionerRepo.insert(practitioner);}
       catch(Exception ex){

           String.format("Exception: "+ex.getCause()+", the exception msg: "+ex.getMessage());
       }
        // Here is where we place and connect practitioner repository
    }

    public void deletePractitioner(String id)
    {
        practitionerRepo.deleteById(id);
    }


    public Optional<Practitioner> getPractitioner(String Pracid) {
        Optional<Practitioner> practitioner = Optional.of(new Practitioner());

        try{
            practitioner = practitionerRepo.findById(Pracid);

        }
        catch(Exception ex){

            String.format("Exception: "+ex.getCause()+", the exception msg: "+ex.getMessage());
        }
        return practitioner;
    }

//    public Practitioner getPractitioner(String Pracid) {
//        return practitionerRepo.findById(Pracid)
//                .orElseThrow(() -> new RuntimeException
//                        (String.format("Cannot Find Expense by Name - %s", Pracid)));
//      //  practitionerRepo.findById(id);
//        // Here is where we place and connect practitioner repository
//    }

}
