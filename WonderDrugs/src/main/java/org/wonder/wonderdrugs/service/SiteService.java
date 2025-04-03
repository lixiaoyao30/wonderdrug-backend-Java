package org.wonder.wonderdrugs.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wonder.wonderdrugs.dto.SiteHealthDTO;
import org.wonder.wonderdrugs.model.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteService {
    private final VaultService vaultService;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // ================ Constants ================
    private static final String DEFAULT_COLOR = "#CCCCCC";
    private static final Map<String, String> DEFAULT_COLORS = Map.of(
            SiteStatus.ACTIVE.getValue(), "#4CAF50",     // Green
            SiteStatus.INACTIVE.getValue(), "#F44336",   // Red
            SiteStatus.ON_HOLD.getValue(), "#FF9800",    // Orange
            SiteStatus.COMPLETED.getValue(), "#2196F3",  // Blue
            SiteStatus.SUSPENDED.getValue(), "#FFC107",  // Yellow
            SiteStatus.TERMINATED.getValue(), "#9C27B0"  // Purple
    );

    // ================ Site Methods ================
    public List<Site> getSites(String studyId) {
        log.info("Fetching sites for study: {}", studyId);

        try {
            JsonNode siteData = fetchSiteData(studyId);
            Map<String, Map<String, String>> countryData = fetchCountryData(collectCountryIds(siteData));
            Map<String, String> statusColors = getStatusColors();

            return processSiteData(siteData, countryData, statusColors);
        } catch (Exception e) {
            log.error("Error fetching sites: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, Map<String, String>> fetchCountryData(Set<String> countryIds) {
        Map<String, Map<String, String>> countryData = new HashMap<>();

        if (countryIds.isEmpty()) {
            return countryData;
        }

        for (String countryId : countryIds) {
            String query = """
            SELECT id, name__v, code__sys 
            FROM country__v 
            WHERE id = '%s'
            """.formatted(countryId);

            try {
                JsonNode countryNodes = vaultService.executeQuery(query);
                if (countryNodes.size() > 0) {
                    JsonNode node = countryNodes.get(0);
                    Map<String, String> countryInfo = new HashMap<>();
                    countryInfo.put("name__v", getNodeText(node, "name__v"));
                    countryInfo.put("code__sys", getNodeText(node, "code__sys"));
                    countryData.put(countryId, countryInfo);
                    log.debug("Fetched country info for ID {}: {}", countryId, countryInfo);
                }
            } catch (Exception e) {
                log.warn("Error fetching country {}: {}", countryId, e.getMessage());
            }
        }

        log.info("Fetched data for {} countries", countryData.size());
        return countryData;
    }


    private JsonNode fetchSiteData(String studyId) {
        String query = """
            SELECT id, name__v, site_name__v, site_status__v, status__v,
                   latitude__c, longitude__c, link__sys,
                   principal_investigator__v, planned_greenlight_date__v,
                   actual_siv__v, days_to_greenlight__v, country__v
            FROM site__v
            WHERE study__v = '%s'
            """.formatted(studyId);
        return vaultService.executeQuery(query);
    }

    private Set<String> collectCountryIds(JsonNode siteData) {
        return StreamSupport.stream(siteData.spliterator(), false)
                .map(node -> getNodeText(node, "country__v"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<Site> processSiteData(JsonNode siteData,
                                       Map<String, Map<String, String>> countryData,
                                       Map<String, String> statusColors) {
        List<Site> sites = new ArrayList<>();
        int sitesWithCoordinates = 0;
        int sitesWithoutCoordinates = 0;

        for (JsonNode node : siteData) {
            Site site = createSite(node, countryData, statusColors);
            if (site != null) {
                sites.add(site);
                sitesWithCoordinates++;
            } else {
                sitesWithoutCoordinates++;
            }
        }

        log.info("Processed sites - Total: {}, With coordinates: {}, Without coordinates: {}",
                siteData.size(), sitesWithCoordinates, sitesWithoutCoordinates);
        return sites;
    }

    private Site createSite(JsonNode node,
                            Map<String, Map<String, String>> countryData,
                            Map<String, String> statusColors) {
        try {
            Site site = new Site();

            // Basic info
            site.setId(getNodeText(node, "id"));
            site.setNumber(getNodeText(node, "name__v"));
            site.setName(getNodeText(node, "site_name__v", getNodeText(node, "name__v")));

            // Status and color
            String siteStatus = getFirstArrayElement(node, "site_status__v");
            if (siteStatus != null) {
                site.setSiteStatus(siteStatus);
                site.setStatusColor(statusColors.getOrDefault(siteStatus, DEFAULT_COLOR));
            }
            site.setStatus(getFirstArrayElement(node, "status__v"));

            // Country info
            setCountryInfo(site, node, countryData);

            // Coordinates
            if (!setCoordinates(site, node)) {
                return null;
            }

            // Dates
            setDates(site, node);

            // Additional info
            site.setDaysToGreenlight(getNodeInt(node, "days_to_greenlight__v"));
            site.setInvestigatorId(getNodeText(node, "principal_investigator__v"));
            site.setVaultUrl(constructVaultUrl(node, "site", site.getId()));

            return site;
        } catch (Exception e) {
            log.warn("Error creating site from node: {}", e.getMessage());
            return null;
        }
    }

    // ================ Country Methods ================
    public List<Country> getCountries(String studyId) {
        log.info("Fetching countries for study: {}", studyId);

        try {
            Set<String> countryIds = fetchStudyCountryIds(studyId);
            if (countryIds.isEmpty()) {
                return Collections.emptyList();
            }

            Map<String, Country> countryMap = fetchCountryDetails(countryIds);
            updateCountryStatuses(countryMap, studyId);

            return new ArrayList<>(countryMap.values());
        } catch (Exception e) {
            log.error("Error fetching countries: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Set<String> fetchStudyCountryIds(String studyId) {
        String query = "SELECT country__v FROM site__v WHERE study__v = '%s'".formatted(studyId);
        JsonNode nodes = vaultService.executeQuery(query);

        return StreamSupport.stream(nodes.spliterator(), false)
                .map(node -> getNodeText(node, "country__v"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map<String, Country> fetchCountryDetails(Set<String> countryIds) {
        Map<String, Country> countryMap = new HashMap<>();

        for (String countryId : countryIds) {
            String query = """
                SELECT id, name__v, code__sys, abbreviation__c, link__sys
                FROM country__v
                WHERE id = '%s'
                """.formatted(countryId);

            try {
                JsonNode nodes = vaultService.executeQuery(query);
                if (nodes.size() > 0) {
                    Country country = createCountry(nodes.get(0));
                    countryMap.put(country.getId(), country);
                }
            } catch (Exception e) {
                log.warn("Error fetching country details for ID {}: {}", countryId, e.getMessage());
            }
        }
        return countryMap;
    }

    private Country createCountry(JsonNode node) {
        Country country = new Country();
        country.setId(getNodeText(node, "id"));
        country.setName(getNodeText(node, "name__v", "Unknown Country"));
        country.setCode(getNodeText(node, "code__sys", "??"));
        country.setAbbreviation(getNodeText(node, "abbreviation__c"));
        country.setVaultUrl(constructVaultUrl(node, "country", country.getId()));
        return country;
    }

    private void updateCountryStatuses(Map<String, Country> countryMap, String studyId) {
        for (String countryId : countryMap.keySet()) {
            String query = """
                SELECT status__v
                FROM study_country__v
                WHERE study__v = '%s' AND country__v = '%s'
                """.formatted(studyId, countryId);

            try {
                JsonNode nodes = vaultService.executeQuery(query);
                if (nodes.size() > 0) {
                    String status = getFirstArrayElement(nodes.get(0), "status__v");
                    countryMap.get(countryId).setStatus(status);
                }
            } catch (Exception e) {
                log.warn("Error fetching status for country {}: {}", countryId, e.getMessage());
            }
        }
    }

    // ================ Status Color Methods ================
    public Map<String, String> getStatusColors() {
        try {
            String query = "SELECT site_status__c, status_color__c FROM site_status_configuration__c";
            JsonNode colorData = vaultService.executeQuery(query);

            Map<String, String> colors = new HashMap<>(DEFAULT_COLORS);
            StreamSupport.stream(colorData.spliterator(), false)
                    .forEach(node -> {
                        String status = getNodeText(node, "site_status__c");
                        String color = getNodeText(node, "status_color__c");
                        if (status != null && color != null) {
                            colors.put(status, color);
                        }
                    });
            return colors;
        } catch (Exception e) {
            log.error("Error fetching status colors", e);
            return new HashMap<>(DEFAULT_COLORS);
        }
    }

    public List<StatusColor> getAllStatusColors() {
        try {
            String query = """
                SELECT name__v, site_status__c, status_color__c
                FROM site_status_configuration__c
                WHERE status__v = 'ACTIVE'
                """;
            JsonNode colorData = vaultService.executeQuery(query);

            List<StatusColor> statusColors = StreamSupport.stream(colorData.spliterator(), false)
                    .map(this::createStatusColor)
                    .collect(Collectors.toList());

            return !statusColors.isEmpty() ? statusColors : createDefaultStatusColors();
        } catch (Exception e) {
            log.error("Error fetching all status colors", e);
            return createDefaultStatusColors();
        }
    }

    // ================ Utility Methods ================
    private String getNodeText(JsonNode node, String field) {
        return getNodeText(node, field, null);
    }

    private String getNodeText(JsonNode node, String field, String defaultValue) {
        return !node.path(field).isMissingNode() && !node.path(field).isNull()
                ? node.path(field).asText()
                : defaultValue;
    }

    private String getFirstArrayElement(JsonNode node, String field) {
        return node.path(field).isArray() && node.path(field).size() > 0
                ? node.path(field).get(0).asText()
                : null;
    }

    private Integer getNodeInt(JsonNode node, String field) {
        return !node.path(field).isMissingNode() && !node.path(field).isNull()
                ? node.path(field).asInt()
                : null;
    }

    private Double getNodeDouble(JsonNode node, String field) {
        return !node.path(field).isMissingNode() && !node.path(field).isNull()
                ? node.path(field).asDouble()
                : null;
    }

    private void setCountryInfo(Site site, JsonNode node, Map<String, Map<String, String>> countryData) {
        String countryId = getNodeText(node, "country__v");
        site.setCountryId(countryId);
        if (countryData.containsKey(countryId)) {
            Map<String, String> countryInfo = countryData.get(countryId);
            site.setCountry(countryInfo.get("name__v"));
            site.setCountryCode(countryInfo.get("code__sys"));
        }
    }

    private boolean setCoordinates(Site site, JsonNode node) {
        Double latitude = getNodeDouble(node, "latitude__c");
        Double longitude = getNodeDouble(node, "longitude__c");
        if (latitude == null || longitude == null) {
            return false;
        }
        site.setLatitude(latitude);
        site.setLongitude(longitude);
        return true;
    }

    private void setDates(Site site, JsonNode node) {
        try {
            String plannedDate = getNodeText(node, "planned_greenlight_date__v");
            if (plannedDate != null) {
                site.setPlannedGreenlightDate(DATE_FORMAT.parse(plannedDate));
            }

            String actualDate = getNodeText(node, "actual_siv__v");
            if (actualDate != null) {
                site.setActualSivDate(DATE_FORMAT.parse(actualDate));
            }
        } catch (Exception e) {
            log.warn("Error parsing dates for site {}: {}", site.getId(), e.getMessage());
        }
    }

    private String constructVaultUrl(JsonNode node, String type, String id) {
        String baseUrl = vaultService.getVaultWebUrl();
        String prefix = id.length() >= 3 ? id.substring(0, 3) : "00C";
        return baseUrl + "/#v/" + prefix + "/" + id;
    }

    private StatusColor createStatusColor(JsonNode node) {
        return new StatusColor(
                getNodeText(node, "name__v"),
                getNodeText(node, "site_status__c"),
                getNodeText(node, "status_color__c")
        );
    }

    private List<StatusColor> createDefaultStatusColors() {
        return Arrays.stream(SiteStatus.values())
                .map(status -> new StatusColor(
                        status.name(),
                        status.getValue(),
                        DEFAULT_COLORS.get(status.getValue())
                ))
                .collect(Collectors.toList());
    }

    // ... existing code ...

    public List<SiteHealthDTO> getSiteHealthData(String studyId) {
        log.info("Fetching site health data for study: {}", studyId);

        try {
            // 获取站点数据
            List<Site> sites = getSites(studyId);
            if (sites.isEmpty()) {
                log.info("No sites found for study: {}", studyId);
                return Collections.emptyList();
            }
            List<SiteHealthDTO> healthData = new ArrayList<>();

            for (Site site : sites) {
                try {
                    SiteHealthDTO healthDTO = calculateSiteHealth(site);
                    healthData.add(healthDTO);
                } catch (Exception e) {
                    // 单个站点处理错误不应影响整个列表
                    log.warn("Error calculating health for site {}: {}", site.getId(), e.getMessage());
                }
            }

            log.info("Generated health data for {} sites", healthData.size());
            return healthData;
        } catch (Exception e) {
            log.error("Error generating site health data: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private SiteHealthDTO calculateSiteHealth(Site site) {
        SiteHealthDTO healthDTO = new SiteHealthDTO();

        // 基本信息
        healthDTO.setId(site.getId());
        healthDTO.setName(site.getName());
        healthDTO.setNumber(site.getNumber());
        healthDTO.setStatus(site.getStatus());
        healthDTO.setCountryId(site.getCountryId());
        healthDTO.setCountry(site.getCountry());
        healthDTO.setVaultUrl(site.getVaultUrl());
        healthDTO.setLastUpdated(new Date());

        // 计算健康分数 (示例算法，可根据实际需求调整)
        int healthScore = calculateHealthScore(site);
        healthDTO.setHealthScore(healthScore);

        // 设置健康状态
        if (healthScore >= 80) {
            healthDTO.setHealthStatus("良好");
        } else if (healthScore >= 60) {
            healthDTO.setHealthStatus("一般");
        } else {
            healthDTO.setHealthStatus("需关注");
        }

        // 设置趋势 (这里使用随机值模拟，实际应基于历史数据)
        String[] trends = {"上升", "下降", "稳定"};
        healthDTO.setTrend(trends[new Random().nextInt(trends.length)]);

        // 设置问题列表
        healthDTO.setIssues(identifyIssues(site, healthScore));

        return healthDTO;
    }

    private int calculateHealthScore(Site site) {
        // 基础分数
        int baseScore = 50;

        // 根据站点状态调整分数
        if ("active__v".equals(site.getStatus())) {
            baseScore += 30;
        } else if ("completed__v".equals(site.getStatus())) {
            baseScore += 40;
        } else if ("on_hold__v".equals(site.getStatus())) {
            baseScore -= 10;
        } else if ("suspended__v".equals(site.getStatus())) {
            baseScore -= 20;
        } else if ("terminated__v".equals(site.getStatus())) {
            baseScore -= 30;
        }

        // 根据其他因素调整分数
        if (site.getDaysToGreenlight() != null) {
            if (site.getDaysToGreenlight() <= 30) {
                baseScore += 10;
            } else if (site.getDaysToGreenlight() > 60) {
                baseScore -= 10;
            }
        }

        // 确保分数在0-100范围内
        return Math.max(0, Math.min(100, baseScore));
    }

    private List<String> identifyIssues(Site site, int healthScore) {
        List<String> issues = new ArrayList<>();

        // 根据健康分数和站点状态识别问题
        if (healthScore < 60) {
            issues.add("健康指数低");
        }

        if ("on_hold__v".equals(site.getStatus())) {
            issues.add("站点处于搁置状态");
        } else if ("suspended__v".equals(site.getStatus())) {
            issues.add("站点已暂停");
        } else if ("terminated__v".equals(site.getStatus())) {
            issues.add("站点已终止");
        }

        if (site.getDaysToGreenlight() != null && site.getDaysToGreenlight() > 60) {
            issues.add("启动时间过长");
        }

        // 如果没有问题，添加一个正面评价
        if (issues.isEmpty()) {
            issues.add("站点运行良好");
        }

        return issues;
    }

}
