package com.example.mdbspringboot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class Name{
    public String use;
    public String text;
    public String family;
    public List<String> given;//"Roelof Olaf"
    public List<String> prefix;//"Drs."
    public List<String> suffix; //"PDEng."
//              "name": [
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
//  ]
}