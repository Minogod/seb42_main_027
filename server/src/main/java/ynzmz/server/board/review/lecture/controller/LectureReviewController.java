package ynzmz.server.board.review.lecture.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ynzmz.server.board.review.lecture.dto.LectureReviewDto;
import ynzmz.server.board.review.lecture.entity.LectureReview;
import ynzmz.server.global.dto.SingleResponseDto;
import ynzmz.server.global.error.exception.BusinessLogicException;
import ynzmz.server.board.lecture.service.LectureService;
import ynzmz.server.board.review.lecture.mapper.LectureReviewMapper;
import ynzmz.server.board.review.lecture.sevice.LectureReviewService;
import ynzmz.server.member.dto.MemberDto;
import ynzmz.server.member.entity.Member;
import ynzmz.server.member.service.MemberService;
import ynzmz.server.board.teacher.mapper.TeacherMapper;
import ynzmz.server.board.teacher.service.TeacherService;

import java.util.Optional;

@RestController
@RequestMapping("/boards/reviews/lectures")
@RequiredArgsConstructor
@Slf4j
public class LectureReviewController {
    private final LectureReviewService lectureReviewService;
    private final LectureService lectureService;
    private final TeacherService teacherService;
    private final MemberService memberService;
    private final LectureReviewMapper lectureReviewMapper;
    private final TeacherMapper teacherMapper;
    //리뷰작성
    @PostMapping
    public ResponseEntity<?> postLectureReview(@RequestBody LectureReviewDto.Post lectureReviewPost){
        LectureReview lectureReview = lectureReviewMapper.lectureReviewPostToLectureReview(lectureReviewPost);
        lectureReview.setLecture(lectureService.findLectureById(lectureReviewPost.getLectureId()));
        lectureReview.setMember(loginMemberFindByToken());

        LectureReview createdLectureReview = lectureReviewService.createLectureReview(lectureReview);

        //리뷰 등록시 강의의 평균별점 및 총 리뷰갯수 수정
        lectureService.setLectureStarPointAverageAndTotalReviewCount(createdLectureReview.getLecture());
        //리뷰 등록시 강사의 평균별점 및 총 리뷰갯수 수정
        teacherService.setTeacherStarPointAverageAndTotalReviewCount(createdLectureReview.getLecture().getTeacher());

        LectureReviewDto.InfoResponse response = lectureReviewMapper.lectureReviewToLectureReviewInfoResponse(createdLectureReview);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }
    //리뷰수정
    @PatchMapping("/{lecture-review-id}")
    public ResponseEntity<?> patchLectureReview(@PathVariable("lecture-review-id") long lectureReviewId,
                                                @RequestBody LectureReviewDto.Patch lectureReviewPatch) {

        //토큰에서 memberId 확인 & 본인이 쓴 게시물인지 확인
        memberService.memberValidation(loginMemberFindByToken(), lectureReviewService.findLectureReviewById(lectureReviewId).getMember().getMemberId());

        LectureReview lectureReview = lectureReviewMapper.lectureReviewPatchToLectureReview(lectureReviewPatch);
        lectureReview.setLectureReviewId(lectureReviewId);
        LectureReview updatedLectureReview = lectureReviewService.updateLectureReview(lectureReview);

        //리뷰 수정시 강의의 평균별점 및 총 리뷰갯수 수정
        lectureService.setLectureStarPointAverageAndTotalReviewCount(updatedLectureReview.getLecture());
        //리뷰 수정시 강사의 평균별점 및 총 리뷰갯수 수정
        teacherService.setTeacherStarPointAverageAndTotalReviewCount(updatedLectureReview.getLecture().getTeacher());

        LectureReviewDto.InfoResponse response = lectureReviewMapper.lectureReviewToLectureReviewInfoResponse(updatedLectureReview);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    //리뷰 1건 상세조회
    @GetMapping("/{lecture-review-id}")
    public ResponseEntity<?> getLectureReviewDetail(@PathVariable("lecture-review-id") long lectureReviewId){
        try {
            //로그인 회원인경우, 해당 디테일페이지에서 게시글,댓글 추천값정보 반환
            Member loginMember = loginMemberFindByToken();

            LectureReview lectureReview = lectureReviewService.findLectureReviewById(lectureReviewId);
            MemberDto.LoginUserLectureReviewVoteInfo lectureReviewVoteStatusByLoginUser = memberService.findLectureReviewVoteStatusByLoginUser(loginMember, lectureReview);
            LectureReviewDto.DetailPageResponse response = lectureReviewMapper.lectureReviewToLectureReviewDetailPageResponse(lectureReview);
            response.setTeacher(teacherMapper.teacherToTeacherSimpleInfoResponse(lectureReview.getLecture().getTeacher()));
            response.setLoginUserLectureReviewVoteInfo(lectureReviewVoteStatusByLoginUser);

            return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
        } catch (BusinessLogicException e) {
            //로그인 안된 회원일경우, 추천값 정보 null
            LectureReview lectureReview = lectureReviewService.findLectureReviewById(lectureReviewId);
            LectureReviewDto.DetailPageResponse response = lectureReviewMapper.lectureReviewToLectureReviewDetailPageResponse(lectureReview);

            response.setTeacher(teacherMapper.teacherToTeacherSimpleInfoResponse(lectureReview.getLecture().getTeacher()));

            return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
        }
    }
    //리뷰 삭제
    @DeleteMapping("/{lecture-review-id}")
    public ResponseEntity<?> deleteLectureReview(@PathVariable("lecture-review-id") long lectureReviewId){

        //게시글 작성자 & 로그인된 회원 일치하는지 확인
        memberService.memberValidation(loginMemberFindByToken(), lectureReviewService.findLectureReviewById(lectureReviewId).getMember().getMemberId());

        lectureReviewService.deleteLectureReview(lectureReviewId);
        Optional<LectureReview> deletedLectureReview = lectureReviewService.findOptionalLectureReviewById(lectureReviewId);
        return deletedLectureReview.isEmpty() ? new ResponseEntity<>("삭제완료",HttpStatus.OK) : new ResponseEntity<>("삭제실패",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Member loginMemberFindByToken(){
        String loginEmail = SecurityContextHolder.getContext().getAuthentication().getName(); // 토큰에서 유저 email 확인
        return memberService.findMemberByEmail(loginEmail);
    }
}
