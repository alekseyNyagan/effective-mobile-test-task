package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class UserMapperDelegate implements UserMapper {

    @Override
    public User toEntity(UserDto userDto, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        User user = new User();
        user.setName(userDto.name());
        user.setSurname(userDto.surname());
        user.setPhoneNumber(userDto.phoneNumber());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        user.setRoles(roleRepository.findByNameIn(userDto.roleNames()));
        return user;
    }
}
