package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.OAuth2.OAuth2MergeReqDto;
import com.korit.BoardStudy.entity.OAuth2User;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.repository.OAuth2UserRepository;
import com.korit.BoardStudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OAuth2AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2UserRepository oAuth2UserRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional(rollbackFor = Exception.class)           //예외 발생 시 롤백 발생
    public ApiRespDto mergerAccount(OAuth2MergeReqDto oAuth2MergeReqDto) {
        Optional<User> optionalUser = userRepository.getUserByUserName(oAuth2MergeReqDto.getUsername());
        if (optionalUser.isEmpty()) {
            return new ApiRespDto("failed", "사용자 정보를 확인하세요", null);

        }

        User user = optionalUser.get();

        Optional<OAuth2User> optionalOAuth2User =
                oAuth2UserRepository.getOAuth2UserByProviderAndProviderUserId(oAuth2MergeReqDto.getProvider(), oAuth2MergeReqDto.getProviderUserId());
        if(optionalOAuth2User.isPresent()) {
            return new ApiRespDto<>("failed", "이 계정은 이미 소셜 계정 연동되어있음", null);
        }

        //비밀번호 일치하지 않음
        if (!bCryptPasswordEncoder.matches(oAuth2MergeReqDto.getPassword(), user.getPassword())) {

            return new ApiRespDto<>("failed", "사용자 정보를 확인하세요", null);
        }

        try {
            int result = oAuth2UserRepository.addOAuth2User(oAuth2MergeReqDto.toOAuth2User(user.getUserId()));

            if (result != 1) {
                throw new RuntimeException("Oauth2 사용자 연동에 실패");
            }
            return new ApiRespDto("success", "성공적으로 계정 연동이 되었습니다.", null);
        } catch (Exception e) {
            return new ApiRespDto("failed", "계정 연동 중 오류가 발생했습니다:" + e.getMessage(), null);
        }
    }

}
