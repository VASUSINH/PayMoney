package com.PayMoney.Mapper;

import com.PayMoney.DTO.UserAuthResponseDTO;
import com.PayMoney.DTO.UserRequestDTO;
import com.PayMoney.DTO.UserResponseDTO;
import com.PayMoney.Entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

        //This Function converts DTO Request object to Entity Object.
        public UserEntity toEntity(UserRequestDTO userRequest) {



            UserEntity user = new UserEntity();

            user.setName(userRequest.getName());
            user.setEmail(userRequest.getEmail());
            user.setPassword(userRequest.getPassword());

            return user;
        }
       //This function converts Entity object to DTO Response Object.
        public UserResponseDTO toDTO(UserEntity user) {

            return new UserResponseDTO(
                    user.getUserId(),
                    user.getName(),
                    user.getEmail()
            );
        }

        //This function converts Entity object to the DTO used by other services to authenticate a request.
        public UserAuthResponseDTO toAuthDTO(UserEntity user) {

            return new UserAuthResponseDTO(
                    user.getUserId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getAccountStatus()
            );
        }

}
