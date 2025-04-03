package org.wonder.wonderdrugs.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wonder.wonderdrugs.model.Study;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyService {
    private static final Logger logger = LoggerFactory.getLogger(StudyService.class);

    private final VaultService vaultService;

    @Autowired
    public StudyService(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    public List<Study> getStudies() {
        logger.info("Fetching studies");

        String query = "SELECT id, name__v, study_name__v, status__v, study_phase__v, study_type__v " +
                "FROM study__v " +
                "WHERE status__v != 'Canceled'";

        List<Study> studies = new ArrayList<>();

        try {
            JsonNode data = vaultService.executeQuery(query);

            for (JsonNode node : data) {
                Study study = new Study();
                study.setId(node.path("id").asText());
                study.setNumber(node.path("name__v").asText());
                study.setName(node.path("study_name__v").asText());

                if (node.path("status__v").isArray() && node.path("status__v").size() > 0) {
                    study.setStatus(node.path("status__v").get(0).asText());
                }

                if (node.path("study_phase__v").isArray() && node.path("study_phase__v").size() > 0) {
                    study.setPhase(node.path("study_phase__v").get(0).asText());
                }

                if (node.path("study_type__v").isArray() && node.path("study_type__v").size() > 0) {
                    study.setType(node.path("study_type__v").get(0).asText());
                }

                // 设置Vault URL
                study.setVaultUrl(vaultService.getVaultWebUrl() + "/#study/" + study.getId() + "/details");

                studies.add(study);
            }

            logger.info("Found {} studies", studies.size());
            return studies;
        } catch (Exception e) {
            logger.error("Error fetching studies", e);
            return new ArrayList<>();
        }
    }
}
