package in.btm.dto;

import lombok.Data;

@Data
public class AddressDto {

    private Long id;

    private String fullName;

    private String phoneNumber;

    private String street;

    private String city;

    private String state;

    private String zipCode;
}