package com.englishmovies.server.movies.domain;

/**
 * Тип блока сценария. Значения совпадают с полем type в content.json.
 */
public enum ContentBlockType {
    section,
    action,
    scene,
    dialogue,
    transition
}
