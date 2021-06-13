package com.gamakdragons.wheretruck.auth.service.kakao;

import com.gamakdragons.wheretruck.auth.dto.LoginRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LoginResponseDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutResponseDto;
import com.gamakdragons.wheretruck.auth.dto.kakao.KaKaoUserInfoResponse;
import com.gamakdragons.wheretruck.auth.service.JwtUtil;
import com.gamakdragons.wheretruck.auth.service.OAuth2Service;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.entity.User;
import com.gamakdragons.wheretruck.domain.user.service.UserService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Qualifier("kakao")
@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoAuthService implements OAuth2Service {

    private final RestTemplate restTemplate;
    private final UserService userService;

    @Value("${oauth2.provider.kakao.user_info_url}")
    private String kakaoAuthUrl;

    @Value("${oauth2.provider.kakao.auth}")
    private String authType;

    @Value("${oauth2.provider.kakao.logout_url}")
    private String logoutUrl;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        log.info("loginRequestDto=" + loginRequestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authType + " " + loginRequestDto.getAuthToken());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        HttpEntity<?> httpEntity = new HttpEntity<>(params, headers);

        String userId = restTemplate.postForObject(kakaoAuthUrl, httpEntity, KaKaoUserInfoResponse.class).getId();
        
        log.info("userId=" + userId);

        User user = userService.getById(userId);

        if(user != null) {
            log.info("user exists: " + user);
        } else {
            user = User.builder()
                        .id(userId)
                        .nickName(loginRequestDto.getNickName())
                        .role(loginRequestDto.getRole())
                        .build();

            IndexUpdateResultDto result = userService.saveUser(user); 
            log.info("user save result: " + result);
        }
        
        String jwt = JwtUtil.generateToken(userId);
        log.info("jwt=" + jwt);

        return LoginResponseDto.builder()
                            .jwt(jwt)
                            .user(user)
                            .build();
    }

    @Override
    public LogoutResponseDto logout(String userId, LogoutRequestDto logoutRequestDto) {
        
        log.info("logoutRequestDto=" + logoutRequestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authType + " " + logoutRequestDto.getAccessToken());

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("target_id_type", "user_id");
        params.add("target_id", Integer.parseInt(userId));

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

        String loggedOutUserId = restTemplate.postForObject(logoutUrl, entity, KaKaoUserInfoResponse.class).getId();

        return LogoutResponseDto.builder()
                                .id(loggedOutUserId)
                                .build();
    }

    
}
