package ynzmz.server.board.lecture.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import ynzmz.server.global.error.exception.BusinessLogicException;
import ynzmz.server.global.error.exception.ExceptionCode;
import ynzmz.server.board.lecture.entity.Lecture;
import ynzmz.server.board.lecture.repository.LectureRepository;
import ynzmz.server.board.review.lecture.entity.LectureReview;
import ynzmz.server.tag.entity.GradeTag;
import ynzmz.server.tag.entity.PlatformTag;
import ynzmz.server.tag.entity.SubjectTag;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureService {
    private final LectureRepository lectureRepository;
    @Transactional
    public Lecture createdLecture(Lecture lecture) {
        return lectureRepository.save(lecture);
    }

    public Lecture updateLecture(Lecture lecture) {
        Lecture findLecture = findLectureById(lecture.getLectureId());

        Optional.ofNullable(lecture.getTitle()).ifPresent(findLecture::setTitle);
        Optional.ofNullable(lecture.getIntroduction()).ifPresent(findLecture::setIntroduction);
        Optional.ofNullable(lecture.getStatus()).ifPresent(findLecture::setStatus);

        return lectureRepository.save(findLecture);
    }

    public Page<Lecture> findLectures(GradeTag.Grade grade, PlatformTag.Platform platform, SubjectTag.Subject subject, String title, String sort, int page, int size) {
        return lectureRepository.findAllByGradeAndPlatformAndSubjectAndTitle(grade, platform, subject, title, PageRequest.of(page, size, Sort.by(sort)));
    }

    public Page<Lecture> findLectures(GradeTag.Grade grade, PlatformTag.Platform platform, SubjectTag.Subject subject, String title, String sort,String reverse, int page, int size) {
        return lectureRepository.findAllByGradeAndPlatformAndSubjectAndTitle(grade, platform, subject, title, PageRequest.of(page, size, Sort.by(sort).descending()));
    }

    public Page<Lecture> findLecturesByRandom(GradeTag.Grade grade, PlatformTag.Platform platform, SubjectTag.Subject subject, String title, String sort, int page, int size) {
        return lectureRepository.findAllByGradeAndPlatformAndSubjectAndTitle(grade, platform, subject, title, PageRequest.of(page, size, JpaSort.unsafe("RAND()")));
    }

    @Transactional
    public void deleteLecture(long lectureId) {
        Lecture lecture = findLectureById(lectureId);
        lectureRepository.delete(lecture);
    }

    public Lecture findLectureById(long lectureId){
        Optional<Lecture> lecture = lectureRepository.findById(lectureId);
        return lecture.orElseThrow(() -> new BusinessLogicException(ExceptionCode.TEACHER_NOT_FOUND));
    }

    public Optional<Lecture> findOptionalLectureById(long lectureId){
        return lectureRepository.findById(lectureId);
    }

    //강의 평균 별점 저장
//    @Transactional
    public void setLectureStarPointAverageAndTotalReviewCount(Lecture lecture){
        List<LectureReview> lectureReviews = lecture.getLectureReviews();

        double starPoint = 0;
        double starPointAverage = 0;
        long totalReviewCount = 0;

        if(!lectureReviews.isEmpty()) {
            for(LectureReview lectureReview : lectureReviews) {
                starPoint += lectureReview.getStarPoint();
            }
            starPointAverage = starPoint / lectureReviews.size();
            totalReviewCount = lectureReviews.size();
        }

        lecture.setStarPointAverage(starPointAverage);
        lecture.setTotalReviewCount(totalReviewCount);
        lectureRepository.save(lecture);
    }
}
