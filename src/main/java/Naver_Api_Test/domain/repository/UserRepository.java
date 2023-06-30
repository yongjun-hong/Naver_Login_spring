package Naver_Api_Test.domain.repository;

import Naver_Api_Test.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
