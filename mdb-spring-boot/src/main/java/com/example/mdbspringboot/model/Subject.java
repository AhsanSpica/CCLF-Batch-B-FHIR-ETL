package com.example.mdbspringboot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subject {
    public EntityType reference;//"Patient/f201"
    public String patientid;
    public String display; // patient name
    //public String reference public String display;// where reference is name
    //of patient
}
