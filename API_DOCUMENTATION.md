# Qanun Qapısı API Documentation

## Overview
Complete REST API documentation for the Qanun Qapısı (Law Gateway) application - an Azerbaijani Law Exam Prep platform.

## API Documentation Access

### Swagger UI
Once the application is running, access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification
The OpenAPI 3.0 specification is available at:
```
http://localhost:8080/v3/api-docs
```

## Authentication

Most endpoints require JWT authentication. Include the Bearer token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Security Scheme
- **Type**: HTTP Bearer Authentication
- **Scheme**: bearer
- **Bearer Format**: JWT

## API Endpoints Overview

### 1. Authentication (`/api/v1/auth`)
Public endpoints for user authentication and account management.

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/signup` | Register a new user | No |
| POST | `/verify` | Verify email with code | No |
| POST | `/resend` | Resend verification code | No |
| POST | `/login` | User login | No |
| POST | `/refresh` | Refresh access token | No |
| POST | `/logout` | Logout user | Yes |
| GET | `/me` | Get current user info | Yes |
| POST | `/reset-password` | Request password reset | No |
| POST | `/confirm-reset-password` | Confirm password reset | No |

### 2. Profile (`/api/v1/profile`)
User profile management endpoints.

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get user profile | Yes |
| PUT | `/` | Update user profile | Yes |
| POST | `/change-password` | Change password | Yes |
| POST | `/change-email` | Request email change | Yes |
| POST | `/verify-email-change` | Verify email change | Yes |
| POST | `/picture` | Upload profile picture | Yes |
| DELETE | `/picture` | Delete profile picture | Yes |

### 3. Tests - Customer (`/api/v1/tests`)
Customer-facing test and attempt endpoints.

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List published tests | Yes |
| GET | `/{id}` | Get test details | Yes |
| POST | `/{id}/start` | Start test attempt | Yes |
| POST | `/{id}/submit` | Submit test answers | Yes |
| GET | `/{id}/attempts` | List user's attempts | Yes |
| GET | `/attempts/{attemptId}` | Get attempt results | Yes |

### 4. Admin: Tests (`/api/v1/admin/tests`)
Admin endpoints for test management (requires ADMIN role).

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Create test | Yes (Admin) |
| PUT | `/{id}` | Update test | Yes (Admin) |
| DELETE | `/{id}` | Delete test | Yes (Admin) |
| POST | `/{id}/publish` | Publish test | Yes (Admin) |
| GET | `/` | List all tests | Yes (Admin) |
| GET | `/{id}` | Get test details | Yes (Admin) |
| POST | `/questions/{questionId}/image` | Upload question image | Yes (Admin) |
| DELETE | `/questions/{questionId}/image` | Delete question image | Yes (Admin) |

### 5. Admin: Users (`/api/v1/admin/users`)
Admin endpoints for user management (requires ADMIN role).

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | List all users | Yes (Admin) |
| GET | `/{id}` | Get user details | Yes (Admin) |
| POST | `/` | Create user | Yes (Admin) |
| PUT | `/{id}` | Update user | Yes (Admin) |
| DELETE | `/{id}` | Delete user | Yes (Admin) |

### 6. Admin: Dashboard (`/api/v1/admin/dashboard`)
Admin dashboard statistics (requires ADMIN role).

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get dashboard stats | Yes (Admin) |

## Common Response Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Request successful, no content to return |
| 400 | Bad Request - Invalid input or validation error |
| 401 | Unauthorized - Missing or invalid authentication |
| 403 | Forbidden - User lacks required permissions |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Server error |

## Request/Response Examples

### Authentication Flow

#### 1. Signup
```http
POST /api/v1/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### 2. Verify Email
```http
POST /api/v1/auth/verify
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

#### 3. Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

### Test Flow

#### 1. List Available Tests
```http
GET /api/v1/tests?page=0&size=20&sortBy=publishedAt&sortDir=DESC
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

#### 2. Start Test Attempt
```http
POST /api/v1/tests/{testId}/start
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

#### 3. Submit Test Answers
```http
POST /api/v1/tests/{testId}/submit
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
  "answers": [
    {
      "questionId": "uuid-here",
      "selectedAnswerIds": ["answer-uuid-1", "answer-uuid-2"],
      "openTextAnswer": null
    }
  ]
}
```

## Pagination

List endpoints support pagination with the following query parameters:
- `page`: Page number (0-indexed, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sortBy`: Field to sort by (default varies by endpoint)
- `sortDir`: Sort direction (`ASC` or `DESC`, default: `DESC`)

Response includes:
```json
{
  "content": [...],
  "pageable": {...},
  "totalPages": 10,
  "totalElements": 200,
  "size": 20,
  "number": 0
}
```

## File Uploads

### Profile Picture
- **Max Size**: 5MB
- **Accepted Formats**: JPG, PNG
- **Endpoint**: POST `/api/v1/profile/picture`

### Question Images
- **Max Size**: 5MB
- **Accepted Formats**: JPG, PNG
- **Endpoint**: POST `/api/v1/admin/tests/questions/{questionId}/image`

## Error Response Format

All error responses follow this structure:
```json
{
  "message": "Error description",
  "status": 400,
  "timestamp": "2025-10-17T10:00:00Z",
  "errors": {
    "fieldName": "Error message"
  }
}
```

## Data Models

### User Roles
- `CUSTOMER`: Regular user (default)
- `ADMIN`: Administrator with full access

### Test Status
- `DRAFT`: Test is being created/edited
- `PUBLISHED`: Test is available to users

### Question Types
- `CLOSED_SINGLE`: Single choice question
- `CLOSED_MULTIPLE`: Multiple choice question
- `OPEN_TEXT`: Open-ended text question

### Attempt Status
- `IN_PROGRESS`: Test attempt in progress
- `COMPLETED`: Test attempt submitted

## Rate Limiting

The API implements rate limiting to prevent abuse:
- **Window**: 1 minute
- **Max Requests**: 100 per IP address
- **Response**: 429 Too Many Requests when limit exceeded

## Version

Current API Version: **1.0.0**

Last Updated: October 17, 2025

