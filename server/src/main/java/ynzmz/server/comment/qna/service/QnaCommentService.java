package ynzmz.server.comment.qna.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ynzmz.server.comment.qna.entity.QnaComment;
import ynzmz.server.comment.qna.repository.QnaCommentRepository;
import ynzmz.server.global.error.exception.BusinessLogicException;
import ynzmz.server.global.error.exception.ExceptionCode;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QnaCommentService {

    private final QnaCommentRepository qnaCommentRepository;

    public QnaComment createQnaComment(QnaComment qnaComment){
        return qnaCommentRepository.save(qnaComment);
    }

    public QnaComment updateQnaComment(QnaComment qnaComment){
        QnaComment findQnaComment = findQnaCommentById(qnaComment.getQnaCommentId());

        Optional.ofNullable(qnaComment.getContent()).ifPresent(findQnaComment::setContent);
        Optional.ofNullable(qnaComment.getModifiedAt()).ifPresent(findQnaComment::setModifiedAt);

        return qnaCommentRepository.save(findQnaComment);
    }

    @Transactional
    public void deleteQnaComment(long qnaCommentId) {
        qnaCommentRepository.deleteById(qnaCommentId);
    }
    public Optional<QnaComment> findOptionalQnaCommentById(long qnaCommentId) {
        return qnaCommentRepository.findById(qnaCommentId);
    }

    public QnaComment findQnaCommentById(long qnaCommentId) {
        Optional<QnaComment> qnaComment = qnaCommentRepository.findById(qnaCommentId);
        return qnaComment.orElseThrow(() -> new BusinessLogicException(ExceptionCode.QNA_COMMENT_NOT_FOUND));
    }

    public List<QnaComment> findQnaCommentByMemberId(long memberId){
        return qnaCommentRepository.findByMemberId(memberId);
    }
}
