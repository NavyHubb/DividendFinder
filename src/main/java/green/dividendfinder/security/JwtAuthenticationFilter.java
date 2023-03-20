package green.dividendfinder.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {  // 로그인 인증 구현을 위한 필터

    // http 요청에서 토큰은 header에 포함이 되는데, 어떤 key를 기준으로 토큰을 주고 받을지에 대한 key 값
    private static final String TOKEN_HEADER = "Authorization";

    // token prefix는 인증 타입을 나타내기 위해 사용. JWT의 경우 Bearer를 사용
    private static final String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // request의 header로부터 token 가져오기
        String token = this.resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {  // 토큰이 유효한 경우. 유효기간이 남았다는 의미.
            // 인증은 추가적으로 더 필요한 상황
            Authentication auth = this.tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);  // Context에 인증정보 넣어준다
        }

        filterChain.doFilter(request, response);  // filter가 연속적으로 실행될 수 있도록
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());  // prefix를 제외한 부분. 즉 토큰 부분 추출
        }

        return null;
    }

}
