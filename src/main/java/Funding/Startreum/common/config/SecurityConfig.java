package Funding.Startreum.common.config;

import Funding.Startreum.common.util.JwtAuthenticationFilter;
import Funding.Startreum.domain.users.CustomUserDetailsService;
import Funding.Startreum.domain.users.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // ✅ 비밀번호 암호화 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 사용자 인증 서비스 설정
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    // ✅ Spring Security 필터 체인 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable) //  CSRF 비활성화 (REST API 방식)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ 세션 비활성화 (JWT 사용)
                .authorizeHttpRequests(authorize -> authorize

                        // ✅ 프로젝트 생성 API는 수혜자(ROLE_BENEFICIARY)만 접근 가능하도록 설정
                        .requestMatchers(HttpMethod.POST, "/api/beneficiary/create/projects").hasRole("BENEFICIARY")
                        .requestMatchers(HttpMethod.GET, "/projects/new").permitAll()

                        // ✅ 인증 없이 접근 가능한 정적 리소스 및 공용 API
                        .requestMatchers("/", "/home", "/index.html").permitAll()
                        .requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/users/signup", "/api/users/registrar", "/api/users/login", "/api/users/check-name", "/api/users/check-email").permitAll()

                        .requestMatchers("/profile/{name}").permitAll()  // ✅ 프로필 뷰는 인증 없이 접근 가능
                        .requestMatchers("/profile/modify/{name}").permitAll() // ✅ 프로필 수정 뷰도 인증 없이 접근 가능
                        .requestMatchers("/api/users/profile/{name}").authenticated()  // ✅ 프로필 API는 인증 필요
                        .requestMatchers("/api/users/profile/modify/{name}")
                        .access((authenticationSupplier, context) -> {
                            Authentication authentication = authenticationSupplier.get();
                            String pathUsername = context.getVariables().get("name");

                            boolean isOwner = authentication.getName().equals(pathUsername);
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                            return new AuthorizationDecision(isOwner || isAdmin);  // ✅ 본인 또는 관리자만 수정 가능
                        })

                        .requestMatchers("/profile/account/{name}").permitAll()  // ✅ HTML 페이지는 인증 없이 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/account/user/{name}").authenticated()  // ✅ 계좌 조회는 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/api/account/user/{name}/create")
                        .access((authenticationSupplier, context) -> {
                            Authentication authentication = authenticationSupplier.get();
                            String requestURI = context.getRequest().getRequestURI();

                            // 🔍 요청된 사용자 이름 추출
                            String[] parts = requestURI.split("/"); // /api/account/user/{name}/create 형태
                            String pathUsername = parts[parts.length - 2]; // {name} 위치


                            return new AuthorizationDecision(authentication.getName().equals(pathUsername));  // ✅ 본인만 계좌 생성 가능
                        })

                        // ✅ 모든 API 요청에 대해 JWT 인증 필터 적용
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // ✅ JWT 필터 추가
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성화 (Spring이 가로채지 않도록)
                .logout(logout -> logout
                        .logoutUrl("/api/users/logout") // ✅ 로그아웃 URL
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);

                            String jsonResponse = "{\"status\": \"success\", \"message\": \"로그아웃 성공\"}";
                            response.getWriter().write(jsonResponse);
                            response.getWriter().flush();
                        })
                        .permitAll()
                );

        System.out.println("✅ Spring Security 설정 로드됨");
        return http.build();
    }

    // CORS 설정 추가 (필요한 경우 도메인 허용 가능)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:9090")); // 허용할 도메인 추가
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); //  Authorization 헤더 추가
        configuration.setExposedHeaders(List.of("Authorization")); //  클라이언트가 Authorization 헤더 접근 가능하게

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}