package com.PayMoney.Mapper;

import com.PayMoney.DTO.userRequestDTO;
import com.PayMoney.DTO.userResponseDTO;
import com.PayMoney.Entity.userEntity;
import org.springframework.stereotype.Component;

@Component
public class userMapper {

        //This Function converts DTO Request object to Entity Object.
        public userEntity toEntity(userRequestDTO userRequest) {



            userEntity user = new userEntity();

            user.setName(userRequest.getName());
            user.setEmail(userRequest.getEmail());
            user.setPassword(userRequest.getPassword());

            return user;
        }
       //This function converts Entity object to DTO Response Object.
        public userResponseDTO toDTO(userEntity user) {

            return new userResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );
        }

}
