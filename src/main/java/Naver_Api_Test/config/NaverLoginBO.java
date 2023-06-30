package Naver_Api_Test.config;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

public class NaverLoginBO {
    @Value("${spring.client.id}")
    private String CLIENT_ID;

    @Value("${spring.client.secret}")
    private String CLIENT_SECRET;

    @Value("${spring.client.redirect-uri}")
    private String REDIRECT_URI;

    @Value("${spring.client.session-state}")
    private String SESSION_STATE;

    @Value("${spring.profile-api-url}")
    private String PROFILE_API_URL;

    public String getAuthorizationUrl(HttpSession session) {
        //세션 유효성 검증을 위하여 난수를 생성
        String state = generateRandomString();
        // 생성한 난수 값을 session에 저장
        setSession(session, state);
//        Scribe에서 제공하는 인증 URL 생성 기능을 이용하여 네아로 인증 URL 생성
        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI)
                .state(state)
                .build(NaverOAuthApi.instance());
        return oAuth20Service.getAuthorizationUrl();
    }

//    네이버아이디로 Callback처리 및 AccessToken 획득 Method
    public OAuth2AccessToken getAccessToken(HttpSession session, String code, String state)throws IOException {
        //Callback으로 전달받은 세션검증용 난수값과 저장되어있는 값이 일치하는지 확인
        String sessionState = getSession(session);
        if (StringUtils.pathEquals(sessionState, state)) {
            OAuth20Service oAuth20Service = new ServiceBuilder()
                    .apiKey(CLIENT_ID)
                    .apiSecret(CLIENT_SECRET)
                    .callback(REDIRECT_URI)
                    .state(state)
                    .build(NaverOAuthApi.instance());
            OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(code);
            return accessToken;
        }
        return null;
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    private void setSession(HttpSession session, String state) {
        session.setAttribute(SESSION_STATE, state);
    }

    private String getSession(HttpSession session) {
        return (String) session.getAttribute(SESSION_STATE);
    }
//    AccessToken을 이용해 네이버 사용자프로필 API를 호출
    public String getUserProfile(OAuth2AccessToken oAuth2AccessToken) throws  IOException {
        OAuth20Service oAuth20Service = new ServiceBuilder()
                .apiKey(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI).build(NaverOAuthApi.instance());
        OAuthRequest request = new OAuthRequest(Verb.GET, PROFILE_API_URL, oAuth20Service);
        oAuth20Service.signRequest(oAuth2AccessToken, request);
        Response response = request.send();
        return response.getBody();
    }
}
