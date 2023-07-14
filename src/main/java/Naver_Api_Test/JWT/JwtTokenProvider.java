package Naver_Api_Test.JWT;


import Naver_Api_Test.controller.TokenResponse;
import Naver_Api_Test.domain.repository.UserRepository;
import Naver_Api_Test.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// 토큰을 생성하고 검증하는 클래스입니다.
// 해당 컴포넌트는 필터클래스에서 사전 검증을 거칩니다.
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    private String secretKey = "missyouannawhereareyou";

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    private final RedisService redisService;

    // 객체 초기화, secretKey를 Base64로 인코딩한다.
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // JWT 토큰 생성
    public String createToken(String userPk, List<String> roles, Long tokenValidTime) { // userPk = email이다.
        Claims claims = Jwts.claims().setSubject(userPk); // JWT payload 에 저장되는 정보단위, 보통 여기서 user를 식별하는 값을 넣는다.
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // 정보 저장
                .claim("AUTHORITIES_KEY", roles)
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + tokenValidTime)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 사용할 암호화 알고리즘과
                // signature 에 들어갈 secret값 세팅
                .compact();
    }

    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 회원 정보 추출
    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public Long getExpiration(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        Date expirationDate = claims.getExpiration();
        return expirationDate.getTime();
    }

    // Request의 Header에서 token 값을 가져옵니다. "Authorization" : "TOKEN값'
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    // 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public TokenResponse createAccessTokenFromrefreshToken(String refreshToken) {
        Long tokenValidTime = 1000 * 60 * 60l;
        Long RefreshExpireTimeMs = 1000 * 60 * 60 * 60L;
        // Redis에 존재하는지 확인하기
        if (redisService.exists(refreshToken)) {
            // redis에서 email값 가져오기
            String email = redisService.getValues(refreshToken);

            // email로 user의 정보 가져오기
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 새로운 accesstoken 만들기
            String newAccessToken = createToken(userDetails.getUsername(), getRolesFromUserDetails(userDetails), tokenValidTime);
            String newRefreshToken = createToken(userDetails.getUsername(), getRolesFromUserDetails(userDetails), RefreshExpireTimeMs);

            TokenResponse tokenResponse = TokenResponse.builder()
                    .RefreshToken(newRefreshToken)
                    .AccessToken(newAccessToken)
                    .build();
            redisService.setValues(newRefreshToken, email);
            redisService.delValues(refreshToken);
            return tokenResponse;
        } else {
            throw new IllegalStateException("Invalid refresh token");
        }
    }


    private List<String> getRolesFromUserDetails(UserDetails userDetails) {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }


}