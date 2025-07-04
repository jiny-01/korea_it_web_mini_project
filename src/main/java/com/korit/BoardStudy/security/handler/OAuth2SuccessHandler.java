package com.korit.BoardStudy.security.handler;


import com.korit.BoardStudy.entity.OAuth2User;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.repository.OAuth2UserRepository;
import com.korit.BoardStudy.repository.UserRepository;
import com.korit.BoardStudy.security.jwt.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

//인증 객체 만들어졌을 때 사용자 정보 DB 에서 확인 - 연동 여부
//연동 O : 토큰
//연동 X : prover id, email 넘겨줌
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private OAuth2UserRepository oAuth2UserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String provider = defaultOAuth2User.getAttribute("provider");
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//        String provider = oauthToken.getAuthorizedClientRegistrationId();  // "naver", "kakao" 등
        String providerUserId = defaultOAuth2User.getAttribute("id");
        String email = defaultOAuth2User.getAttribute("email");


        //가져온 거로 조회 - mapper 만들기
        Optional<OAuth2User> optionalOAuth2User = oAuth2UserRepository.getOAuth2UserByProviderAndProviderUserId(provider, providerUserId);

        //연동 안되어있을 때 - provider, userid, email 주고 연동할지/새로 가입할지 선택하는 페이지 반환
        if (optionalOAuth2User.isEmpty()) {
            response.sendRedirect
                    ("http://localhost:3030/auth/oauth2?provider=" + provider + "&providerUserId=" + providerUserId + "&email=" + email);    //리다이렉트시킬 프론트주소 들어감
            return;
        }

        //oAuth2User 객체로 가져오기
        OAuth2User oAuth2User = optionalOAuth2User.get();

        //연동된 적 있음 - User(기존 회원객체) 있을 것
        Optional<User> optionalUser = userRepository.getUserByUserId(oAuth2User.getUserId());

        String accessToken = null;

        if (optionalUser.isPresent()) {
            accessToken = jwtUtils.generateAccessToken(optionalUser.get().getUserId().toString());
        }

        response.sendRedirect("http://localhost:3000/auth/signin?accessToken=" + accessToken);

    }










}
