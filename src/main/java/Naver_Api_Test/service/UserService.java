package Naver_Api_Test.service;

import Naver_Api_Test.Dto.UserDto;
import Naver_Api_Test.domain.entity.User;
import Naver_Api_Test.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User saveUser(UserDto userDto) {
        User user = User.builder()
                .name(userDto.getName())
                .Email(userDto.getEmail())
                .build();
        return userRepository.save(user);
    }
}
