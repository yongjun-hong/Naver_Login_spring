package Naver_Api_Test.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class NaverApiService {

    public boolean validateAccessToken(String accessToken) {
        try {
            // API 호출을 통해 AccessToken의 유효성을 검사합니다.
            // 네이버 API의 사용자 프로필 엔드포인트를 호출하여 응답 코드를 확인합니다.
            URL url = new URL("https://openapi.naver.com/v1/nid/me");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            // 200 OK 응답 코드인 경우에는 AccessToken이 유효합니다.
            return responseCode == 200;
        } catch (IOException e) {
            e.printStackTrace();
            // 예외 발생 시 AccessToken은 유효하지 않은 것으로 처리합니다.
            return false;
        }
    }
}
