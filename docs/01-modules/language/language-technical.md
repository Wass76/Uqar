# Language Module - Technical Documentation

## Data Flow Architecture

### Language Retrieval Flow

```
LanguageController.getLanguages()
    ↓
LanguageService.gitAll()
    ↓
LanguageRepository.findAll()
    ↓
Return List<Language>
```

### Product Translation Flow

```
ProductController.getProduct(id, lang="ar")
    ↓
ProductService.getProduct(id, lang)
    ↓
1. Get product base information
2. Get translation for language "ar"
3. Map product with translation
    ↓
Return ProductDTO with translated name/description
```

## Key Endpoints

### Language Endpoints

#### `GET /api/v1/languages`
**Purpose**: Get all available languages

**Response**: `List<Language>`
```json
[
  {
    "id": 1,
    "code": "ar",
    "name": "Arabic"
  },
  {
    "id": 2,
    "code": "en",
    "name": "English"
  }
]
```

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `GET /api/v1/languages/{id}`
**Purpose**: Get language by ID

**Path Parameters**:
- `id`: Language ID

**Response**: `Language`

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

## Service Layer Components

### LanguageService

**Location**: `com.Uqar.language.LanguageService`

**Key Methods**:

1. **`gitAll()`**
   - Gets all languages
   - Returns list

2. **`gitById(Long id)`**
   - Gets language by ID
   - Returns language

**Note**: Simple service, mainly read-only operations

---

## Repository Layer

### LanguageRepository

**Location**: `com.Uqar.language.LanguageRepo`

**Key Methods**:
```java
List<Language> findAll();
Optional<Language> findById(Long id);
Optional<Language> findByCode(String code);
```

---

## Entity Relationships

### Language Entity

```java
@Entity
public class Language {
    private Long id;
    private String code;  // "ar", "en"
    private String name;  // "Arabic", "English"
}
```

### Product Translation (in Product Module)

```java
@Entity
@Table(name = "master_product_translation")
public class MasterProductTranslation {
    @ManyToOne
    private MasterProduct product;
    
    @ManyToOne
    private Language language;
    
    private String tradeName;
    private String scientificName;
    private String description;
}
```

---

## Dependencies

### Internal Dependencies

1. **Product Module**
   - Uses Language for translations
   - Translation entities in product module

### External Dependencies

- **Spring Data JPA**: Database access

---

## Database Queries

### Get Languages

```sql
SELECT * FROM language;
```

### Get Product Translation

```sql
SELECT mpt.trade_name, mpt.scientific_name, mpt.description
FROM master_product_translation mpt
WHERE mpt.product_id = ? AND mpt.language_id = ?;
```

---

## Error Handling

### Common Exceptions

1. **`ResourceNotFoundException`**: 
   - Language not found

---

## Performance Considerations

1. **Caching**: 
   - Languages can be cached (rarely change)
   - Product translations cached per language

2. **Indexing**: 
   - `language.code` (for lookup)
   - `product_translation.product_id` + `language_id` (for translation lookup)

---

## Security Considerations

1. **Read-Only**: Languages are system-defined
2. **No User Input**: Language selection is from predefined list

---

## Usage in Other Modules

### Product Endpoints

Most product endpoints accept `lang` parameter:

```java
@GetMapping("/products/{id}")
public ProductResponseDTO getProduct(
    @PathVariable Long id,
    @RequestParam(defaultValue = "ar") String lang) {
    return productService.getProduct(id, lang);
}
```

### Translation Lookup

```java
// In ProductService
public ProductResponseDTO getProduct(Long id, String lang) {
    Product product = productRepository.findById(id);
    Language language = languageRepository.findByCode(lang)
        .orElseGet(() -> getDefaultLanguage());
    
    ProductTranslation translation = 
        translationRepository.findByProductAndLanguage(product, language)
            .orElse(null);
    
    // Map product with translation
    return productMapper.toResponse(product, translation);
}
```

