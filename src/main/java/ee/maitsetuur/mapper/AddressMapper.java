package ee.maitsetuur.mapper;

import ee.maitsetuur.model.address.Address;
import ee.maitsetuur.request.AddressRequest;
import ee.maitsetuur.response.AddressResponse;
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
