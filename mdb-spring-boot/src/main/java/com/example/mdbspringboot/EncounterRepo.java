package com.example.mdbspringboot;

import com.example.mdbspringboot.model.Encounter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EncounterRepo extends MongoRepository<Encounter, String> {
    //no logic to add as mongorepository
}
