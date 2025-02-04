package Funding.Startreum.common.config;

import Funding.Startreum.common.util.JwtAuthenticationFilter;
import Funding.Startreum.domain.users.CustomUserDetailsService;
import Funding.Startreum.domain.users.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

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


    // PasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    // SecurityFilterChain Bean 등록
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable) //  CSRF 비활성화 (REST API 방식)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ✅ 세션 비활성화 (JWT 사용)
                .authorizeHttpRequests(authorize -> authorize

                                .requestMatchers("/", "/home", "/index.html").permitAll()
                                .requestMatchers("/api/users/logout").permitAll()  // ✅ 로그아웃 요청은 인증 없이 가능
                                // ✅ 정적 리소스 허용 (CSS, JS, Images 등)
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                                // ✅ 회원가입, 회원생성 ,로그인, 중복 확인 API 허용
                                .requestMatchers("/api/users/signup", "/api/users/registrar", "/api/users/login", "/api/users/check-name", "/api/users/check-email").permitAll()

                                // ✅ 프로필 페이지 (View)는 누구나 접근 가능
                                .requestMatchers("/profile/{name}").permitAll()

                                // ✅ 프로필 수정 페이지 접근 허용 (로그인 없이 가능)
                                .requestMatchers("/profile/modify/{name}").permitAll()

                                // ✅ 댓글 조회 접근 허용 (로그인 없이 가능)
                                .requestMatchers("/api/comment/{projectId}").permitAll()

                                // ✅ 프로필 API는 인증된 사용자만 접근 가능
                                .requestMatchers("/api/users/profile/{name}").hasAnyRole("ADMIN", "BENEFICIARY", "SPONSOR")

                                // ✅ 이메일 수정 API (로그인 필요)
                                .requestMatchers("/api/users/profile/modify/{name}").authenticated()


                                // ✅ 그 외 모든 요청은 인증 필요
                                .anyRequest().authenticated()
                        //  .anyRequest().permitAll() // ✅ 모든 요청 허용 (테스트용)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // ✅ JWT 필터 추가
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성화 (Spring이 가로채지 않도록)
                .logout(logout -> logout
                        .logoutUrl("/api/users/logout") // ✅ 로그아웃 URL
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("로그아웃 성공");
                            response.getWriter().flush();
                        })
                        .permitAll()
                );
        http.authorizeHttpRequests(authorize -> {
            System.out.println("✅ Spring Security 설정 로드됨");
        });
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