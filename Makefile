.PHONY: help up up-nc down stop restart ps logs logs-live logs-backend logs-frontend logs-db logs-valkey \
        backend-run backend-test backend-build frontend-install frontend-dev frontend-build \
        act-frontend act-backend act-db act-security db-shell valkey-shell clean clean-all

help:
	@echo "Available commands:"
	@echo ""
	@echo "Docker:"
	@echo "  make up                - Build and start all containers"
	@echo "  make up-nc             - Build without Docker cache and start containers"
	@echo "  make down              - Stop containers and remove volumes"
	@echo "  make stop              - Stop containers without removing volumes"
	@echo "  make restart           - Restart Docker stack"
	@echo "  make ps                - Show running containers"
	@echo "  make clean             - Stop containers, remove volumes, orphans and unused Docker resources"
	@echo "  make clean-all         - Stop containers and remove project images plus all unused Docker images"
	@echo ""
	@echo "Logs:"
	@echo "  make logs              - Show logs from all services"
	@echo "  make logs-live         - Show only new logs from all services"
	@echo "  make logs-backend      - Show backend logs"
	@echo "  make logs-frontend     - Show frontend logs"
	@echo "  make logs-db           - Show database logs"
	@echo "  make logs-valkey       - Show Valkey logs"
	@echo ""
	@echo "Backend:"
	@echo "  make backend-run       - Run backend locally with the dev profile"
	@echo "  make backend-test      - Run backend tests"
	@echo "  make backend-build     - Clean and build backend"
	@echo ""
	@echo "Frontend:"
	@echo "  make frontend-install  - Install frontend dependencies"
	@echo "  make frontend-dev      - Run frontend development server"
	@echo "  make frontend-build    - Build frontend"
	@echo ""
	@echo "GitHub Actions with act:"
	@echo "  make act-frontend      - Run frontend GitHub Actions workflow locally"
	@echo "  make act-backend       - Run backend GitHub Actions workflow locally"
	@echo "  make act-db            - Run database migration check workflow locally"
	@echo "  make act-security      - Run security audit workflow locally"
	@echo ""
	@echo "Shells:"
	@echo "  make db-shell          - Open MySQL shell inside database container"
	@echo "  make valkey-shell      - Open Valkey CLI inside Valkey container"

up:
	docker compose down -v && docker compose up --build

up-nc:
	docker compose down -v
	docker compose build --no-cache
	docker compose up

down:
	docker compose down -v

stop:
	docker compose down

restart:
	docker compose down
	docker compose up --build

ps:
	docker compose ps

logs:
	docker compose logs -f

logs-live:
	docker compose logs -f --tail=0

logs-backend:
	docker compose logs -f backend

logs-frontend:
	docker compose logs -f frontend

logs-db:
	docker compose logs -f database

logs-valkey:
	docker compose logs -f valkey

backend-run:
	cd backend && ./gradlew bootRun --args='--spring.profiles.active=dev'

backend-test:
	cd backend && ./gradlew test

backend-build:
	cd backend && ./gradlew clean build

frontend-install:
	cd frontend && pnpm install

frontend-dev:
	cd frontend && pnpm run dev

frontend-build:
	cd frontend && pnpm run build

act-frontend:
	act push -W .github/workflows/frontend-build.yml -j build-frontend

act-backend:
	act push -W .github/workflows/backend-build.yaml -j build --env-file .env.act

act-db:
	act push -W .github/workflows/db-migration-check.yaml -j validate-current-migrations

act-security:
	act push -W .github/workflows/security-audit.yaml -j audit-frontend

db-shell:
	docker exec -it barbershop-db mysql -u root -p

valkey-shell:
	docker exec -it barbershop-valkey valkey-cli

clean:
	docker compose down -v --remove-orphans
	docker system prune -f

clean-all:
	docker compose down -v --remove-orphans --rmi all
	docker system prune -af

