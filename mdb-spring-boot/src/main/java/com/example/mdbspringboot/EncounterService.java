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
public class EncounterService {

    private final EncounterRepo encounterRepo;
    @Autowired
    public EncounterService(EncounterRepo encounterRepo) {

        this.encounterRepo = encounterRepo;
    }
    public void addEncounter(Encounter encounter) {
       try{ encounterRepo.insert(encounter);}
        catch(Exception ex){
            String.format("Exception: "+ex.getCause()+", the exception msg: "+ex.getMessage());
        }
        // Here is where we place and connect encounter repository

    }
    public void deleteEncounter(String id)
    {
        encounterRepo.deleteById(id);
    }
    public void updateEncounter(Encounter encounter) {
        encounterRepo.save(encounter);
    }

    public Optional<Encounter> getEncounter(String Eid) {
        Optional<Encounter> encounter = Optional.of(new Encounter());

       try{
           encounter = encounterRepo.findById(Eid);

       }
       catch(Exception ex){

           String.format("Exception: "+ex.getCause()+", the exception msg: "+ex.getMessage());
       }
        return encounter;
    }

}
