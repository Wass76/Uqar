# Language Module - Summary

## Objective

The **Language Module** provides multi-language support for the Uqar system, allowing product names, descriptions, and other content to be displayed in different languages (primarily Arabic and English). It solves the critical problem of supporting a bilingual pharmacy system for the Syrian market.

## Problem Statement

Pharmacies in Syria need to:
- Display product information in Arabic and English
- Support multiple languages for product names
- Allow users to switch language preferences
- Maintain translations for all products

## User Roles

### Primary Users

1. **All Users**
   - Can view content in their preferred language
   - Language selection affects product names and descriptions

## Core Concepts

### Language

A **Language** represents a supported language in the system:
- Has language code (e.g., "ar", "en")
- Has language name (e.g., "Arabic", "English")
- Used for product translations

### Product Translations

Products (MasterProduct and PharmacyProduct) have translations:
- Each product can have multiple translations
- Translations stored in separate tables:
  - `master_product_translation`
  - `pharmacy_product_translation`
- Links product to language and translated text

## Main User Workflows

### 1. Language Selection Workflow

**Scenario**: User wants to view content in Arabic.

1. **Select Language**: User selects language (e.g., "ar")
2. **API Request**: All API requests include `lang` parameter
3. **System Response**: 
   - Product names in selected language
   - Descriptions in selected language
   - All translatable content in selected language
4. **Display**: Frontend displays content in selected language

**Outcome**: User sees content in their preferred language.

### 2. Product Translation Workflow

**Scenario**: System displays product with translation.

1. **Product Request**: User requests product with `lang=ar`
2. **System Queries**: 
   - Gets product base information
   - Gets translation for language "ar"
3. **Response**: Returns product with Arabic name and description
4. **Display**: Frontend shows Arabic text

**Outcome**: Product displayed in requested language.

## Key Business Rules

1. **Default Language**: Arabic ("ar") is default
2. **Fallback**: If translation not found, shows base language
3. **Language Codes**: Standard ISO codes (ar, en)
4. **Read-Only**: Languages are system-defined (no user creation)

## Integration Points

### With Product Module
- **Product Names**: Product names translated
- **Product Descriptions**: Descriptions translated
- **Category Names**: Category names translated

### With All Modules
- **API Parameters**: All product-related endpoints accept `lang` parameter
- **Response Format**: Responses include translated content

## Success Metrics

- **Language Support**: All content available in supported languages
- **Translation Coverage**: High percentage of products translated
- **User Experience**: Seamless language switching

