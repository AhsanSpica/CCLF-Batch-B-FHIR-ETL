package com.example.mdbspringboot;

import com.example.mdbspringboot.model.Practitioner;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PractitionerRepo extends MongoRepository<Practitioner, String> {
}
