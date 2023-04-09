package ee.kolbaska.kolbaska.mapper;

import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.request.AddressRequest;
import ee.kolbaska.kolbaska.response.AddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AddressMapper {
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    @Mapping(target = "DefaultModel.id", ignore = true)
    @Mapping(target = "TimeControl.createdAt", ignore = true)
    @Mapping(target = "TimeControl.updatedAt", ignore = true)
    @Mapping(target = "TimeControl.deletedAt", ignore = true)
    Address toAddress(AddressRequest addressRequest);

    AddressResponse toAddressResponse(Address address);
}
