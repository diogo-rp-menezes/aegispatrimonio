<instruction>You are an expert software engineer. You are working on a WIP branch. Please run `git status` and `git diff` to understand the changes and the current state of the code. Analyze the workspace context and complete the mission brief.</instruction>
<workspace_context>
<artifacts>
--- CURRENT TASK CHECKLIST ---
# Aegis Patrimônio: Dockerize & Debug Task

- [x] Dockerize Frontend (Dockerfile + Nginx)
- [x] Standardize Ports (8080)
- [x] Fix 401 Unauthorized on Login
  - [x] Update SecurityConfig to permit /api/auth/**
  - [x] Align VITE_API_BASE_URL in docker-compose.yml
- [x] Rebuild and Verify Docker Stack
- [X] Final User Verification

--- IMPLEMENTATION PLAN ---
# Dockerize Frontend Implementation Plan

The goal is to include the Vue.js frontend in the existing Docker Compose setup, allowing the entire application (DB, Backend, and Frontend) to run in containers.

## Proposed Changes

### [Component: Frontend]

#### [NEW] [Dockerfile](file:///c:/Users/diogo/IdeaProjects/aegispatrimonio/frontend/Dockerfile)

Create a multi-stage Dockerfile:

- Stage 1: Build the Vue application using Node.js.
- Stage 2: Serve the static files using Nginx.

#### [MODIFY] [docker-compose.yml](file:///c:/Users/diogo/IdeaProjects/aegispatrimonio/docker-compose.yml)

- Add a `frontend` service.
- Configure dependencies (`aegis-app`).
- Map ports (e.g., 80 to 80 or 5173 to 80).

## Verification Plan

### Automated Tests

- Run `docker compose up -d`.
- Verify containers are healthy using `docker ps`.

### Manual Verification

- Access the frontend in the browser (<http://localhost>).
- Verify login and data fetching from the backend.
</artifacts>
</workspace_context>
<mission_brief>[Describe your task here...]</mission_brief>