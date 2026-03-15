package com.englishmovies.server.dictionary.service;

import com.englishmovies.server.dictionary.domain.ContentType;
import com.englishmovies.server.dictionary.converter.DictionaryConverter;
import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.domain.dto.DictionaryRequestDto;
import com.englishmovies.server.dictionary.domain.dto.ExpandedDictionaryDto;
import com.englishmovies.server.dictionary.domain.entity.DictionaryEntity;
import com.englishmovies.server.dictionary.repository.DictionaryRepository;
import com.englishmovies.server.book.repository.BookContentRepository;
import com.englishmovies.server.book.repository.BookRepository;
import com.englishmovies.server.comedy.repository.ComedyContentRepository;
import com.englishmovies.server.comedy.repository.ComedySpecialRepository;
import com.englishmovies.server.movies.converter.ContentBlockMapper;
import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import com.englishmovies.server.movies.domain.entity.MovieContentEntity;
import com.englishmovies.server.movies.repository.EpisodeContentRepository;
import com.englishmovies.server.movies.repository.EpisodeRepository;
import com.englishmovies.server.movies.repository.MovieContentRepository;
import com.englishmovies.server.movies.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final DictionaryConverter dictionaryConverter;
    private final MovieRepository movieRepository;
    private final MovieContentRepository movieContentRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeContentRepository episodeContentRepository;
    private final ComedySpecialRepository comedySpecialRepository;
    private final ComedyContentRepository comedyContentRepository;
    private final BookRepository bookRepository;
    private final BookContentRepository bookContentRepository;

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
     * Следующая запись для Telegram: сначала записи без lastSentAt, затем с наименьшим lastSentAt.
     */
    @Transactional(readOnly = true)
    public Optional<DictionaryDto> findNextForTelegram() {
        return dictionaryRepository.findNextForTelegram(PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .map(dictionaryConverter::toDto);
    }

    @Transactional
    public void markSent(Long id, Instant sentAt) {
        dictionaryRepository.findById(id).ifPresent(entity -> {
            entity.setLastSentAt(sentAt);
        });
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
     * Поиск по value с расширением: для каждой записи подставляются title (название фильма/сериала/комедии)
     * и block (тот же ContentBlockDto, что в GET /movie-content/.../pages).
     */
    @Transactional(readOnly = true)
    public List<ExpandedDictionaryDto> searchByValueExpanded(String query) {
        List<DictionaryDto> list = searchByValue(query);
        return list.stream()
            .map(this::toExpanded)
            .toList();
    }

    /** Расширенная запись по id (для Telegram и др.): словарь + title + block. */
    @Transactional(readOnly = true)
    public Optional<ExpandedDictionaryDto> findExpandedById(Long id) {
        return dictionaryRepository.findById(id)
            .map(dictionaryConverter::toDto)
            .map(this::toExpanded);
    }

    private ExpandedDictionaryDto toExpanded(DictionaryDto dto) {
        String ck = dto.getContentKey();
        String blockId = dto.getBlockId();
        ContentType contentType = dto.getContentType();

        String title = null;
        ContentBlockDto block = null;

        if (ck != null && !ck.isBlank() && contentType != null) {
            if (contentType == ContentType.MOVIE) {
                title = movieRepository.findByContentKey(ck).map(m -> m.getName()).orElse(null);
            } else if (contentType == ContentType.EPISODE) {
                title = episodeRepository.findByContentKeyWithSeries(ck)
                    .map(e -> e.getSeries() != null ? e.getSeries().getName() : null)
                    .orElse(null);
            } else if (contentType == ContentType.COMEDY) {
                title = comedySpecialRepository.findByContentKey(ck).map(s -> s.getName()).orElse(null);
            } else if (contentType == ContentType.BOOK) {
                title = bookRepository.findByContentKey(ck).map(b -> b.getName()).orElse(null);
            }
            if (blockId != null && !blockId.isBlank()) {
                if (contentType == ContentType.MOVIE) {
                    block = resolveMovieBlock(ck, blockId);
                } else if (contentType == ContentType.EPISODE) {
                    block = resolveEpisodeBlock(ck, blockId);
                } else if (contentType == ContentType.COMEDY) {
                    block = resolveComedyBlock(ck, blockId);
                } else if (contentType == ContentType.BOOK) {
                    block = resolveBookBlock(ck, blockId);
                }
            }
        }

        return new ExpandedDictionaryDto(dto, title, block);
    }

    private ContentBlockDto resolveMovieBlock(String contentKey, String blockId) {
        return movieRepository.findByContentKey(contentKey)
            .flatMap(movie -> movieContentRepository.findByMovieIdAndBlockId(movie.getId(), blockId))
            .map(ContentBlockMapper::fromEntity)
            .orElse(null);
    }

    private ContentBlockDto resolveEpisodeBlock(String contentKey, String blockId) {
        return episodeRepository.findByContentKey(contentKey)
            .flatMap(ep -> episodeContentRepository.findByEpisodeIdAndBlockId(ep.getId(), blockId))
            .map(ContentBlockMapper::fromEntity)
            .orElse(null);
    }

    private ContentBlockDto resolveComedyBlock(String contentKey, String blockId) {
        return comedySpecialRepository.findByContentKey(contentKey)
            .flatMap(special -> comedyContentRepository.findByComedySpecialIdAndBlockId(special.getId(), blockId))
            .map(ContentBlockMapper::fromEntity)
            .orElse(null);
    }

    private ContentBlockDto resolveBookBlock(String contentKey, String blockId) {
        return bookRepository.findByContentKey(contentKey)
            .flatMap(book -> bookContentRepository.findByBookIdAndBlockId(book.getId(), blockId))
            .map(ContentBlockMapper::fromEntity)
            .orElse(null);
    }
}
