package com.englishmovies.server.dictionary.service;

import com.englishmovies.server.dictionary.domain.ContentType;
import com.englishmovies.server.dictionary.converter.DictionaryConverter;
import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.domain.dto.DictionaryRequestDto;
import com.englishmovies.server.dictionary.domain.dto.ExpandedDictionaryDto;
import com.englishmovies.server.dictionary.domain.entity.DictionaryEntity;
import com.englishmovies.server.dictionary.repository.DictionaryRepository;
import com.englishmovies.server.movies.converter.ContentBlockMapper;
import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import com.englishmovies.server.movies.domain.entity.EpisodeContentEntity;
import com.englishmovies.server.movies.domain.entity.MovieContentEntity;
import com.englishmovies.server.movies.repository.EpisodeRepository;
import com.englishmovies.server.movies.repository.MovieContentRepository;
import com.englishmovies.server.movies.repository.MovieRepository;
import com.englishmovies.server.movies.repository.WorkRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final DictionaryConverter dictionaryConverter;
    private final WorkRepository workRepository;
    private final MovieRepository movieRepository;
    private final MovieContentRepository movieContentRepository;
    private final EpisodeRepository episodeRepository;

    @Transactional
    public DictionaryDto save(DictionaryRequestDto request) {
        DictionaryEntity entity = dictionaryConverter.toEntity(request);
        DictionaryEntity saved = dictionaryRepository.save(entity);
        return dictionaryConverter.toDto(saved);
    }

    @Transactional
    public DictionaryDto update(Long id, DictionaryRequestDto request) {
        DictionaryEntity entity = dictionaryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Dictionary entry not found: " + id));
        dictionaryConverter.updateEntity(request, entity);
        DictionaryEntity updated = dictionaryRepository.save(entity);
        return dictionaryConverter.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!dictionaryRepository.existsById(id)) {
            throw new IllegalArgumentException("Dictionary entry not found: " + id);
        }
        dictionaryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DictionaryDto> findAll() {
        return dictionaryRepository.findAll().stream()
            .map(dictionaryConverter::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DictionaryDto> findFirst() {
        return dictionaryRepository.findFirstByOrderByIdAsc()
            .map(dictionaryConverter::toDto);
    }

    /**
     * Поиск записей по value: LIKE %query% без учёта регистра.
     */
    @Transactional(readOnly = true)
    public List<DictionaryDto> searchByValue(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return dictionaryRepository.findByValueContainingIgnoreCase(query.trim()).stream()
            .map(dictionaryConverter::toDto)
            .toList();
    }

    /**
     * Поиск по value с расширением: для каждой записи подставляются title (название фильма/сериала)
     * и block (тот же ContentBlockDto, что в GET /movie-content/.../pages).
     */
    @Transactional(readOnly = true)
    public List<ExpandedDictionaryDto> searchByValueExpanded(String query) {
        List<DictionaryDto> list = searchByValue(query);
        return list.stream()
            .map(this::toExpanded)
            .toList();
    }

    private ExpandedDictionaryDto toExpanded(DictionaryDto dto) {
        String ck = dto.getContentKey();
        String blockId = dto.getBlockId();
        ContentType contentType = dto.getContentType();

        String title = null;
        ContentBlockDto block = null;

        if (ck != null && !ck.isBlank() && contentType != null) {
            title = workRepository.findByContentKey(ck).map(w -> w.getName()).orElse(null);
            if (contentType == ContentType.EPISODE) {
                title = episodeRepository.findByContentKeyWithWorkAndContent(ck)
                    .map(e -> e.getWork() != null ? e.getWork().getName() : null)
                    .orElse(title);
            }
            if (blockId != null && !blockId.isBlank()) {
                if (contentType == ContentType.MOVIE) {
                    block = resolveMovieBlock(ck, blockId);
                } else if (contentType == ContentType.EPISODE) {
                    block = episodeRepository.findByContentKeyWithWorkAndContent(ck)
                        .map(e -> resolveEpisodeBlock(e.getContent(), blockId))
                        .orElse(null);
                }
            }
        }

        return new ExpandedDictionaryDto(dto, title, block);
    }

    private ContentBlockDto resolveMovieBlock(String contentKey, String blockId) {
        return workRepository.findByContentKey(contentKey)
            .flatMap(work -> movieRepository.findByWorkId(work.getId()))
            .flatMap(movie -> movieContentRepository.findByMovieIdAndBlockId(movie.getId(), blockId))
            .map(ContentBlockMapper::fromEntity)
            .orElse(null);
    }

    private ContentBlockDto resolveEpisodeBlock(EpisodeContentEntity contentEntity, String blockId) {
        if (contentEntity == null) return null;
        JsonNode root = contentEntity.getContent();
        if (root == null || root.isNull()) return null;
        JsonNode contentArray = root.path("content");
        if (!contentArray.isArray()) return null;
        for (JsonNode node : contentArray) {
            if (blockId.equals(node.path("id").asText(null))) {
                return ContentBlockMapper.toDto(node);
            }
        }
        return null;
    }
}
