ifneq (,$(wildcard .env))
include .env
export
endif

STRIPE_FORWARD_URL ?= http://localhost:8080/stripe/webhook

.PHONY: up up-nc down stripe-listen stripe-trigger

up:
	docker compose down -v && docker compose up --build


up-nc:
	docker compose down -v
	docker compose build --no-cache
	docker compose up

down:
	docker compose down -v

stripe-listen:
	stripe listen --api-key "$(STRIPE_SECRET_KEY)" --forward-to "$(STRIPE_FORWARD_URL)"

stripe-trigger:
	stripe trigger checkout.session.completed --api-key "$(STRIPE_SECRET_KEY)"