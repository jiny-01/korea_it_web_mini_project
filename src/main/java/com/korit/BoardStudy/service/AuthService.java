package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.auth.SigninReqDto;
import com.korit.BoardStudy.dto.auth.SignupReqDto;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.entity.UserRole;
import com.korit.BoardStudy.repository.UserRepository;
import com.korit.BoardStudy.repository.UserRoleRepository;
import com.korit.BoardStudy.security.jwt.JwtUtils;
import com.korit.BoardStudy.security.model.PrincipalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Service
public class AuthService {



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    //회원가입 로직
    @Transactional(rollbackFor = Exception.class)  //하나라도 실패하면 실패하도록
    public ApiRespDto<?> signup(SignupReqDto signupReqDto) {

        //아이디 중복확인
        Optional<User> userByUsername = userRepository.getUserByUserName(signupReqDto.getUsername());
        if (userByUsername.isPresent()) {
            return new ApiRespDto<>("failed", "이미 사용중인 아이디", null);


        }

        //이메일 중복확인
        Optional<User> userByEmail = userRepository.getUserByUserEmail(signupReqDto.getEmail());
        if (userByEmail.isPresent()) {
            return new ApiRespDto<>("failed", "이미 사용중인 이메일", null);
        }

        try {
            //사용자 정보 추가
            Optional<User> optionalUser = userRepository.addUser(signupReqDto.toEntity(bCryptPasswordEncoder));
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("회원 정보 추가에 실패했습니다.");
            }

            //유저 객체 가져와놓기
            User user = optionalUser.get();

            //유저 권한 확인
            UserRole userRole = UserRole.builder()
                    .userId(user.getUserId())
                    .roleId(3)     //일단 임시사용자("3") 으로 설정
                    .build();

            int addUserRoleResult = userRoleRepository.addUserRole(userRole);
            if (addUserRoleResult != 1) {
                throw new RuntimeException("권한 추가에 실패했습니다.");
            }

            return new ApiRespDto<>("success", "회원가입이 성공적으로 완료되었습니다.", user);

        } catch (RuntimeException e) {
            return new ApiRespDto<>("failed", "회원가입 중 오류 발생 : " + e.getMessage(), null);
        }
    }


    //로그인 처리
    public ApiRespDto<?> signin(SigninReqDto signinReqDto) {
        //유저네임 존재하는지
        Optional<User> optionalUser = userRepository.getUserByUserName(signinReqDto.getUsername());
        //유저네임 존재안할 경우
        if (optionalUser.isEmpty()) {
            return new ApiRespDto<>("failed", "아이디 / pw 일치하지 않습니다.", null);
        }

        User user = optionalUser.get();

        //평문과 암호문 비교해서 다르면
        if (!bCryptPasswordEncoder.matches(signinReqDto.getPassword(), user.getPassword())) {
            return new ApiRespDto<>("failed", "아이디 / pw 일치하지 않습니다.", null);
        }

        //토큰 발급해줌
        String accessToken = jwtUtils.generateAccessToken(user.getUserId().toString());  //엑세스 토큰 생성
        return new ApiRespDto<>("success", "로그인 성공함", accessToken);

    }




}
