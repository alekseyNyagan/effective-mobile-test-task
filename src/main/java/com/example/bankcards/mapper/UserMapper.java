package com.example.bankcards.mapper;

import org.mapstruct.*;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@DecoratedWith(UserMapperDelegate.class)
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toEntity(UserDto userDto, PasswordEncoder passwordEncoder, Set<Role> roles);

    UserResponseDto toUserResponseDto(User user);
}
