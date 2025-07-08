package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.board.AddBoardReqDto;
import com.korit.BoardStudy.entity.Board;
import com.korit.BoardStudy.repository.BoardRepository;
import com.korit.BoardStudy.security.model.PrincipalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> addBoard(AddBoardReqDto addBoardReqDto, PrincipalUser principalUser) {
        if (principalUser == null || !addBoardReqDto.getUserId().equals(principalUser.getUserId())) {
            return new ApiRespDto<>("failed", "잘못된 접근. 로그인 정보가 유효하지 않거나 권한이 없습니다", null);
        }

        if (addBoardReqDto.getTitle() == null || addBoardReqDto.getTitle().trim().isEmpty()) {
            return new ApiRespDto<>("failed", "제목은 필수입력사항입니다.", null);
        }

        if (addBoardReqDto.getContent() == null || addBoardReqDto.getContent().trim().isEmpty()) {
            return new ApiRespDto<>("failed", "내용은 필수입력사항입니다.", null);
        }

        try {
          int result = boardRepository.addBoard(addBoardReqDto.toEntity());
          if (result != 1) {
              return new ApiRespDto<>("failed", "게시물 추가 실패", null);
          }

          return new ApiRespDto<>("success", "게시물 추가 성공", null);
        } catch (Exception e) {
            return new ApiRespDto<>("failed", "서버 오류로 게시물 추가 실패 : " + e.getMessage(), null);
        }

    }


    //게시물 단건 조회
    public ApiRespDto<?> getBoardByBoardId(Integer boardId) {
        if (boardId == null || boardId <= 0) {
            return new ApiRespDto<>("failed", "유효하지 않은 게시물 ID 입니다.", null);
        }

        //정상조회
        Optional<Board> optionalBoard = boardRepository.getBoardByBoardId(boardId);
        if (optionalBoard.isEmpty()) {
            return new ApiRespDto<>("failed", "해당 ID 의 게시물을 찾을 수 없음.", null);
        } else {
            return new ApiRespDto<>("success", "게시물 조회 성공", optionalBoard.get());
        }

    }


    //게시물 전체 조회
    public ApiRespDto<?> getBoardList () {
        List<Board> boardList = boardRepository.getBoardList();
        if (boardList.isEmpty()) {
            return new ApiRespDto<>("failed", "조회할 게시물 없음", null);
        } else {
            return new ApiRespDto<>("success", "게시물 목록 조회 성공", boardList);
        }
    }



}
