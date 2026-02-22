package com.englishmovies.server.movies.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Script credits (writtenBy, storyBy, directedBy, source, scriptDate). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditsDto {
    private final String writtenBy;
    private final String storyBy;
    private final String directedBy;
    private final String source;
    private final String scriptDate;

    public CreditsDto(String writtenBy, String storyBy, String directedBy, String source, String scriptDate) {
        this.writtenBy = writtenBy;
        this.storyBy = storyBy;
        this.directedBy = directedBy;
        this.source = source;
        this.scriptDate = scriptDate;
    }

    public String getWrittenBy() { return writtenBy; }
    public String getStoryBy() { return storyBy; }
    public String getDirectedBy() { return directedBy; }
    public String getSource() { return source; }
    public String getScriptDate() { return scriptDate; }
}
