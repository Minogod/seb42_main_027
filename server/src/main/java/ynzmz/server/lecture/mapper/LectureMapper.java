package ynzmz.server.lecture.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ynzmz.server.lecture.dto.LectureDto;
import ynzmz.server.lecture.entity.Lecture;
import ynzmz.server.tag.dto.GradeTagDto;
import ynzmz.server.tag.dto.PlatformTagDto;
import ynzmz.server.tag.dto.SubjectTagDto;
import ynzmz.server.tag.entity.GradeTag;
import ynzmz.server.tag.entity.PlatformTag;
import ynzmz.server.tag.entity.SubjectTag;
import ynzmz.server.tag.mappingtable.lecture.LectureGradeTag;
import ynzmz.server.tag.mappingtable.lecture.LecturePlatformTag;
import ynzmz.server.tag.mappingtable.lecture.LectureSubjectTag;
import ynzmz.server.tag.mappingtable.teacher.TeacherGradeTag;
import ynzmz.server.tag.mappingtable.teacher.TeacherPlatformTag;
import ynzmz.server.tag.mappingtable.teacher.TeacherSubjectTag;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LectureMapper {
    Lecture lectureToLecturePost(LectureDto.Post lecturePost);
    Lecture lectureToLecturePatch(LectureDto.Patch lecurePatch);
    LectureDto.SimpleInfoResponse lectureInfoResponseToLecture(Lecture lecture);
    LectureDto.ListPageResponse lectureListPageResponseToLecture(Lecture lecture);
    List<LectureDto.ListPageResponse> lectureListPageResponsesToLectures(List<Lecture> lectures);
    LectureDto.DetailPageResponse lectureDetailPageResponseToLecture(Lecture lecture);
    LectureDto.TeacherReviewDetailPageResponse lectureTeacherReviewDetailPageResponseToLecture(Lecture lecture);

    default GradeTag.Grade map(GradeTag gradeTag) {
        return gradeTag.getGrade();
    }
    default SubjectTag.Subject map(SubjectTag subjectTag) {
        return subjectTag.getSubject();
    }
    default PlatformTag.Platform map(PlatformTag platformTag) {
        return platformTag.getPlatform();
    }

    default GradeTagDto.Response LectureGradeTagResponseToLectureGradeTag(LectureGradeTag lectureGradeTag) {
        if ( lectureGradeTag == null ) return null;

        GradeTagDto.Response response = new GradeTagDto.Response();
        response.setGradeTag( lectureGradeTag.getGradeTag().getGrade() );

        return response;
    }

    default PlatformTagDto.Response LecturePlatformTagResponseToLecturePlatformTag(LecturePlatformTag lecturePlatformTag) {
        if ( lecturePlatformTag == null ) return null;

        PlatformTagDto.Response response = new PlatformTagDto.Response();

        response.setPlatformTag( lecturePlatformTag.getPlatformTag().getPlatform() );

        return response;
    }

    default SubjectTagDto.Response LectureSubjectTagResponseToLectureSubjectTag(LectureSubjectTag lectureSubjectTag) {
        if ( lectureSubjectTag == null ) return null;

        SubjectTagDto.Response response = new SubjectTagDto.Response();

        response.setSubjectTag( lectureSubjectTag.getSubjectTag().getSubject() );

        return response;
    }
}
