package org.wonder.wonderdrugs.model;
import lombok.Data;

@Data
public class Country {
    private String id;
    private String name;
    private String code;
    private String abbreviation;
    private String status;
    private String vaultUrl;
}
