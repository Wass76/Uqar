# Pharmaceutical Data Import System

This system allows importing pharmaceutical data from Excel files into the Uqar database using a Python script integrated with Spring Boot.

## Overview

The import flow works as follows:
1. Admin uploads an Excel file via the `/api/pharmaceutical/import` endpoint
2. The file is processed by a Python script (`extract_for_spring_boot.py`)
3. The script extracts data and returns a list of `MProductDTORequest` objects
4. Each product is saved to the database using the `MasterProduct` entity

## Files Structure

```
scripts/
├── extract_for_spring_boot.py    # Main Python extraction script
└── test_extract.py              # Test script for validation

src/main/java/com/Uqar/product/
├── controller/
│   └── PharmaceuticalController.java    # REST endpoints
├── service/
│   └── PharmaceuticalImportService.java # Business logic
└── dto/
    ├── ImportResponse.java              # Response DTO
    ├── MProductDTORequest.java          # Product request DTO
    └── MProductTranslationDTORequest.java # Translation DTO
```

## Required Excel File Format

The Excel file must contain the following columns (in Arabic):
- `الاسم التجاري` (Trade Name)
- `التركيب ` (Scientific Name)
- `العيار` (Concentration)
- ` العبوة` (Size)
- `المعمل` (Manufacturer)
- ` الشكل الصيدلاني` (Pharmaceutical Form)
- `السعر للعموم` (Selling Price)
- `السعر للصيدلاني` (Purchase Price)

## API Endpoints

### Import Pharmaceutical Data
```
POST /api/pharmaceutical/import
Content-Type: multipart/form-data

Parameter: file (Excel file)
```

### Get Import Status
```
GET /api/pharmaceutical/import/status
```

### Validate Database
```
GET /api/pharmaceutical/import/validate
```

### Get Supported Formats
```
GET /api/pharmaceutical/import/formats
```

### Health Check
```
GET /api/pharmaceutical/health
```

## Configuration

Add the following configuration to your `application.yml`:

```yaml
# Pharmaceutical Import Configuration
pharmaceutical:
  python:
    script:
      path: scripts/
  temp:
    dir: /tmp/pharmaceutical/
  import:
    timeout:
      minutes: 5
    max-file-size:
      mb: 50
```

### Configuration Options

- `pharmaceutical.python.script.path`: Path to the Python script directory (default: `scripts/`)
- `pharmaceutical.temp.dir`: Temporary directory for file processing (default: `/tmp/pharmaceutical/`)
- `pharmaceutical.import.timeout.minutes`: Script execution timeout in minutes (default: `5`)
- `pharmaceutical.import.max-file-size.mb`: Maximum file size in MB (default: `50`)

### Environment-Specific Examples

**Development (local):**
```yaml
pharmaceutical:
  python:
    script:
      path: scripts/
  temp:
    dir: /tmp/pharmaceutical/
```

**Docker/Production:**
```yaml
pharmaceutical:
  python:
    script:
      path: /opt/scripts/
  temp:
    dir: /tmp/pharmaceutical/
  import:
    timeout:
      minutes: 10
    max-file-size:
      mb: 100
```

**Windows:**
```yaml
pharmaceutical:
  python:
    script:
      path: scripts\\
  temp:
    dir: C:\\temp\\pharmaceutical\\
```

## Database Requirements

The system requires the following tables to exist:
- `manufacturers` - Pharmaceutical manufacturers
- `forms` - Pharmaceutical forms (tablets, capsules, etc.)
- `master_product` - Main product table

## Python Dependencies

The Python script requires:
- `pandas` - Excel file processing
- `psycopg2` - PostgreSQL connection
- `mysql-connector-python` - MySQL connection (optional)
- `openpyxl` - Excel file support

Install dependencies:
```bash
pip install pandas psycopg2-binary mysql-connector-python openpyxl
```

## Testing

Run the test script to validate the extraction:
```bash
cd scripts
python3 test_extract.py
```

## Error Handling

The system includes comprehensive error handling:
- File validation (format, size, required columns)
- Database connection validation
- Individual record processing with rollback
- Detailed error reporting

## Output Format

The Python script returns JSON in the following format:
```json
[
  {
    "tradeName": "Product Name",
    "scientificName": "Scientific Name",
    "concentration": "20mg",
    "size": "14 capsules",
    "refPurchasePrice": 150.0,
    "refSellingPrice": 300.0,
    "notes": "Product description",
    "tax": 15.0,
    "barcode": "1234567890123",
    "requiresPrescription": false,
    "typeId": 1,
    "formId": 2,
    "manufacturerId": 1,
    "categoryIds": [1],
    "translations": [
      {
        "tradeName": "اسم المنتج",
        "scientificName": "الاسم العلمي",
        "lang": "ar"
      }
    ]
  }
]
```

## Troubleshooting

### Common Issues

1. **Script not found**: Ensure the Python script path is correct in configuration
2. **Database connection failed**: Check database credentials and connection string
3. **Missing columns**: Verify Excel file has all required columns
4. **Permission errors**: Ensure temp directory is writable

### Logs

Check application logs for detailed error information:
- Python script execution logs
- Database connection issues
- File processing errors
- Individual record processing failures

## Security Considerations

- File uploads are validated for type and size
- Temporary files are automatically cleaned up
- Database connections use configured credentials
- Input validation prevents SQL injection
