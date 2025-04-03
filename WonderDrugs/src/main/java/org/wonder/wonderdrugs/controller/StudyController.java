package org.wonder.wonderdrugs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wonder.wonderdrugs.model.Study;
import org.wonder.wonderdrugs.service.StudyService;

import java.util.List;

@RestController
@RequestMapping("/api/studies")
public class StudyController {

    private final StudyService studyService;

    @Autowired
    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @GetMapping
    public ResponseEntity<List<Study>> getStudies() {
        List<Study> studies = studyService.getStudies();
        return ResponseEntity.ok(studies);
    }
}
