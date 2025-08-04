package com.korit.BoardStudy.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UpdateBoardReqDto {

    private Integer boardId;
    private String title;
    private String content;


}
