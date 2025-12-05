package com.Uqar.language;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("api/v1/languages")
@Tag(name = "Language Management", description = "APIs for managing system languages")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class LanguageController {

    private LanguageService languageService;

    @Autowired
    public void setLanguageService(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    @Operation(
        summary = "Get all languages",
        description = "Retrieves all available languages in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all languages",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Language.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<Language> getLanguages() {
        return languageService.gitAll();
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get language by ID",
        description = "Retrieves a specific language by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved language",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Language.class))),
        @ApiResponse(responseCode = "404", description = "Language not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Language getLanguage(
            @Parameter(description = "Language ID", example = "1") @PathVariable Long id) {
        return languageService.gitById(id);
    }

//    @PostMapping
//    public void addLanguage(@RequestBody Language language) {
//         languageService.createLanguage(language);
//    }
//
//    @PutMapping("{id}")
//    public Language updateLanguage(@PathVariable Long id, @RequestBody Language language) {
//        return languageService.editLanguage(id, language);
//    }

//    @DeleteMapping("{id}")
//    public void deleteLanguage(@PathVariable Long id) {
//         languageService.deleteLanguage(id);
//    }

}
