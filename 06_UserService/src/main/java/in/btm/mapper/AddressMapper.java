package in.btm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import in.btm.dto.AddressDto;
import in.btm.entity.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

	@Mapping(target = "name", ignore = true)
    @Mapping(target = "phone", ignore = true)
    AddressDto toDto(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createDateTime", ignore = true)
    @Mapping(target = "updateDateTime", ignore = true)
    Address toEntity(AddressDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createDateTime", ignore = true)
    @Mapping(target = "updateDateTime", ignore = true)
    void updateEntityFromDto(AddressDto dto, @MappingTarget Address address);
}