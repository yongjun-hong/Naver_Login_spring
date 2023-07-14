package Naver_Api_Test.controller;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String AccessToken;
    private String RefreshToken;
}
