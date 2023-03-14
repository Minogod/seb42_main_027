package ynzmz.server.teacher.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ynzmz.server.dto.MultiResponseDto;
import ynzmz.server.dto.SingleResponseDto;
import ynzmz.server.tag.entity.GradeTag;
import ynzmz.server.tag.entity.PlatformTag;
import ynzmz.server.tag.entity.SubjectTag;
import ynzmz.server.tag.service.TagService;
import ynzmz.server.teacher.dto.TeacherDto;
import ynzmz.server.teacher.entity.Teacher;
import ynzmz.server.teacher.mapper.TeacherMapper;
import ynzmz.server.teacher.service.TeacherService;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class TeacherController {
    private final TeacherService teacherService;
    private final TeacherMapper teacherMapper;
    private final TagService tagService;
    //강사등록
    @PostMapping
    public ResponseEntity<?> postTeacher(@RequestBody TeacherDto.Post teacherPost){
        //태그를 받았을때 태그객체 생성 및 저장 필요
        Teacher teacher = teacherMapper.teacherToTeacherPost(teacherPost);
        Teacher createdTeacher = teacherService.createTeacher(teacher);

        //학년,과목,플랫폼 Tag 찾기 ( String -> 저장된 객체 )
        List<GradeTag.Grade> gradeTags = tagService.findGradeTags(teacherPost.getGradeTag());
        List<PlatformTag.Platform> platformTags = tagService.findPlatformTags(teacherPost.getPlatformTag());
        List<SubjectTag.Subject> subjectTags = tagService.findSubjectTags(teacherPost.getSubjectTag());

        //생성된 강사 맵핑테이블 생성
        tagService.createTeacherTag(createdTeacher, gradeTags, platformTags, subjectTags);

        TeacherDto.SimpleInfoResponse response = teacherMapper.teacherInfoResponseToTeacher(teacher);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }
    //강사수정
    @PatchMapping("/{teacher-id}")
    public ResponseEntity<?> patchTeacher(@PathVariable("teacher-id") long teacherId,
                             @RequestBody TeacherDto.Patch teacherPatch) {
        Teacher teacher = teacherMapper.teacherToTeacherPatch(teacherPatch);
        teacher.setTeacherId(teacherId);
        Teacher updatedTeacher = teacherService.updateTeacher(teacher);

        //학년,과목,플랫폼 Tag 찾기 ( String -> 저장된 객체 )
        List<GradeTag.Grade> gradeTags = tagService.findGradeTags(teacherPatch.getGradeTag());
        List<PlatformTag.Platform> platformTags = tagService.findPlatformTags(teacherPatch.getPlatformTag());
        List<SubjectTag.Subject> subjectTags = tagService.findSubjectTags(teacherPatch.getSubjectTag());

        //태그 수정방법 : 저장값 전부 삭제후 재등록
        tagService.deleteAllTeacherTagByTeacher(updatedTeacher);
        //생성된 강사 맵핑테이블 생성
        tagService.createTeacherTag(updatedTeacher, gradeTags, platformTags, subjectTags);

        TeacherDto.SimpleInfoResponse response = teacherMapper.teacherInfoResponseToTeacher(updatedTeacher);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }
    //강사 리스트 페이지  = 필터 과목별 강사조회 + 학년별 + 플랫폼별  + 강사 전체 조회
    @GetMapping
    public ResponseEntity<?> getTeacherListPage(@RequestParam(required = false) String grade,
                                                @RequestParam(required = false) String platform,
                                                @RequestParam(required = false) String subject,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(required = false) String reverse,
                                                @RequestParam int page,
                                                @RequestParam int size){

        log.info("grade : " + grade + ", platform : " + platform + ", subject :  " + subject + ", name = " + name);
        if(sort == null) sort = "teacherId";
        GradeTag.Grade gradeTag;
        PlatformTag.Platform platformTag;
        SubjectTag.Subject subjectTag;
        if(grade != null) {
            gradeTag = tagService.findGradeTag(grade);
        } else gradeTag = null;
        if(platform != null) {
            platformTag = tagService.findPlatformTag(platform);
        } else  platformTag = null;
        if(subject != null) {
            subjectTag = tagService.findSubjectTag(subject);
        } else subjectTag = null;

        Page<Teacher> teacherPage;

        if(reverse != null) {
            teacherPage = teacherService.findTeachers(gradeTag, platformTag, subjectTag, name, sort, reverse,page - 1, size);

        } else {
            teacherPage = teacherService.findTeachers(gradeTag, platformTag, subjectTag, name, sort,page - 1, size);
        }
        List<Teacher> teachers = teacherPage.getContent();
        List<TeacherDto.ListPageResponse> responses = teacherMapper.teacherListPageResponsesToTeachers(teachers);
        return new ResponseEntity<>(new MultiResponseDto<>(responses, teacherPage), HttpStatus.OK);
    }
    //강사 상세조회
    @GetMapping("/{teacher-id}")
    public ResponseEntity<?> getTeacherDetail(@PathVariable("teacher-id") long teacherId){
        Teacher teacher = teacherService.findTeacherById(teacherId);
        TeacherDto.SimpleInfoResponse response = teacherMapper.teacherInfoResponseToTeacher(teacher);
        return new ResponseEntity<>(new SingleResponseDto<>(response),HttpStatus.OK);
    }
    //강사 삭제
    @DeleteMapping("/{teacher-id}")
    public void deleteTeacher(@PathVariable("teacher-id") long teacherId){
        teacherService.deleteTeacher(teacherId);
    }
}
