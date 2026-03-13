package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

public abstract class UserMapperDelegate implements UserMapper {

    @Override
    public User toEntity(UserDto userDto, PasswordEncoder passwordEncoder, Set<Role> roles) {
        User user = new User();
        user.setName(userDto.name());
        user.setSurname(userDto.surname());
        user.setPhoneNumber(userDto.phoneNumber());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        user.setRoles(roles);
        return user;
    }
}
