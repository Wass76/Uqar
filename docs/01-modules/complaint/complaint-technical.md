# Complaint Module - Technical Documentation

## Data Flow Architecture

### Create Complaint Flow

```
Frontend (POST /api/v1/complaints)
    ↓
ComplaintController.createComplaint()
    ↓
ComplaintService.createComplaint()
    ├── BaseSecurityService.getCurrentUser() → User
    ├── BaseSecurityService.getCurrentUserPharmacyId() → Long
    ├── ComplaintMapper.toEntity() → Complaint
    ├── setAuditInfo() → Sets IP, user agent, session
    └── ComplaintRepository.save() → Complaint
    ↓
ComplaintMapper.toResponseDTO() → ComplaintResponseDTO
    ↓
Response (201 Created)
```

### Get Complaint Flow

```
Frontend (GET /api/v1/complaints/{id})
    ↓
ComplaintController.getComplaintById()
    ↓
ComplaintService.getComplaintById()
    ├── ComplaintRepository.findById() → Complaint
    └── BaseSecurityService.validatePharmacyAccess() → Validates access
    ↓
ComplaintMapper.toResponseDTO() → ComplaintResponseDTO
    ↓
Response (200 OK)
```

### Update Complaint Flow

```
Frontend (PUT /api/v1/complaints/{id})
    ↓
ComplaintController.updateComplaint()
    ↓
ComplaintService.updateComplaint()
    ├── ComplaintRepository.findById() → Complaint
    ├── BaseSecurityService.validatePharmacyAccess() → Validates access
    ├── ComplaintMapper.updateEntity() → Updates fields
    ├── Sets respondedBy, respondedAt
    └── ComplaintRepository.save() → Complaint
    ↓
ComplaintMapper.toResponseDTO() → ComplaintResponseDTO
    ↓
Response (200 OK)
```

## Key Endpoints

### Base Path: `/api/v1/complaints`

#### 1. `POST /api/v1/complaints`
- **Purpose**: Create a new complaint
- **Authorization**: `PHARMACY_MANAGER` or `PHARMACY_EMPLOYEE`
- **Request Body**: `ComplaintRequestDTO`
  ```json
  {
    "title": "string (required, max 255)",
    "description": "string (required)"
  }
  ```
- **Response**: `ComplaintResponseDTO` (201 Created)
- **Side Effects**:
  - Creates new `Complaint` entity
  - Sets `pharmacyId` from current user's pharmacy
  - Sets `createdBy` to current user ID
  - Sets status to `PENDING`
  - Records audit information (IP, user agent, session)
  - Sets `createdAt` timestamp

#### 2. `GET /api/v1/complaints/{id}`
- **Purpose**: Get complaint by ID
- **Authorization**: `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PLATFORM_ADMIN`
- **Path Parameters**: `id` (Long)
- **Response**: `ComplaintResponseDTO` (200 OK)
- **Side Effects**: None (read-only)
- **Access Control**: Validates pharmacy access (non-admin users)

#### 3. `GET /api/v1/complaints`
- **Purpose**: Get all complaints for pharmacy (paginated)
- **Authorization**: `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PLATFORM_ADMIN`
- **Query Parameters**:
  - `page` (int, default: 0) - Page number (0-based)
  - `size` (int, default: 10, max: 100) - Items per page
- **Response**: `Page<ComplaintResponseDTO>` (200 OK)
- **Side Effects**: None (read-only)
- **Access Control**: Filters by pharmacy ID (non-admin users)

#### 4. `GET /api/v1/complaints/status/{status}`
- **Purpose**: Get complaints filtered by status
- **Authorization**: `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PLATFORM_ADMIN`
- **Path Parameters**: `status` (ComplaintStatus enum)
- **Query Parameters**: `page`, `size` (via Pageable)
- **Response**: `List<ComplaintResponseDTO>` (200 OK)
- **Side Effects**: None (read-only)
- **Access Control**: Filters by pharmacy ID and status

#### 5. `PUT /api/v1/complaints/{id}`
- **Purpose**: Update complaint status and response
- **Authorization**: `PHARMACY_MANAGER` or `PLATFORM_ADMIN`
- **Path Parameters**: `id` (Long)
- **Request Body**: `ComplaintUpdateRequestDTO`
  ```json
  {
    "status": "PENDING | IN_PROGRESS | RESOLVED | CLOSED | REJECTED",
    "response": "string (optional)"
  }
  ```
- **Response**: `ComplaintResponseDTO` (200 OK)
- **Side Effects**:
  - Updates complaint status
  - Sets `respondedBy` to current user ID
  - Sets `respondedAt` to current timestamp
  - Updates `lastModifiedBy` and `lastModifiedAt`
  - Records audit information
- **Access Control**: Only managers and admins can update

