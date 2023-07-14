package Naver_Api_Test.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import Naver_Api_Test.Dto.UserDto;
import Naver_Api_Test.config.NaverLoginBO;
import Naver_Api_Test.domain.entity.LoginProvider;
import Naver_Api_Test.domain.entity.NaverTokens;
import Naver_Api_Test.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * Handles requests for the application home page.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private NaverLoginBO naverLoginBO;
    private String apiResult = null;

    @Autowired
    private void setNaverLoginBO(NaverLoginBO naverLoginBO) {
        this.naverLoginBO = naverLoginBO;
    }

    // 로그인 첫 화면 요청 메소드
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(Model model, HttpSession session) {

        /* 네이버아이디로 인증 URL을 생성하기 위하여 naverLoginBO클래스의 getAuthorizationUrl메소드 호출 */
        String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);
        log.info(naverAuthUrl);
        // 네이버
        model.addAttribute("url", naverAuthUrl);
        /* 생성한 인증 URL을 View로 전달 */
        return "login";
    }


    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state, HttpSession session) {
        OAuth2AccessToken oauthToken;
        try {
            oauthToken = naverLoginBO.getAccessToken(session, code, state);
            apiResult = naverLoginBO.getUserProfile(oauthToken);

            JSONObject json = new JSONObject(apiResult);
            String name = json.getJSONObject("response").getString("name");
            String email = json.getJSONObject("response").getString("email");
            String id = json.getJSONObject("response").getString("id");

            String AccessToken = oauthToken.getAccessToken();
            String RefreshToken = oauthToken.getRefreshToken();

            log.info(RefreshToken);
            UserDto userDto = new UserDto(name, email, LoginProvider.NAVER);
            userService.saveUser(userDto);


            return ResponseEntity.ok("Name: " + name + ", Email: " + email + ", id: " + id);
        } catch (IOException e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user profile.");
        }
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public ResponseEntity<String> refresh(@RequestParam String refreshToken) {
        try {
            OAuth2AccessToken newAccessToken = naverLoginBO.refreshAccessToken(refreshToken);
            if (newAccessToken != null) {
                String newAccessTokenValue = newAccessToken.getAccessToken();

                // 갱신된 AccessToken을 사용하여 추가 작업 수행
                // 예시: naverLoginBO.getUserProfile(newAccessToken);

                return ResponseEntity.ok("New Access Token: " + newAccessTokenValue);
            } else {
                // AccessToken을 갱신할 수 없는 경우
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to refresh access token.");
            }
        } catch (IOException e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to refresh access token.");
        }
    }
    @ResponseBody
    @GetMapping("/remove") //token = access_token임
    public String remove(@RequestParam String token, HttpSession session, HttpServletRequest request) {

        String deleteURL = naverLoginBO.removeAccessToken(token);
        session.invalidate();

        return "deleteURL";
    }

}
