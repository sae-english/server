package com.englishmovies.server.movies.domain.dto;

import lombok.Value;

import java.util.List;

/**
 * Одна страница контента фильма (пагинация по курсору).
 * nextCursor — block_id последнего блока в content; передать в after для следующей страницы. null — больше страниц нет.
 */
@Value
public class MovieContentPageDto {
    Long movieId;
    String contentKey;
    CreditsDto credits;
    String note;
    List<ContentBlockDto> content;
    /** block_id последнего блока; передать в ?after= для следующей страницы. null если hasMore == false. */
    String nextCursor;
    boolean hasMore;
}
