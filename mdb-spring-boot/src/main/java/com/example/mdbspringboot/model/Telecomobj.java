package com.example.mdbspringboot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Telecomobj {
           //system": "phone",
    //      "value": "(03) 3410 5613",
    //      "use": "mobile",
    //      "rank": 2
     private String value;
     private String use;
     private String system;
     private String rank;

   // use,system,value,rank
}
