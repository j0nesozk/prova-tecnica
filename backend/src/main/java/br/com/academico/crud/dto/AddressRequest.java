package br.com.academico.crud.dto;

import br.com.academico.crud.domain.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 200) String street,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 60) String state,
        @NotBlank @Size(max = 20) String zipCode,
        @NotBlank @Size(max = 60) String country
) {
    public Address toEntity() {
        return new Address(street, city, state, zipCode, country);
    }

    public void applyTo(Address address) {
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setZipCode(zipCode);
        address.setCountry(country);
    }
}
