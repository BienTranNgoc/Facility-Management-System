# FacilityFlow — Facility Management System

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?logo=spring&logoColor=white)](#)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](#)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178C6?logo=typescript&logoColor=white)](#)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)](#)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)](#)
[![Vite](https://img.shields.io/badge/Vite-6-646CFF?logo=vite&logoColor=white)](#)

## Overview / Elevator Pitch

FacilityFlow is a full-stack facility management platform for managing rooms, equipment, bookings, maintenance tickets, approvals, and notifications across role-based user groups (Admin, Facility Manager, Technician, User). It centralizes operational workflows to reduce scheduling conflicts, improve resource utilization, and speed up issue resolution in organizations with shared facilities.

## Key Features

- JWT-based authentication (`/auth/token`, `/auth/refresh`, `/auth/introspect`, `/auth/logout`)
- Role-aware access flows in clients (Admin, Facility Manager, Technician, User)
- Room and room type management with filtering and pagination
- Equipment and equipment model management
- Booking lifecycle management: create, approval, reject, cancel, check-in/out, complete, revoke
- Maintenance ticket reporting and status updates
- Notification APIs including overdue reminder triggers
- Dashboard APIs for room/equipment views and user navigation counts
- Static media serving from backend (`/images/**`)
- Two clients:
  - `webclient`: React + TypeScript SPA
  - `appclient`: JavaFX desktop app

## Architecture & Tech Stack

### Architecture

This repository is a **modular monolith backend + multi-client architecture** (not microservices):

- `server`: Spring Boot REST API + business logic + persistence
- `webclient`: Browser-based frontend consuming the REST API
- `appclient`: JavaFX desktop frontend consuming the REST API

Communication is HTTP/JSON from both clients to the backend API.

> **System Diagram Placeholder**  
> Replace with your architecture diagram image link (e.g., PlantUML output):  
> `![Architecture Diagram](<YOUR_ARCHITECTURE_DIAGRAM_URL>)`

### Tech Stack

- **Backend**
  - Java 21
  - Spring Boot 3.4.3
  - Spring Web, Spring Data JPA, Spring Security OAuth2 Resource Server
  - MapStruct, Lombok
- **Frontend (Web)**
  - React 19, TypeScript, Vite
  - MUI, React Router, TanStack Query, Axios
- **Frontend (Desktop)**
  - JavaFX 21
  - OkHttp + Gson
- **Database**
  - MySQL (configured in backend `application.yml`)
- **DevOps / Tooling**
  - Maven Wrapper (`mvnw`) for Java modules
  - npm + Vite + ESLint for web module
- **Cloud**
  - No cloud deployment/IaC configuration is currently defined in this repository

## Prerequisites

- JDK **21**
- Node.js + npm (recommended: Node 18+)
- MySQL (backend config targets local MySQL)
- Git

Optional (desktop app):
- JavaFX-compatible environment for running `javafx:run`

## Getting Started / Local Setup

### 1) Clone repository

```bash
git clone https://github.com/BienTranNgoc/Facility-Management-System.git
cd Facility-Management-System
```

### 2) Configure database (MySQL)

Create database:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS facility;"
```

Update backend DB/JWT settings in:

- `server/src/main/resources/application.yml`

Current backend expects:
- host: `localhost`
- port: `3307`
- database: `facility`
- context path: `/facility`

### 3) Run backend API (`server`)

```bash
cd server
chmod +x mvnw
./mvnw spring-boot:run
```

Backend base URL:

`http://localhost:8080/facility`

### 4) Run web frontend (`webclient`)

```bash
cd ../webclient
npm ci
echo 'VITE_APP_SERVER_URL="http://localhost:8080/facility"' > .env
npm run dev
```

Web app default URL:

`http://localhost:5173`

### 5) Run JavaFX desktop client (`appclient`) (optional)

Set API base URL in:

- `appclient/src/main/resources/config.properties`

Then run:

```bash
cd ../appclient
chmod +x mvnw
./mvnw javafx:run
```

## API Endpoints / Usage

> Base URL: `http://localhost:8080/facility`

### 1) Authenticate

```bash
curl -X POST "http://localhost:8080/facility/auth/token" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Admin",
    "password": "admin"
  }'
```

Example response shape:

```json
{
  "code": 1000,
  "result": {
    "token": "<JWT_TOKEN>",
    "authenticated": true
  }
}
```

### 2) Get rooms (paged)

```bash
curl "http://localhost:8080/facility/rooms?page=0&size=10"
```

### 3) Create maintenance ticket

```bash
curl -X POST "http://localhost:8080/facility/maintenance" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "R001",
    "description": "Projector is not powering on"
  }'
```

### 4) Booking approval queue

```bash
curl "http://localhost:8080/facility/booking/approval-request?page=0&size=10&status=PENDING_APPROVAL"
```

### API Documentation Note

No Swagger/OpenAPI configuration was found in this repository, so endpoint discovery is currently code-driven (controllers under `server/src/main/java/com/utc2/facility/controller`).

## Project Structure

```text
Facility-Management-System/
├── server/                         # Spring Boot backend
│   ├── src/main/java/com/utc2/facility/
│   │   ├── controller/             # REST controllers
│   │   ├── service/                # Business services
│   │   ├── repository/             # JPA repositories
│   │   ├── entity/                 # Domain entities
│   │   ├── dto/                    # Request/response DTOs
│   │   └── configuration/          # Security, JWT, app init
│   └── src/main/resources/
│       ├── application.yml
│       └── static/images/
├── webclient/                      # React + TypeScript + Vite client
│   ├── src/components/
│   ├── src/pages/
│   ├── src/reports/
│   └── src/utils/
└── appclient/                      # JavaFX desktop client
    ├── src/main/java/com/utc2/facilityui/
    │   ├── controller/
    │   ├── service/
    │   ├── model/
    │   └── app/
    └── src/main/resources/
```

## Author / Contact

- **Name:** `<YOUR_NAME>`
- **LinkedIn:** `<YOUR_LINKEDIN_URL>`
- **Email:** `<YOUR_EMAIL>`

