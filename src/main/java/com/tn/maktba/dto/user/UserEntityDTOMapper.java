package com.tn.maktba.dto.user;

import com.tn.maktba.model.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserEntityDTOMapper implements Function<UserEntity, UserEntityDTO> {

    @Override
    public UserEntityDTO apply(UserEntity user) {
        return new UserEntityDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getIdCartNumber(),
                user.getEmail(),
                user.getAddress()
        );
    }
}
