package ynzmz.server.board.qna.question.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ynzmz.server.dto.MultiResponseDto;
import ynzmz.server.dto.SingleResponseDto;
import ynzmz.server.error.exception.BusinessLogicException;
import ynzmz.server.member.dto.MemberDto;
import ynzmz.server.member.entity.Member;
import ynzmz.server.member.service.MemberService;
import ynzmz.server.board.qna.answer.dto.AnswerDto;
import ynzmz.server.board.qna.answer.entity.Answer;
import ynzmz.server.board.qna.answer.mapper.AnswerMapper;
import ynzmz.server.board.qna.answer.service.AnswerService;
import ynzmz.server.board.qna.question.dto.QuestionDto;
import ynzmz.server.board.qna.question.entity.Question;
import ynzmz.server.board.qna.question.mapper.QuestionMapper;
import ynzmz.server.board.qna.question.service.QuestionService;
import ynzmz.server.s3.service.S3FileService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/boards/qnas/questions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class QuestionController {
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final MemberService memberService;
    private final AnswerService answerService;
    private final S3FileService s3FileService;

    @PostMapping
    public ResponseEntity<?> postQuestion(@Valid @RequestBody QuestionDto.Post questionPost){
        Question requestQuestion = questionMapper.questionPostToQuestion(questionPost);
        requestQuestion.setMember(loginMemberFindByToken());

        Question createdQuestion = questionService.createQuestion(requestQuestion);

        QuestionDto.InfoResponse response = questionMapper.questionToQuestionInfoResponse(createdQuestion);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    @PatchMapping("/{question-id}")
    public ResponseEntity<?> patchQuestion(@PathVariable("question-id") long questionId, @Valid @RequestBody QuestionDto.Patch questionPatch){
        //토큰에서 memberId 확인 & 본인이 쓴 게시물인지 확인
        memberService.memberValidation(loginMemberFindByToken(), questionService.findQuestionById(questionId).getMember().getMemberId());

        Question question = questionMapper.questionPatcToQuestion(questionPatch);
        question.setQuestionId(questionId);
        Question updateQuestion = questionService.updateQuestion(question);


        QuestionDto.InfoResponse response = questionMapper.questionToQuestionInfoResponse(updateQuestion);

        return new ResponseEntity<>(new SingleResponseDto<>(response),HttpStatus.OK);
    }

    //질문글 검색 리스트페이지 = 과목태그별 + 정렬 + 정순 역순 default
    @GetMapping
    public ResponseEntity<?> getQuestions(@RequestParam(required = false) String category,
                                          @RequestParam(required = false) String title,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam int page,
                                          @RequestParam(required = false) Integer size){
        size = (size == null) ? 15 : size;
        sort = (sort == null || sort.equals("최신순"))
                ? "questionId" : sort.equals("조회순") ? "viewCount" : sort.equals("추천순") ? "voteCount" : "questionId";

        Page<Question> questionPage = (category != null && category.equals("전체"))
                ? questionService.findAllQuestions(title, sort, page - 1, size)
                : (category != null && category.equals("공지"))
                ? questionService.findQuestionsByNotice( sort, page - 1, size)
                : questionService.findQuestionsByCategory(category, title, sort, page -1, size);

        List<Question> questions = questionPage.getContent();

        List<QuestionDto.ListPageResponse> responses = questionMapper.questionToQuestionListPageResponses(questions);

        return new ResponseEntity<>(new MultiResponseDto<>(responses,questionPage), HttpStatus.OK);
    }

    //질문글 상세페이지
    @GetMapping("/{question-id}")
    public ResponseEntity<?> getQuestion(@PathVariable("question-id") long questionId) {
        try {
            //로그인된 회원일경우 (해당 글 & 댓글 추천여부 같이반환)
            Member loginMember = loginMemberFindByToken();
            Question question = questionService.findQuestionById(questionId);
            questionService.setViewCount(question); //조회수기능  1번당 1씩 올라가게 (임시)

            MemberDto.VoteInfo loginMemberVoteInfo = memberService.findQnaVoteStatusByLoginUser(loginMember, question);
            QuestionDto.DetailPageResponse response = questionMapper.questionToQuestionDetailPageResponse(question);
            response.setLoginUserVoteInfo(loginMemberVoteInfo);

            return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
        } catch (BusinessLogicException e) {
            //로그인 안된 회원일 경우 (해당 글 & 댓글 추천상태값 없음)
            Question question = questionService.findQuestionById(questionId);
            questionService.setViewCount(question); //조회수기능  1번당 1씩 올라가게 (임시)

            QuestionDto.DetailPageResponse response = questionMapper.questionToQuestionDetailPageResponse(question);
            response.setLoginUserVoteInfo(null);

            return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
        }
    }


    //내가쓴글 조회
    @GetMapping("/{member-id}/question")
    public ResponseEntity<?> getMemberQuestion(@PathVariable("member-id") long memberId,
                                               @RequestParam int page,
                                               @RequestParam int size) {

        memberService.memberValidation(loginMemberFindByToken(), memberId); // 작성자 & 로그인된 회원 검증

        //페이지네이션 으로 질문글전체조회와 리스폰값 명세 통일(요청사항)
        Page<Question> pageQuestions = questionService.findQuestionsByMemberId(memberId, page - 1 , size);
        List<Question> questions = pageQuestions.getContent();
        List<QuestionDto.ListPageResponse> responses = questionMapper.questionToQuestionListPageResponses(questions);

        return new ResponseEntity<>(new MultiResponseDto<>(responses, pageQuestions), HttpStatus.OK);
    }

    @DeleteMapping("/{question-id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable("question-id") long questionId){

        //게시글 작성자 & 로그인된 회원 일치하는지 확인
        memberService.memberValidation(loginMemberFindByToken(), questionService.findQuestionById(questionId).getMember().getMemberId());

        Question question = questionService.findQuestionById(questionId);
        //게시글 이미지 삭제
        s3FileService.deleteFilesByS3Urls(question.getUploadImages());
        questionService.deleteQuestion(questionId);
        Optional<Question> deletedQuestion = questionService.findOptionalQuestionById(questionId);

        return deletedQuestion.isEmpty() ? new ResponseEntity<>("삭제완료",HttpStatus.OK) : new ResponseEntity<>("삭제실패",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 답변 채택기능
     * @param questionId = 질문글 식별자
     * @param answerId = 답변글 식별자
     * @return ResponseEntity
     */
    @PostMapping("{question-id}/adopt-answer/{answer-id}")
    public ResponseEntity<?> adoptAnswerToQuestion(@PathVariable("question-id") long questionId,
                                                   @PathVariable("answer-id") long answerId) {

        //답변 채택시 update 된 답변의 정보만 response 요청
        Member member = loginMemberFindByToken();
        Answer answer = answerService.findAnswerById(answerId);
        questionService.adoptAnswer(questionId, answer, member);
        Answer adoptedAnswer = answerService.findAnswerById(answerId);
        AnswerDto.SimpleInfoResponse response = answerMapper.answerToAnswerInfoResponse(adoptedAnswer);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }
    //로그인된 사용자 확인
    private Member loginMemberFindByToken(){

        String loginEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 토큰에서 유저 email 확인
        return memberService.findMemberByEmail(loginEmail);
    }
}

