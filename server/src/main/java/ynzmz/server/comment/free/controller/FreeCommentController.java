package ynzmz.server.comment.free.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ynzmz.server.comment.free.dto.FreeCommentDto;
import ynzmz.server.comment.free.entity.FreeComment;
import ynzmz.server.comment.free.mapper.FreeCommentMapper;
import ynzmz.server.comment.free.service.FreeCommentService;
import ynzmz.server.dto.MultiResponseDto;
import ynzmz.server.dto.SingleResponseDto;
import ynzmz.server.member.entity.Member;
import ynzmz.server.member.service.MemberService;
import ynzmz.server.vote.VoteCount;

import java.util.List;

@RestController
@RequestMapping("/comments/frees")
@RequiredArgsConstructor
public class FreeCommentController  {
    private final MemberService memberService;
    private final FreeCommentMapper freeCommentMapper;
    private final FreeCommentService freeCommentService;
    @PostMapping
    public ResponseEntity<?> createFreeComment(@RequestBody FreeCommentDto.Post postDto) {
        FreeComment freeComment = freeCommentMapper.freeCommentPostToFreeComment(postDto);
        Member member = memberService.findMemberById(postDto.getMemberId());
        freeComment.setMember(member);

        FreeComment createFreeComment = freeCommentService.creatFreeComment(freeComment);
        FreeCommentDto.Response response = freeCommentMapper.freeCommentToFreeCommentResponse(createFreeComment);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    @PatchMapping("/{free-comment-id}")
    public ResponseEntity<?> updateFreeComment(@RequestBody FreeCommentDto.Patch patchDto,
                                                        @PathVariable("free-comment-id") long freePostCommentId) {
        FreeComment freeComment = freeCommentMapper.freeCommentPatchToFreeComment(patchDto);
        freeComment.setFreeCommentId(freePostCommentId);

        FreeComment updateFreeComment = freeCommentService.updateFreeComment(freeComment);
        FreeCommentDto.Response response = freeCommentMapper.freeCommentToFreeCommentResponse(updateFreeComment);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

//게시글별 코멘트 받아오기
//    @GetMapping
//    public ResponseEntity<?> getFreeComments(@RequestParam(value = "filter", required = false) String filter,
//                                                      @RequestParam long freeId,
//                                                      @RequestParam int page,
//                                                      @RequestParam int size) {
//
//        if (filter == null){
//            filter = "freeCommentId";
//        }
//        Page<FreeComment> pageFreePostComments = freeCommentService.getFrees(freeId, filter, page - 1, size);
//        List<FreeComment> freeComments = pageFreePostComments.getContent();
//
//        List<FreeCommentDto.Response> responses = freeCommentMapper.freeCommentToFreeCommentsResponses(freeComments);
//
//        return new ResponseEntity<>(new MultiResponseDto<>(responses, pageFreePostComments), HttpStatus.OK);
//    }

    @DeleteMapping("/{free-comment-id}")
    public void deleteFreeComment(@PathVariable("free-comment-id") long freeCommentId) {
        freeCommentService.deleteFreeComment(freeCommentId);
    }
}
