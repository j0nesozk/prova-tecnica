package br.com.academico.crud.dto;

import br.com.academico.crud.domain.entity.Address;

public record AddressResponse(
        Long id,
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {
    public static AddressResponse from(Address a) {
        return new AddressResponse(a.getId(), a.getStreet(), a.getCity(), a.getState(), a.getZipCode(), a.getCountry());
    }
}
