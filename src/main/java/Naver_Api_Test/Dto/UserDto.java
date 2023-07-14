package Naver_Api_Test.Dto;

import Naver_Api_Test.domain.entity.LoginProvider;
import Naver_Api_Test.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String name;
    private String email;
    private LoginProvider loginProvider;

    public User to_Entity() {
        return User.builder()
                .name(name)
                .email(email)
                .loginProvider(loginProvider)
                .build();
    }
}
