package com.optimagrowth.license.model;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Getter @Setter @ToString
public class Organization extends RepresentationModel<Organization> {

    String id;
    String name;
    String contactName;
    String contactEmail;
    String contactPhone;

}