package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.OAuth2.OAuth2MergeReqDto;
import com.korit.BoardStudy.dto.OAuth2.OAuth2SignupReqDto;
import com.korit.BoardStudy.entity.OAuth2User;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.entity.UserRole;
import com.korit.BoardStudy.repository.OAuth2UserRepository;
import com.korit.BoardStudy.repository.UserRepository;
import com.korit.BoardStudy.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class OAuth2AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2UserRepository oAuth2UserRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRoleRepository userRoleRepository;


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


    @Transactional(rollbackFor = Exception.class)        //try 안에서 예외 하나라도 터지면 모두 실패로
    public ApiRespDto<?> signup(OAuth2SignupReqDto oAuth2SignupReqDto) {

        //유저 네임 확인
        Optional<User> userByUsername = userRepository.getUserByUserName(oAuth2SignupReqDto.getUsername());
        if (userByUsername.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 아이디입니다.", null);
        }

        //유저 이메일 확인
        Optional<User> userByEmail = userRepository.getUserByUserEmail(oAuth2SignupReqDto.getEmail());
        if (userByEmail.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 이메일입니다.", null);
        }

        Optional<OAuth2User> optionalOAuth2User =
                oAuth2UserRepository.getOAuth2UserByProviderAndProviderUserId(oAuth2SignupReqDto.getProvider(), oAuth2SignupReqDto.getProviderUserId());
        if (userByEmail.isPresent()) {
            return new ApiRespDto<>("failed", "이미 존재하는 이메일입니다.", null);
        }

        try {
            Optional<User> optionalUser = userRepository.addUser(oAuth2SignupReqDto.toEntity(bCryptPasswordEncoder));

            if(optionalUser.isEmpty()) {
                throw new RuntimeException("회원정보 추가에 실패함");
            }

            //optionaluser 에 있는 것 따로 User 객체로 뺴옴
            User user = optionalUser.get();

            UserRole userRole = UserRole.builder()
                    .userId(user.getUserId())
                    .roleId(3)
                    .build();

            int addUserRoleResult = userRoleRepository.addUserRole(userRole);    //성공하면 1, 실패면 1 아님

            if (addUserRoleResult !=1 ) {
                throw new RuntimeException("권한 정보 추가에 실패함");
            }

            //정상적으로 성공했다면
            int oauth2InsertResult = oAuth2UserRepository.addOAuth2User(oAuth2SignupReqDto.toOAuth2User(user.getUserId()));
            if (oauth2InsertResult != 1) {
                throw new RuntimeException("OAuth2 사용자 정보 추가 실패");
            }

            return new ApiRespDto<>("success", "정상 회원가입 완료", user);

        } catch (Exception e) {
            return new ApiRespDto<>("failed", "회원가입 중 오류 발생:" + e.getMessage(), null);
        }
    }
}
