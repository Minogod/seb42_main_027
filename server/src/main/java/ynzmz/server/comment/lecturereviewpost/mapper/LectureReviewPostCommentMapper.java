package ynzmz.server.comment.lecturereviewpost.mapper;

import org.mapstruct.Mapper;
import ynzmz.server.comment.lecturereviewpost.dto.LectureReviewPostCommentDto;
import ynzmz.server.comment.lecturereviewpost.entity.LectureReviewPostComment;

@Mapper(componentModel = "spring")
public interface LectureReviewPostCommentMapper {
    LectureReviewPostComment lectureReviewPostCommentToLectureReviewPostCommentPost(LectureReviewPostCommentDto.Post lectureReviewPostCommentPostDto);
    LectureReviewPostComment lectureReviewPostCommentToLectureReviewPostCommentPatch(LectureReviewPostCommentDto.Patch lectureReviewPostCommentPatchDto);
    LectureReviewPostCommentDto.Response lectureReviewPostCommentResponseToLectureReviewPostComment(LectureReviewPostComment lectureReviewPostComment);
}
