package com.example.mdbspringboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public class Link {
    @JsonProperty("PatientName")
    @Field("PatientName")
    String patientname;
    @JsonProperty("PatientCode")
    @Field("PatientCode")
    String patientcode;
}
