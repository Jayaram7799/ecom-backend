package in.btm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import in.btm.dto.UserDto;
import in.btm.entity.UserProfile;

@Mapper(
    componentModel = "spring",
    uses = AddressMapper.class
)
public interface UserMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserProfile toEntity(UserDto dto);

    UserDto toDto(UserProfile user);
}