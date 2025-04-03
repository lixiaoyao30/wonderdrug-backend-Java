package org.wonder.wonderdrugs.model;
import lombok.Data;
import java.util.Date;

@Data
public class Site {
    private String id;
    private String number;
    private String name;
    private String status;
    private String siteStatus;
    private String statusColor;

    // 国家相关信息
    private String country;
    private String countryId;
    private String countryCode;

    // 地址相关信息
    private String address;
    private String city;
    private String state;
    private double latitude;
    private double longitude;

    // 其他信息
    private String vaultUrl;
    private String investigatorId;

    // 日期相关信息
    private Date plannedGreenlightDate;
    private Date actualSivDate;
    private Integer daysToGreenlight;

   
}
