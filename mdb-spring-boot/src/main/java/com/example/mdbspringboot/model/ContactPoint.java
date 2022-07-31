package com.example.mdbspringboot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactPoint {
    private String value;
    private String use;
    private String system;
    private String rank;

}
