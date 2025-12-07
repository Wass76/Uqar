# Complaint Module - Business Summary

## Objective

The Complaint Module enables pharmacy staff to create, track, and manage complaints within the pharmacy system. It provides a structured workflow for handling issues, tracking their resolution status, and maintaining a complete audit trail of all complaint-related activities.

**Problem Solved**: Pharmacies need a systematic way to log, track, and resolve internal complaints or issues. This module provides a centralized complaint management system with role-based access control, status tracking, and comprehensive audit information.

## User Roles

### Primary Users

1. **Pharmacy Manager** (`PHARMACY_MANAGER`)
   - Can create complaints
   - Can view all complaints for their pharmacy
   - Can update complaint status and add responses
   - Can delete complaints
   - Can view statistics and complaints needing response

2. **Pharmacy Employee** (`PHARMACY_EMPLOYEE`)
   - Can create complaints
   - Can view all complaints for their pharmacy
   - Cannot update or delete complaints (read-only access)

3. **Platform Admin** (`PLATFORM_ADMIN`)
   - Full access to all complaints across all pharmacies
   - Can update and delete any complaint
   - Can view statistics for any pharmacy

## Core Concepts

### Complaint Status Workflow

The complaint lifecycle follows a structured status workflow:

```
PENDING → IN_PROGRESS → RESOLVED/CLOSED
                ↓
            REJECTED
```

**Status Definitions**:
- **PENDING**: Complaint has been created but not yet reviewed
- **IN_PROGRESS**: Complaint is being actively worked on
- **RESOLVED**: Complaint has been successfully resolved
- **CLOSED**: Complaint has been closed (may be resolved or not actionable)
- **REJECTED**: Complaint has been rejected (not valid or not actionable)

### Multi-Tenancy

- Each complaint is associated with a specific pharmacy (`pharmacyId`)
- Users can only access complaints from their own pharmacy
- Platform admins can access complaints from all pharmacies
- All queries are automatically filtered by pharmacy ID

### Audit Trail

Every complaint maintains comprehensive audit information:
- **Creation**: `createdBy`, `createdAt`, `ipAddress`, `userAgent`, `sessionId`, `userType`
- **Modification**: `lastModifiedBy`, `lastModifiedAt`
- **Response**: `respondedBy`, `respondedAt`, `response` (text)

## Main User Workflows

### 1. Creating a Complaint

**Actor**: Pharmacy Manager or Employee

**Steps**:
1. User navigates to complaint creation form
2. User enters:
   - Title (required, max 255 characters)
   - Description (required, text)
3. System automatically:
   - Sets `pharmacyId` from current user's pharmacy
   - Sets `createdBy` to current user ID
   - Sets initial status to `PENDING`
   - Records audit information (IP, user agent, session, user type)
   - Sets `createdAt` timestamp

**Business Rules**:
- Title and description are required
- Pharmacy ID is automatically set (cannot be manually specified)
- Status always starts as `PENDING`
- All audit fields are automatically populated

### 2. Viewing Complaints

**Actor**: Pharmacy Manager, Employee, or Platform Admin

**Steps**:
1. User requests list of complaints
2. System filters complaints by:
   - Pharmacy ID (for non-admin users)
   - Optional status filter
   - Pagination support
3. User can:
   - View all complaints for their pharmacy
   - Filter by status
   - View individual complaint details
   - View complaints needing response

**Business Rules**:
- Non-admin users can only see complaints from their pharmacy
- Platform admins can see all complaints
- Pagination is supported (default: 10 items per page, max 100)

### 3. Updating Complaint Status

**Actor**: Pharmacy Manager or Platform Admin

**Steps**:
1. User selects a complaint to update
2. User can update:
   - Status (PENDING → IN_PROGRESS → RESOLVED/CLOSED/REJECTED)
   - Response text (optional)
3. System automatically:
   - Sets `respondedBy` to current user ID
   - Sets `respondedAt` to current timestamp
   - Updates `lastModifiedBy` and `lastModifiedAt`
   - Records audit information

**Business Rules**:
- Only managers and admins can update complaints
- Employees cannot update complaints
- Response is optional but recommended when resolving
- Status transitions should follow logical workflow

### 4. Viewing Statistics

**Actor**: Pharmacy Manager or Platform Admin

**Steps**:
1. User requests complaint statistics
2. System calculates counts by status for the pharmacy
3. Returns map of status → count

**Business Rules**:
- Statistics are pharmacy-scoped (non-admin users)
- Platform admins can view statistics for any pharmacy
- Includes all status types (PENDING, IN_PROGRESS, RESOLVED, CLOSED, REJECTED)

### 5. Finding Complaints Needing Response

**Actor**: Pharmacy Manager or Platform Admin

**Steps**:
1. User requests complaints needing response
2. System filters complaints with status:
   - `PENDING` (not yet reviewed)
   - `IN_PROGRESS` (actively being worked on)
3. Returns list of complaints requiring attention

**Business Rules**:
- Only returns PENDING and IN_PROGRESS complaints
- Filtered by pharmacy ID (non-admin users)
- Helps managers prioritize work

### 6. Deleting Complaints

**Actor**: Complaint Creator, Pharmacy Manager, or Platform Admin

**Steps**:
1. User requests to delete a complaint
2. System validates:
   - User is the creator, OR
   - User is a manager/admin
   - Complaint belongs to user's pharmacy (if not admin)
3. System deletes the complaint

**Business Rules**:
- Only creator, manager, or admin can delete
- Employees cannot delete complaints they didn't create
- Platform admins can delete any complaint
- Deletion is permanent (soft delete not implemented)

## Key Business Rules

1. **Pharmacy Isolation**: Users can only access complaints from their pharmacy (except admins)
2. **Status Workflow**: Status should follow logical progression (PENDING → IN_PROGRESS → RESOLVED/CLOSED)
3. **Role-Based Actions**:
   - Employees: Create and view only
   - Managers: Full CRUD operations
   - Admins: Full access across all pharmacies
4. **Audit Trail**: All actions are logged with user, timestamp, IP, and session information
5. **Automatic Pharmacy Assignment**: Complaint pharmacy ID is always set from current user's pharmacy
6. **Response Tracking**: When status is updated, response information is recorded

## Integration Points

### User Management Module
- **Dependency**: Uses `User` entity for creator and responder tracking
- **Dependency**: Uses `BaseSecurityService` for authentication and authorization
- **Dependency**: Uses pharmacy ID from current user context

### Audit System
- **Integration**: Extends `AuditedEntity` for automatic audit fields
- **Integration**: Custom audit fields (IP, user agent, session) for enhanced tracking

## Success Metrics

- **Complaint Resolution Time**: Average time from PENDING to RESOLVED/CLOSED
- **Response Rate**: Percentage of complaints with responses
- **Status Distribution**: Distribution of complaints across statuses
- **User Engagement**: Number of complaints created per pharmacy/user

## Use Cases

1. **Internal Issue Tracking**: Pharmacy staff can log internal issues or concerns
2. **Problem Resolution**: Managers can track and resolve issues systematically
3. **Audit Compliance**: Complete audit trail for compliance and accountability
4. **Performance Monitoring**: Statistics help identify recurring issues
5. **Workload Management**: "Needing Response" feature helps prioritize work

