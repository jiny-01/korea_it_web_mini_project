package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.account.ChangePasswordReqDto;
import com.korit.BoardStudy.dto.account.ChangeProfileImgReqDto;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.repository.UserRepository;
import com.korit.BoardStudy.security.model.PrincipalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public ApiRespDto<?> changePassword(ChangePasswordReqDto changePasswordReqDto, PrincipalUser principalUser) {
        Optional<User> userByUserId = userRepository.getUserByUserId(principalUser.getUserId());
        if (userByUserId.isEmpty()) {
            return new ApiRespDto<>("failed", "존재하지않는 사용자", null);
        }

        if(!Objects.equals(changePasswordReqDto.getUserId(), principalUser.getUserId())) {
            return new ApiRespDto<>("failed", "잘못된 요청", null);
        }

        if (!bCryptPasswordEncoder.matches(changePasswordReqDto.getOldPassword(), userByUserId.get().getPassword())) {
            return new ApiRespDto<>("failed", "기존비밀번호가 일치하지 않음", null);
        }

        if (bCryptPasswordEncoder.matches(changePasswordReqDto.getNewPassword(), userByUserId.get().getPassword())) {
            return new ApiRespDto<>("failed", "새 비밀번호는 기존 비밀번호와 달라야합니다.", null);
        }

        int result = userRepository.changePassword(changePasswordReqDto.toEntity(bCryptPasswordEncoder));
        if (result != 1) {
            return new ApiRespDto<>("failed", "문제가 발생함", null);
        }

        //정상 처리
        return new ApiRespDto<>("success", "비밀번호 변경 완료 \n다시 로그인해주세요", null);
    }

    //프로필 이미지 변경
    public ApiRespDto<?> changeProfileImg(ChangeProfileImgReqDto changeProfileImgReqDto, PrincipalUser principalUser) {
        //유저 아이디 비교
        Optional<User> userByUserId = userRepository.getUserByUserId(principalUser.getUserId());
        if (userByUserId.isEmpty()) {
            return new ApiRespDto<>("failed", "존재하지않는 사용자", null);
        }

        if(!Objects.equals(changeProfileImgReqDto.getUserId(), principalUser.getUserId())) {
            return new ApiRespDto<>("failed", "잘못된 요청", null);
        }

        //changeProfileImg 의 매개변수로 엔티티가 필요함 - 엔티티로 변환
        //result 에는 1(성공) / 0 반환
        int result = userRepository.changeProfileImg(changeProfileImgReqDto.toEntity());

        if (result != 1) {
            return new ApiRespDto<>("failed", "문제가 발생함", null);
        }

        //정상 처리
        return new ApiRespDto<>("success", "프로필 이미지 변경 완료", null);
    }
}
