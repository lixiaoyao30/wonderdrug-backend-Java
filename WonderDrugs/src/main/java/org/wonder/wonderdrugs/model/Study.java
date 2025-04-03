package org.wonder.wonderdrugs.model;
import lombok.Data;


@Data
public class Study {
    private String id;
    private String number;
    private String name;
    private String status;
    private String phase;
    private String type;
    private String vaultUrl;
}
