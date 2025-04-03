package org.wonder.wonderdrugs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wonder.wonderdrugs.dto.SiteHealthDTO;
import org.wonder.wonderdrugs.model.Country;
import org.wonder.wonderdrugs.model.Site;
import org.wonder.wonderdrugs.service.SiteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SiteController {

    private final SiteService siteService;

    @Autowired
    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/{studyId}/sites")
    public ResponseEntity<List<Site>> getSites(@PathVariable String studyId) {
        List<Site> sites = siteService.getSites(studyId);
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/{studyId}/countries")
    public ResponseEntity<List<Country>> getCountries(@PathVariable String studyId) {
        List<Country> countries = siteService.getCountries(studyId);
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/status-colors")
    public ResponseEntity<Map<String, String>> getStatusColors() {
        Map<String, String> statusColors = siteService.getStatusColors();
        return ResponseEntity.ok(statusColors);
    }

    @GetMapping("/{studyId}/site-health")
    public ResponseEntity<List<SiteHealthDTO>> getSiteHealth(@PathVariable String studyId) {
        List<SiteHealthDTO> healthData = siteService.getSiteHealthData(studyId);
        return ResponseEntity.ok(healthData);
    }
}
