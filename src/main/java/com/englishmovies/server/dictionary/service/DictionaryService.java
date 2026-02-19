package com.englishmovies.server.dictionary.service;

import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.domain.dto.DictionaryRequestDto;
import com.englishmovies.server.dictionary.domain.entity.DictionaryEntity;
import com.englishmovies.server.dictionary.repository.DictionaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    @Transactional
    public DictionaryDto save(DictionaryRequestDto request) {
        DictionaryEntity entity = new DictionaryEntity();
        entity.setValue(request.getValue());
        entity.setTranslation(request.getTranslation());
        entity.setLanguage(request.getLanguage());
        entity.setComment(request.getComment());
        entity.setContentKey(request.getContentKey());
        entity.setContentType(request.getContentType());
        entity.setBlockId(request.getBlockId());
        DictionaryEntity saved = dictionaryRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public DictionaryDto update(Long id, DictionaryRequestDto request) {
        DictionaryEntity entity = dictionaryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Dictionary entry not found: " + id));
        entity.setValue(request.getValue());
        entity.setTranslation(request.getTranslation());
        entity.setLanguage(request.getLanguage());
        entity.setComment(request.getComment());
        entity.setContentKey(request.getContentKey());
        entity.setContentType(request.getContentType());
        entity.setBlockId(request.getBlockId());
        DictionaryEntity updated = dictionaryRepository.save(entity);
        return toDto(updated);
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
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DictionaryDto> findFirst() {
        return dictionaryRepository.findFirstByOrderByIdAsc()
            .map(this::toDto);
    }

    private DictionaryDto toDto(DictionaryEntity entity) {
        return new DictionaryDto(
            entity.getId(),
            entity.getValue(),
            entity.getTranslation(),
            entity.getLanguage(),
            entity.getComment(),
            entity.getContentKey(),
            entity.getContentType(),
            entity.getBlockId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
