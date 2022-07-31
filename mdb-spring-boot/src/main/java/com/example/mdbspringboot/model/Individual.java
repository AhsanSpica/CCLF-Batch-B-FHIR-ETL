package com.example.mdbspringboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public class Individual {
   // final String entitytype="Practitioner";
        @JsonProperty("reference")
        EntityType Entity;
        @JsonProperty("PractitionerId")
        String PractitionerId ;//f201
   // public String reference; Partici
    //i am thinking indivual might be an object
}
