package com.example.mdbspringboot.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
//@AllArgsConstructor
@NoArgsConstructor
public class Address {

   private String use;
   private Type type;
   private String text;
   private List<String> line;
   private String City;
   private String District;
   private String State;
   private String postcode;
}
//    "address": [
//            {
//            "use": "home",

//            "line": [
//            "Bos en Lommerplein 280"
//            ],
//            "city": "Amsterdam",
//            "District":"Amsterdam",
//             "State":"Sindh",
//            "postalCode": "1055RW"
//            }
//            ]
