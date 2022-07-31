package com.example.mdbspringboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
@Getter
@Setter
public class Practitioner {
    @Field("resourceType")
    private String resourceType;
    @Id
    @JsonProperty("pracid")
    private String pracid;
    //private String resourceType;
    @Field("name")
    private List<Name> name;
    @Field("Active")
    private boolean Active;
    @Field("telecom")
    private List<ContactPoint> telecom;
    @Field ("gender")
    private Gender gender;
    @Field ("address")
    private List<Address> address;
    //private Address address;

  /*
  "active": true,
  "name": [
    {
      "use": "official",
      "text": "Dokter Bronsig",
      "family": "Bronsig",
      "given": [
        "Arend"
      ],
      "prefix": [
        "Dr."
      ]
    }
  ],
  "telecom": [
    {
      "system": "phone",
      "value": "+31715269111",
      "use": "work"
    }
  ],
  "address": [
    {
      "use": "work",
      "line": [
        "Walvisbaai 3",
        "C4 - Automatisering"
      ],
      "city": "Den helder",
      "postalCode": "2333ZA",
      "country": "NLD"
    }
  ],
   */


}
