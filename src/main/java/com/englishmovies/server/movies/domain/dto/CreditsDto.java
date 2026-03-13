package com.englishmovies.server.movies.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Script credits (writtenBy, storyBy, directedBy, source, scriptDate). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditsDto {
    private String writtenBy;
    private String storyBy;
    private String directedBy;
    private String source;
    private String scriptDate;

    public CreditsDto() {
        // default constructor for Jackson
    }

    public CreditsDto(String writtenBy, String storyBy, String directedBy, String source, String scriptDate) {
        this.writtenBy = writtenBy;
        this.storyBy = storyBy;
        this.directedBy = directedBy;
        this.source = source;
        this.scriptDate = scriptDate;
    }

    public String getWrittenBy() { return writtenBy; }
    public void setWrittenBy(String writtenBy) { this.writtenBy = writtenBy; }

    public String getStoryBy() { return storyBy; }
    public void setStoryBy(String storyBy) { this.storyBy = storyBy; }

    public String getDirectedBy() { return directedBy; }
    public void setDirectedBy(String directedBy) { this.directedBy = directedBy; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getScriptDate() { return scriptDate; }
    public void setScriptDate(String scriptDate) { this.scriptDate = scriptDate; }
}
