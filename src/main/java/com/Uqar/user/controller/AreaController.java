package com.Uqar.user.controller;

import com.Uqar.user.dto.AreaRequestDTO;
import com.Uqar.user.dto.AreaResponseDTO;
import com.Uqar.user.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    public ResponseEntity<List<AreaResponseDTO>> getAllAreas() {
        List<AreaResponseDTO> areas = areaService.getAllAreas();
        return ResponseEntity.ok(areas);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AreaResponseDTO>> getActiveAreas() {
        List<AreaResponseDTO> areas = areaService.getActiveAreas();
        return ResponseEntity.ok(areas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaResponseDTO> getAreaById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ar") String lang) {
        Optional<AreaResponseDTO> area = areaService.getAreaById(id, lang);
        return area.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<AreaResponseDTO> getAreaByName(
            @PathVariable String name) {
        Optional<AreaResponseDTO> area = areaService.getAreaByName(name);
        return area.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @GetMapping("/translation/{language}/{translatedName}")
//    public ResponseEntity<AreaResponseDTO> getAreaByTranslation(
//            @PathVariable String language,
//            @PathVariable String translatedName) {
//        Optional<AreaResponseDTO> area = areaService.getAreaByTranslation(language, translatedName);
//        return area.map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }

    @PostMapping
    public ResponseEntity<AreaResponseDTO> createArea(@RequestBody AreaRequestDTO requestDTO) {
        AreaResponseDTO createdArea = areaService.createArea(requestDTO);
        return ResponseEntity.ok(createdArea);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaResponseDTO> updateArea(@PathVariable Long id, @RequestBody AreaRequestDTO requestDTO) {
        AreaResponseDTO updatedArea = areaService.updateArea(id, requestDTO);
        return ResponseEntity.ok(updatedArea);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        areaService.deleteArea(id);
        return ResponseEntity.noContent().build();
    }
}