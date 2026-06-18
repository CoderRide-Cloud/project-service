# 📂 Project Service

The Project Service allows developers to showcase their portfolios, open-source work, and collaborative projects.

## 🏗️ Architecture Flow

```mermaid
sequenceDiagram
    participant User
    participant Gateway
    participant ProjectService
    participant ProjectDB

    User->>Gateway: POST /api/v1/projects
    Gateway->>ProjectService: Create Project
    ProjectService->>ProjectDB: Save Project Details
    ProjectDB-->>ProjectService: Return ID
    ProjectService-->>User: 201 Created
```

## 🔑 Key Responsibilities
- **Project Portfolios**: Managing the CRUD operations for developer projects.
- **Collaboration**: Linking multiple members to a single project.
- **Showcase**: Fetching lists of projects by tech stack, popularity, or owner.

## ⚙️ Environment Variables
Required variables in `.env`:
- `PROJECT_DB_URL`
- `PROJECT_DB_USERNAME`
- `PROJECT_DB_PASSWORD`

## 🛠️ Tech Stack
- **Database**: PostgreSQL (`project_db`)
- **Port**: `8084`
