package com.englishmovies.server.comedy.service;

import com.englishmovies.server.comedy.domain.dto.ComedySpecialDto;
import com.englishmovies.server.comedy.domain.dto.ComedySpecialFullDto;
import com.englishmovies.server.comedy.domain.entity.ComedyContentEntity;
import com.englishmovies.server.comedy.domain.entity.ComedySpecialEntity;
import com.englishmovies.server.comedy.repository.ComedyContentRepository;
import com.englishmovies.server.comedy.repository.ComedySpecialRepository;
import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComedyService {

    private final ComedySpecialRepository comedySpecialRepository;
    private final ComedyContentRepository comedyContentRepository;

    @Transactional(readOnly = true)
    public List<ComedySpecialDto> getSpecials(int limit) {
        int size = Math.min(Math.max(1, limit), 100);
        return comedySpecialRepository.findRandomSpecials(size).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ComedySpecialFullDto> getSpecialById(Long id) {
        return comedySpecialRepository.findById(id)
                .map(this::toFullDto);
    }

    private ComedySpecialDto toDto(ComedySpecialEntity e) {
        return new ComedySpecialDto(
                e.getId(),
                e.getName(),
                e.getContentKey(),
                e.getPerformer(),
                e.getYear(),
                e.getDescription()
        );
    }

    private ComedySpecialFullDto toFullDto(ComedySpecialEntity e) {
        List<ContentBlockDto> blocks = comedyContentRepository.findByComedySpecialIdOrderByPosition(e.getId()).stream()
                .map(this::toBlockDto)
                .toList();
        return new ComedySpecialFullDto(
                e.getId(),
                e.getName(),
                e.getContentKey(),
                e.getPerformer(),
                e.getYear(),
                e.getDescription(),
                e.getNote(),
                blocks
        );
    }

    private ContentBlockDto toBlockDto(ComedyContentEntity b) {
        return new ContentBlockDto(
                b.getBlockType(),
                b.getBlockId(),
                b.getTitle(),
                b.getText(),
                null,
                null,
                null
        );
    }
}
