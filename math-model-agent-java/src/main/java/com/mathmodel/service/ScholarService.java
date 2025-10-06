package com.mathmodel.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scholar Service - Search and retrieve academic papers
 * Uses OpenAlex API for literature search
 */
@Slf4j
@Service
public class ScholarService {

    private static final String OPENALEX_API = "https://api.openalex.org/works";
    private static final int MAX_RESULTS = 5;
    
    private final RestTemplate restTemplate;

    public ScholarService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Search for relevant papers
     * 
     * @param query Search query (keywords)
     * @param maxResults Maximum number of results
     * @return List of papers
     */
    public List<Paper> searchPapers(String query, int maxResults) {
        List<Paper> papers = new ArrayList<>();
        
        try {
            log.info("[ScholarService] Searching papers for: {}", query);
            
            String url = String.format("%s?search=%s&per_page=%d&sort=cited_by_count:desc",
                    OPENALEX_API,
                    query.replace(" ", "%20"),
                    Math.min(maxResults, MAX_RESULTS)
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("results")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                
                for (Map<String, Object> result : results) {
                    Paper paper = parsePaper(result);
                    if (paper != null) {
                        papers.add(paper);
                    }
                }
                
                log.info("[ScholarService] Found {} papers", papers.size());
            }
            
        } catch (Exception e) {
            log.error("[ScholarService] Error searching papers", e);
            // Return empty list on error
        }
        
        return papers;
    }

    /**
     * Search for papers with default max results
     */
    public List<Paper> searchPapers(String query) {
        return searchPapers(query, MAX_RESULTS);
    }

    /**
     * Format papers as citations
     */
    public String formatCitations(List<Paper> papers) {
        if (papers == null || papers.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 参考文献\n\n");
        
        for (int i = 0; i < papers.size(); i++) {
            Paper paper = papers.get(i);
            sb.append(String.format("[%d] %s. %s. %s. %d. DOI: %s\n\n",
                    i + 1,
                    paper.getAuthors(),
                    paper.getTitle(),
                    paper.getVenue(),
                    paper.getYear(),
                    paper.getDoi() != null ? paper.getDoi() : "N/A"
            ));
        }
        
        return sb.toString();
    }

    /**
     * Parse paper from OpenAlex response
     */
    private Paper parsePaper(Map<String, Object> data) {
        try {
            Paper paper = new Paper();
            
            // Title
            paper.setTitle((String) data.get("title"));
            
            // Authors
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> authorships = 
                    (List<Map<String, Object>>) data.get("authorships");
            if (authorships != null && !authorships.isEmpty()) {
                List<String> authorNames = new ArrayList<>();
                for (Map<String, Object> authorship : authorships) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> author = (Map<String, Object>) authorship.get("author");
                    if (author != null && author.containsKey("display_name")) {
                        authorNames.add((String) author.get("display_name"));
                        if (authorNames.size() >= 3) break; // Limit to 3 authors
                    }
                }
                paper.setAuthors(String.join(", ", authorNames) + (authorNames.size() >= 3 ? ", et al." : ""));
            }
            
            // Publication year
            Object yearObj = data.get("publication_year");
            if (yearObj instanceof Integer) {
                paper.setYear((Integer) yearObj);
            }
            
            // DOI
            paper.setDoi((String) data.get("doi"));
            
            // Venue (journal/conference)
            @SuppressWarnings("unchecked")
            Map<String, Object> primaryLocation = 
                    (Map<String, Object>) data.get("primary_location");
            if (primaryLocation != null && primaryLocation.containsKey("source")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> source = (Map<String, Object>) primaryLocation.get("source");
                if (source != null) {
                    paper.setVenue((String) source.get("display_name"));
                }
            }
            
            // Citation count
            Object citedByCount = data.get("cited_by_count");
            if (citedByCount instanceof Integer) {
                paper.setCitedBy((Integer) citedByCount);
            }
            
            // Abstract
            @SuppressWarnings("unchecked")
            Map<String, Object> abstractInverted = 
                    (Map<String, Object>) data.get("abstract_inverted_index");
            if (abstractInverted != null) {
                // Simplified: just indicate abstract exists
                paper.setAbstractText("Abstract available");
            }
            
            return paper;
            
        } catch (Exception e) {
            log.warn("[ScholarService] Error parsing paper", e);
            return null;
        }
    }

    /**
     * Paper data class
     */
    @Data
    public static class Paper {
        private String title;
        private String authors;
        private Integer year;
        private String doi;
        private String venue;
        private Integer citedBy;
        private String abstractText;
    }
}
