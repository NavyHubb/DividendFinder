package green.dividendfinder.security;

import green.dividendfinder.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;  // 1 hour

    private final MemberService memberService;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰 생성(발급)
     */
    public String generateToken(String username, List<String> roles) {
         // 사용자의 권한정보를 저장하기 위한 claim
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(KEY_ROLES, roles);

        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)  // 토큰 생성 시간
                .setExpiration(expiredDate)  // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, this.secretKey)  // 사용할 암호화 알고리즘, 비밀키
                .compact();
    }

//    @Transactional  // 이거 없으면 MemberEntity의 roles가 영속성을 잃어버려 조회가 안돼
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));

        // 사용자 정보와 사용자의 권한 정보를 담은 토큰
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();  // 토큰 생성 시에 subject에 username을 담아줬었으니까
    }

    // 토큰 유효성 검사(만료 여부)
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {  // token에 값이 없는 경우(공백, null 포함)
            return false;
        }

        var claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date());  // 현재 시각과 토큰의 만료 시각 비교
    }

    // claims 정보 가져오기
    private Claims parseClaims(String token) {
        try {
            // 토큰이 만료된 상태에서 파싱을 하면 아래 예외 발생 가능
            return  Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
