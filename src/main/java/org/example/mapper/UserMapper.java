package org.example.mapper;

import org.example.dto.user.UserGetDto;
import org.example.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "avatar", target = "avatar")
    UserGetDto toDto(UserEntity userEntity);
}
