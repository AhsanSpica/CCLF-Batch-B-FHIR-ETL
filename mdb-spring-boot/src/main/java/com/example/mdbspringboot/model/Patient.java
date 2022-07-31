package com.example.mdbspringboot.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
//@AllArgsConstructor
//@NoArgsConstructor
@Document("patient")
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Patient {
    @Field("resourceType")
    private String resourceType;
    @Id
    @JsonProperty("pid")
    private String pid;
    @Field("Active")
    private Boolean Active;
  //  @Indexed(unique = true)
    @Field("name")
   private List<Name> name;
    @Field ("telecom")
    private List<ContactPoint> telecom;
    @Field ("gender")
    private Gender gender;
    @Field ("address")
    private List<Address> address;
    @Field ("link")
    private Link link;
    @Field ("linkcode")
    private int linkcode;

      //"id": "f201",
//      "active": true,
//      "name": [
//    {
//        "use": "official",
//            "text": "Roel",
//            "family": "Bor",
//            "given": [
//        "Roelof Olaf"
//      ],
//        "prefix": [
//        "Drs."
//      ],
//        "suffix": [
//        "PDEng."
//      ]
//    }
//  ],
//     "telecom": [
//    {
//        "system": "phone",
//            "value": "+31612345678",
//            "use": "mobile"
//    },
//    {
//        "system": "phone",
//            "value": "+31201234567",
//            "use": "home"
//    }
//  ],
//          "gender": "male",
//          "birthDate": "1960-03-13",
//          "deceasedBoolean": false,
//          "address": [
//    {
//        "use": "home",
//            "line": [
//        "Bos en Lommerplein 280"
//      ],
//        "city": "Amsterdam",
//            "postalCode": "1055RW",
//            "country": "NLD"
//    }
//  ]

    //@Field("description"); We can use this if we want to declare a description in the mongodb
    //collection of the element names perse column names
    //  @Field ("birthdate")
    //   private Date birthdate;
   // @Field ("period")
  //  private perioddate date;

  //  @Field("managingOrganization")
   // private Reference orgreference;
}