#### 6. `DELETE /api/v1/complaints/{id}`
- **Purpose**: Delete a complaint
- **Authorization**: `PHARMACY_MANAGER` or `PLATFORM_ADMIN`
- **Path Parameters**: `id` (Long)
- **Response**: 200 OK (no body)
- **Side Effects**:
  - Deletes complaint from database
  - Permanent deletion (no soft delete)
- **Access Control**: 
  - Creator can delete their own complaints
  - Managers can delete any complaint in their pharmacy
  - Admins can delete any complaint

#### 7. `GET /api/v1/complaints/statistics`
- **Purpose**: Get complaint statistics by status
- **Authorization**: `PHARMACY_MANAGER` or `PLATFORM_ADMIN`
- **Response**: `Map<ComplaintStatus, Long>` (200 OK)
  ```json
  {
    "PENDING": 5,
    "IN_PROGRESS": 2,
    "RESOLVED": 10,
    "CLOSED": 3,
    "REJECTED": 1
  }
  ```
- **Side Effects**: None (read-only)
- **Access Control**: Statistics are pharmacy-scoped

#### 8. `GET /api/v1/complaints/needing-response`
- **Purpose**: Get complaints that need response (PENDING or IN_PROGRESS)
- **Authorization**: `PHARMACY_MANAGER` or `PLATFORM_ADMIN`
- **Response**: `List<ComplaintResponseDTO>` (200 OK)
- **Side Effects**: None (read-only)
- **Access Control**: Filters by pharmacy ID and status

## Service Layer Components

### ComplaintService

**Package**: `com.Uqar.complaint.service`

**Extends**: `BaseSecurityService` (provides authentication and authorization utilities)

**Key Methods**:

1. **`createComplaint(ComplaintRequestDTO, HttpServletRequest)`**
   - Creates new complaint
   - Sets pharmacy ID from current user
   - Records audit information
   - Returns `ComplaintResponseDTO`

2. **`getComplaintById(Long)`**
   - Retrieves complaint by ID
   - Validates pharmacy access
   - Returns `ComplaintResponseDTO`

3. **`getAllComplaintsForPharmacy(Pageable)`**
   - Retrieves all complaints for current user's pharmacy
   - Supports pagination
   - Returns `Page<ComplaintResponseDTO>`

4. **`getComplaintsByStatus(ComplaintStatus, Pageable)`**
   - Retrieves complaints filtered by status
   - Pharmacy-scoped
   - Returns `Page<ComplaintResponseDTO>`

5. **`updateComplaint(Long, ComplaintUpdateRequestDTO, HttpServletRequest)`**
   - Updates complaint status and response
   - Validates authorization (manager/admin only)
   - Records response information
   - Returns `ComplaintResponseDTO`

6. **`deleteComplaint(Long)`**
   - Deletes complaint
   - Validates authorization (creator/manager/admin)
   - Permanent deletion

7. **`getComplaintStatistics()`**
   - Calculates complaint counts by status
   - Pharmacy-scoped
   - Returns `Map<ComplaintStatus, Long>`

8. **`getComplaintsNeedingResponse()`**
   - Retrieves PENDING and IN_PROGRESS complaints
   - Pharmacy-scoped
   - Returns `List<ComplaintResponseDTO>`

**Dependencies**:
- `ComplaintRepository` - Data access
- `ComplaintMapper` - Entity-DTO mapping
- `BaseSecurityService` - Authentication and authorization

## Repository Layer

### ComplaintRepository

**Package**: `com.Uqar.complaint.repository`

**Interface**: `JpaRepository<Complaint, Long>`

**Key Methods**:

1. **`findByPharmacyId(Long, Pageable)`**
   - Finds complaints by pharmacy ID with pagination
   - Returns `Page<Complaint>`

2. **`findByPharmacyIdAndStatus(Long, ComplaintStatus, Pageable)`**
   - Finds complaints by pharmacy ID and status
   - Returns `Page<Complaint>`

3. **`countByPharmacyIdAndStatus(Long, ComplaintStatus)`**
   - Counts complaints by pharmacy ID and status
   - Returns `long`

4. **`findComplaintsNeedingResponseByPharmacyId(Long)`**
   - Custom query: Finds PENDING or IN_PROGRESS complaints
   - Returns `List<Complaint>`

5. **`findByCreatedAtBetween(LocalDateTime, LocalDateTime)`**
   - Finds complaints created between dates
   - Returns `List<Complaint>`

6. **`findByPharmacyIdAndCreatedAtBetween(Long, LocalDateTime, LocalDateTime)`**
   - Finds pharmacy complaints created between dates
   - Returns `List<Complaint>`

## Entity Relationships

### Complaint Entity

**Package**: `com.Uqar.complaint.entity`

**Table**: `complaints`

