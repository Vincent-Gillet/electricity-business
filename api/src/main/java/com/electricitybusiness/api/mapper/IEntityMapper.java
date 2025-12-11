package com.electricitybusiness.api.mapper;

import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.dto.user.UserUpdateDTO;
import com.electricitybusiness.api.model.User;

public interface IEntityMapper {
    UserDTO toDTO(User user);
    User toEntity(UserDTO dto);
/*
    User updateEntityFromDto(UserUpdateDTO dto, User entity);
*/
    User toEntity(UserUpdateDTO dto, User entity);
}
