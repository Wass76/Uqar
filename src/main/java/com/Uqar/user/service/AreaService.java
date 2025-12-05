package com.Uqar.user.service;

import com.Uqar.user.dto.AreaRequestDTO;
import com.Uqar.user.dto.AreaResponseDTO;
import com.Uqar.user.entity.Area;
import com.Uqar.user.entity.AreaTranslation;
import com.Uqar.user.mapper.AreaMapper;
import com.Uqar.user.repository.AreaRepository;
import com.Uqar.user.repository.AreaTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AreaService {

    private final AreaRepository areaRepository;
    private final AreaTranslationRepository areaTranslationRepository;

//    public List<AreaResponseDTO> getAllAreas() {
//        return areaRepository.findAll().stream()
//                .map(AreaMapper::toResponseDTO)
//                .collect(Collectors.toList());
//    }

    public List<AreaResponseDTO> getAllAreas() {
        return areaRepository.findAll().stream()
                .map(area -> AreaMapper.toResponseDTO(area))
                .collect(Collectors.toList());
    }

//    public List<AreaResponseDTO> getActiveAreas() {
//        return areaRepository.findByIsActiveTrue().stream()
//                .map(AreaMapper::toResponseDTO)
//                .collect(Collectors.toList());
//    }

    public List<AreaResponseDTO> getActiveAreas() {
        return areaRepository.findByIsActiveTrue().stream()
                .map(AreaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }


    public Optional<AreaResponseDTO> getAreaById(Long id, String language) {
        return areaRepository.findById(id)
                .map(AreaMapper::toResponseDTO);
    }

//    public Optional<AreaResponseDTO> getAreaByName(String name) {
//        return areaRepository.findByName(name)
//                .map(AreaMapper::toResponseDTO);
//    }

    public Optional<AreaResponseDTO> getAreaByName(String name) {
        return areaRepository.findByName(name)
                .map(area -> AreaMapper.toResponseDTO(area));
    }

    public Optional<AreaResponseDTO> getAreaByTranslation(String language, String translatedName) {
        List<AreaTranslation> translations = areaTranslationRepository.findByLanguageAndTranslatedName(language, translatedName);
        if (translations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AreaMapper.toResponseDTO(translations.get(0).getArea()));
    }

    public AreaResponseDTO createArea(AreaRequestDTO requestDTO) {
        Area area = AreaMapper.toEntity(requestDTO);
        Area savedArea = areaRepository.save(area);

        // Save translations
        if (requestDTO.getTranslations() != null) {
            for (Map.Entry<String, String> entry : requestDTO.getTranslations().entrySet()) {
                AreaTranslation translation = AreaTranslation.builder()
                        .area(savedArea)
                        .language(entry.getKey())
                        .translatedName(entry.getValue())
                        .build();
                areaTranslationRepository.save(translation);
            }
        }

        return AreaMapper.toResponseDTO(savedArea);
    }

    public AreaResponseDTO updateArea(Long id, AreaRequestDTO requestDTO) {
        Area existingArea = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found with id: " + id));

        AreaMapper.updateEntityFromDTO(existingArea, requestDTO);
        Area updatedArea = areaRepository.save(existingArea);

        // Update translations
        if (requestDTO.getTranslations() != null) {
            // Delete existing translations
            List<AreaTranslation> existingTranslations = areaTranslationRepository.findByAreaId(id);
            areaTranslationRepository.deleteAll(existingTranslations);

            // Create new translations
            for (Map.Entry<String, String> entry : requestDTO.getTranslations().entrySet()) {
                AreaTranslation translation = AreaTranslation.builder()
                        .area(updatedArea)
                        .language(entry.getKey())
                        .translatedName(entry.getValue())
                        .build();
                areaTranslationRepository.save(translation);
            }
        }

        return AreaMapper.toResponseDTO(updatedArea);
    }

    public void deleteArea(Long id) {
        areaRepository.deleteById(id);
    }

    public boolean areaExists(String name) {
        return areaRepository.existsByName(name);
    }
}
