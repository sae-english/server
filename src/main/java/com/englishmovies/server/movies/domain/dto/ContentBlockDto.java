package com.englishmovies.server.movies.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One script block: type = section | action | scene | dialogue | transition. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentBlockDto {
    private final String type;
    private final String id;
    private final String title;
    private final String text;
    private final String description;
    private final String speaker;
    private final String parenthetical;

    public ContentBlockDto(String type, String id, String title, String text,
                           String description, String speaker, String parenthetical) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.text = text;
        this.description = description;
        this.speaker = speaker;
        this.parenthetical = parenthetical;
    }

    public String getType() { return type; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getDescription() { return description; }
    public String getSpeaker() { return speaker; }
    public String getParenthetical() { return parenthetical; }
}
