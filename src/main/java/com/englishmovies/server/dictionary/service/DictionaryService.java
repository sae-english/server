package com.englishmovies.server.dictionary.service;

import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.domain.dto.DictionaryRequestDto;
import com.englishmovies.server.dictionary.domain.entity.Dictionary;
import com.englishmovies.server.dictionary.repository.DictionaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    public DictionaryService(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    @Transactional
    public DictionaryDto save(DictionaryRequestDto request) {
        Dictionary entity = new Dictionary();
        entity.setValue(request.getValue());
        entity.setTranslation(request.getTranslation());
        entity.setComment(request.getComment());
        Dictionary saved = dictionaryRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public DictionaryDto update(Long id, DictionaryRequestDto request) {
        Dictionary entity = dictionaryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Dictionary entry not found: " + id));
        entity.setValue(request.getValue());
        entity.setTranslation(request.getTranslation());
        entity.setComment(request.getComment());
        Dictionary updated = dictionaryRepository.save(entity);
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

    private DictionaryDto toDto(Dictionary entity) {
        return new DictionaryDto(
            entity.getId(),
            entity.getValue(),
            entity.getTranslation(),
            entity.getComment(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
