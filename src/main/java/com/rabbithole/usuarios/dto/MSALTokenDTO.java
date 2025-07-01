package com.rabbithole.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MSALTokenDTO {
    private String oid;
    private String name;
    private String given_name;
    private String family_name;
    private String country;
    private String city;
    private String state;
    private String streetAddress;
    private String jobTitle;
    private List<String> emails;
    
    public String getEmail() {
        return emails != null && !emails.isEmpty() ? emails.get(0) : null;
    }
    
    public boolean isAdmin() {
        return "Admin".equals(jobTitle);
    }
}