**Extends**: `AuditedEntity` (provides `createdBy`, `createdAt`, `lastModifiedBy`, `lastModifiedAt`)

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key (auto-generated) |
| `title` | String | Complaint title (required, max 255) |
| `description` | String | Complaint description (required, TEXT) |
| `pharmacyId` | Long | Foreign key to pharmacy (required) |
| `status` | ComplaintStatus | Status enum (default: PENDING) |
| `response` | String | Response text (optional, TEXT) |
| `respondedBy` | Long | User ID who responded (optional) |
| `respondedAt` | LocalDateTime | Response timestamp (optional) |
| `ipAddress` | String | IP address of creator (max 45) |
| `userAgent` | String | User agent string (max 500) |
| `sessionId` | String | Session ID (max 100) |
| `userType` | String | User role name (max 50) |
| `additionalData` | String | Additional JSON data (optional, TEXT) |

**Relationships**:
- **No JPA relationships** - Uses `Long` IDs for `pharmacyId`, `createdBy`, `respondedBy`
- **Logical relationships**:
  - `pharmacyId` → `Pharmacy.id`
  - `createdBy` → `User.id`
  - `respondedBy` → `User.id`

### ComplaintStatus Enum

**Package**: `com.Uqar.complaint.enums`

**Values**:
- `PENDING` - Initial status
- `IN_PROGRESS` - Being worked on
- `RESOLVED` - Successfully resolved
- `CLOSED` - Closed (may be resolved or not actionable)
- `REJECTED` - Rejected (not valid)

## Dependencies

### Internal Dependencies

1. **User Management Module** (`com.Uqar.user`)
   - `User` entity - For creator and responder tracking
   - `BaseSecurityService` - For authentication and authorization
   - `UserRepository` - For user lookups

2. **Utils Module** (`com.Uqar.utils`)
   - `AuditedEntity` - Base entity with audit fields
   - `ResourceNotFoundException` - Custom exception
   - `UnAuthorizedException` - Custom exception

### External Dependencies

- **Spring Data JPA** - Repository abstraction
- **Spring Security** - Authorization annotations (`@PreAuthorize`)
- **MapStruct** - Entity-DTO mapping (via `ComplaintMapper`)
- **Lombok** - Boilerplate code reduction
- **Jakarta Validation** - Input validation

## Database Queries

### Custom Queries

1. **Find Complaints Needing Response**
   ```sql
   SELECT c FROM Complaint c 
   WHERE c.pharmacyId = :pharmacyId 
   AND c.status IN ('PENDING', 'IN_PROGRESS')
   ```

2. **Find Complaints by Date Range**
   ```sql
   SELECT c FROM Complaint c 
   WHERE c.pharmacyId = :pharmacyId 
   AND c.createdAt BETWEEN :startDate AND :endDate
   ```

### Generated Queries (Spring Data JPA)

- `findByPharmacyId` → `SELECT * FROM complaints WHERE pharmacy_id = ?`
- `findByPharmacyIdAndStatus` → `SELECT * FROM complaints WHERE pharmacy_id = ? AND status = ?`
- `countByPharmacyIdAndStatus` → `SELECT COUNT(*) FROM complaints WHERE pharmacy_id = ? AND status = ?`

## Error Handling

### Custom Exceptions

1. **`ResourceNotFoundException`**
   - Thrown when complaint not found
   - HTTP 404 Not Found

2. **`UnAuthorizedException`**
   - Thrown when user lacks permission
   - HTTP 403 Forbidden

### Validation Errors

- **400 Bad Request**: Invalid input data (title/description missing, invalid status)
- **401 Unauthorized**: User not authenticated
- **403 Forbidden**: User not authorized for action
- **404 Not Found**: Complaint not found

## Security Considerations

1. **Multi-Tenancy Enforcement**
   - All queries filter by `pharmacyId`
   - `validatePharmacyAccess()` ensures users can only access their pharmacy's complaints
   - Platform admins bypass pharmacy filtering

2. **Role-Based Access Control**
   - Employees: Create and view only
   - Managers: Full CRUD operations within their pharmacy
   - Admins: Full access across all pharmacies

3. **Authorization Checks**
   - `@PreAuthorize` annotations on controller methods
   - Service-level validation for update/delete operations
   - Creator validation for delete operations

4. **Audit Trail**
   - IP address tracking
   - User agent tracking
   - Session ID tracking
   - User type (role) tracking

## Performance Considerations

1. **Pagination**: All list endpoints support pagination (default: 10, max: 100)
2. **Indexing**: Consider indexes on:
   - `pharmacy_id`
   - `status`
   - `pharmacy_id + status` (composite)
   - `created_at`
3. **Query Optimization**: Custom queries use JPQL for efficiency
4. **Caching**: Not currently implemented, but could cache statistics

## Testing Considerations

1. **Unit Tests**: Test service methods with mocked repositories
2. **Integration Tests**: Test controller endpoints with test database
3. **Security Tests**: Verify multi-tenancy and role-based access
4. **Edge Cases**: Test with invalid IDs, unauthorized access, missing data

