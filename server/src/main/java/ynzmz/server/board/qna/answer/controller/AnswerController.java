package ynzmz.server.board.qna.answer.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ynzmz.server.board.qna.question.service.QuestionService;
import ynzmz.server.global.dto.SingleResponseDto;
import ynzmz.server.member.entity.Member;
import ynzmz.server.member.service.MemberService;
import ynzmz.server.board.qna.answer.dto.AnswerDto;
import ynzmz.server.board.qna.answer.entity.Answer;
import ynzmz.server.board.qna.answer.mapper.AnswerMapper;
import ynzmz.server.board.qna.answer.service.AnswerService;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/boards/qnas/answers")
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService;
    private final AnswerMapper answerMapper;
    private final MemberService memberService;
    private final AnswerService answerService;

    @PostMapping
    public ResponseEntity<?> postAnswer(@Valid @RequestBody AnswerDto.Post answerPost){
        Answer postDtoToAnswer = answerMapper.answerPostDtoToAnswer(answerPost);
        postDtoToAnswer.setMember(loginMemberFindByToken());
        postDtoToAnswer.setQuestion(questionService.findQuestionById(answerPost.getQuestionId()));

        Answer createAnswer = answerService.createAnswer(postDtoToAnswer);
        AnswerDto.SimpleInfoResponse response = answerMapper.answerToAnswerInfoResponse(createAnswer);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    @PatchMapping("/{answer-id}")
    public ResponseEntity<?> patchAnswer(@PathVariable("answer-id") long answerId, @RequestBody AnswerDto.Patch answerPatch) {
        //게시글 작성자 & 로그인된 회원 일치하는지 확인
        memberService.memberValidation(loginMemberFindByToken(), answerService.findAnswerById(answerId).getMember().getMemberId());

        Answer answer = answerMapper.answerPatchDtoToAnswer(answerPatch);
        answer.setAnswerId(answerId);

        Answer updateAnswer = answerService.updateAnswer(answer);
        AnswerDto.SimpleInfoResponse response = answerMapper.answerToAnswerInfoResponse(updateAnswer);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @DeleteMapping("/{answer-id}")
    public ResponseEntity<?> deleteAnswer(@PathVariable("answer-id")long answerId) {
        //게시글 작성자 & 로그인된 회원 일치하는지 확인
        memberService.memberValidation(loginMemberFindByToken(), answerService.findAnswerById(answerId).getMember().getMemberId());

        answerService.deleteAnswer(answerId);
        Optional<Answer> deletedAnswer = answerService.findOptionalAnswerById(answerId);

        return deletedAnswer.isEmpty() ? new ResponseEntity<>("삭제완료", HttpStatus.OK) : new ResponseEntity<>("삭제실패",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Member loginMemberFindByToken() {
        String loginEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 토큰에서 유저 email 확인
        return memberService.findMemberByEmail(loginEmail);
    }
}
