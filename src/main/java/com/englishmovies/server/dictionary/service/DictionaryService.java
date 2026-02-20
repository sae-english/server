package com.englishmovies.server.dictionary.service;

import com.englishmovies.server.dictionary.converter.DictionaryConverter;
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
    private final DictionaryConverter dictionaryConverter;

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
}
