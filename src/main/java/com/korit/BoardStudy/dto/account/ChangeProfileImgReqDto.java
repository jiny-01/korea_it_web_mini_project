package com.korit.BoardStudy.dto.account;

import com.korit.BoardStudy.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ChangeProfileImgReqDto {
    private Integer userId;
    private String profileImg;

    //엔티티로 DB 에 넣어줘야하므로 User 객체를 만들기
    public User toEntity() {
        return User.builder()
                .userId(userId)
                .profileImg(profileImg)
                .build();
    }
}
