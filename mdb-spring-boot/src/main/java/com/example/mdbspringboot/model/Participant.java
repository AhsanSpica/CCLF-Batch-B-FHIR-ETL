package com.example.mdbspringboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public class Participant {
   // String EntityType;
     // "individual": {
     //   "reference": "Practitioner/f201"
    //}
    @JsonProperty("individual")
    public Individual individual;

}
