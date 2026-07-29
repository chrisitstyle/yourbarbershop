# YourBarbershop 💈

A comprehensive barbershop management application built as a Bachelor's Degree Thesis project. YourBarbershop streamlines daily operations for barbershop owners, from appointment scheduling to service management.

## ✨ Features

The application provides comprehensive management tools:

- **User Management** - Handle customer profiles and administrator accounts
- **Appointment System** - Schedule and manage customer visits efficiently
- **Service Catalog** - Create and manage service offerings with pricing
- **Order Processing** - Track service orders and customer history
- **Secure Authentication** - Short-lived JWT access tokens with refresh tokens stored in secure HttpOnly cookies and password hashing
- **Passwordless Email OTP Login** - Users can sign in with a short-lived one-time code delivered by email
- **Bot Protection** - Integrated Google reCAPTCHA v2 to secure registration and password recovery flows.
- **Role-Based Access Control** - Two roles: administrators and users (customers)
- **Photo Gallery** - Showcase barber portfolio and work examples stored in Supabase
- **Password Recovery** - Secure password reset functionality
- **Guest Orders** - Allow non-registered customers to book appointments
- **Stripe Online Payments** - Support online card payments through Stripe Checkout with webhook-based payment confirmation
- **Payment Management** - Store payment method, payment status, Stripe Checkout Session ID, Payment Intent ID, amount, currency, and paid timestamp in a dedicated `payment` table
- **Email Notifications System** - Automated appointment confirmations sent directly to customers' email addresses
- **Automated Database Migrations** - Robust schema management and versioning with **Flyway**.
- **Integration Testing** - Robust testing suite ensuring system reliability using **Testcontainers**.

## 📋 Roadmap

- [x] Password reset functionality
- [x] Email notifications system
- [x] reCAPTCHA Bot Protection
- [x] Passwordless email OTP login
- [x] Stripe Checkout online payments
- [ ] Customizable user avatars
- [ ] SMS appointment reminders
- [ ] Analytics dashboard

## 🛠️ Technologies

### Backend

- **Java 25** - LTS version
- **Spring Boot 4.0.0**
- **Flyway** - Database migrations and schema versioning
- **Valkey (Redis)** - High-performance data structure store used for efficient caching and short-lived email login OTP data
- **OAuth2, JWT & Refresh Tokens** - Secure authentication with external providers, short-lived access tokens, and HttpOnly refresh-token cookies
- **Google reCAPTCHA API** - Server-side validation of user interactions
- **Stripe API** - Online card payments with Stripe Checkout and webhook-based payment status updates
- **Gradle** - Build automation tool
- **MySQL** - Relational database management
- **Lombok** - Reduce boilerplate code

### Frontend

- **React** - Modern UI library
- **Bootstrap** - Responsive design framework
- **Vite** - Fast build tool and development server
- **Package Manager** - pnpm
- **State & Data Fetching** - TanStack Query, React Context
- **Testing** - Vitest & RTL
- **React Google reCAPTCHA** - Component for easy reCAPTCHA integration

### Infrastructure

- **Docker** - Containerization for easy deployment using custom multi-stage builds (_DockerFileBackend_, _DockerFileFrontend_)
- **Testcontainers** - Used for spinning up real Docker containers (MySQL, Redis/Valkey) during integration tests to ensure environment parity
- **WireMock** - Used for testing Stripe integration scenarios without calling the real Stripe API
- **Supabase** - Cloud storage for barber portfolio images and photo gallery

## 🚀 Getting Started

### Prerequisites

- Java 25 JDK
- Node.js
- MySQL 8.0+
- Valkey server (Docker files provide)
- Supabase account (for photo storage)
- Google reCAPTCHA Keys (Site Key and Secret Key)
- Stripe account and test API keys
- Stripe CLI (recommended for local webhook testing)
- Docker (optional)

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/chrisitstyle/yourbarbershop.git
cd yourbarbershop
```

**2. Database Initialization**

YourBarbershop uses **Flyway** for automatic database setup. You no longer need to manually import SQL dumps.

- **Automated (Docker):** Simply run `make up` / `make up-nc` or `docker-compose up`. Flyway will automatically create the schema and populate sample data.

- **Manual:** Create an empty database named `barbershop-with-roles`. On application startup, Flyway will detect the empty database and run all migrations found in `src/main/resources/db/migration`.

```bash
# Create MySQL database
mysql -u root -p
CREATE DATABASE barbershop_with_roles;
```

> **Note**: The SQL dump includes sample users for testing:
>
> - **Admin**: `admin@test.com` / `test1234`
> - **User (Customer)**: `johndoe@example.com` / `test1234`

**Note on Profiles:**

- **Default Profile:** Runs core migrations (schema).
- **Dev Profile:** Additionally runs repeatable migrations from db/migration/dev to populate the database with sample users and orders for testing.

**3. Configure Supabase**

```bash
# Create a Supabase project at https://supabase.com
# Create a storage bucket for portfolio images
# Copy your Supabase Project URL, Public API Key, and CDN URL from storage
```

**4. Configure OAuth2 Providers (Google & GitHub)**

To enable social login, you need to create OAuth applications on both platforms and get your Client IDs and Secrets:

**For Google OAuth:**

- Go to the [Google Cloud Console](https://console.cloud.google.com).

- Create a new project and navigate to APIs & Services > Credentials.

- Click Create Credentials > OAuth client ID (Choose "Web application").

- Add the following to Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`

- Copy the generated Client ID and Client Secret.

**For GitHub OAuth:**

- Go to your GitHub account Settings > Developer settings > OAuth Apps.

- Click New OAuth App.

- Set the Homepage URL to your frontend (e.g., `http://localhost:3000`).

- Set the Authorization callback URL to: `http://localhost:8080/login/oauth2/code/github`

- Copy the Client ID and generate a Client Secret. <br> <br>

**5. Configure Google reCAPTCHA v2**

To enable bot protection, you need to generate API keys from Google:

- Go to [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin).
- Register a new site, select **reCAPTCHA v2** ("I'm not a robot" checkbox).
- Add _localhost_ to the list of allowed domains.
- Copy your **Site Key** and **Secret Key**.

**6. Configure Stripe Payments**

To enable online card payments, create or use a Stripe account in test mode and copy your test **Secret key** from the Stripe Dashboard.

For local webhook testing, install and log in to the Stripe CLI. Then run the listener with the same Stripe secret key that your backend uses.

You can run the listener directly:

```powershell
stripe listen --api-key sk_test_your_secret_key --forward-to http://localhost:8080/stripe/webhook
```

Or use the Makefile command from the project root:

```bash
make stripe-listen
```

The Makefile loads variables from the root `.env` file if it exists, so `make stripe-listen` uses `STRIPE_SECRET_KEY` and forwards events to `STRIPE_FORWARD_URL` (`http://localhost:8080/stripe/webhook` by default).

The command prints a webhook signing secret beginning with `whsec_`. Use that value as `STRIPE_WEBHOOK_SECRET` in your backend environment and restart the backend after changing it.

> **Important**: The Stripe listener must use the same `sk_test_...` key as the backend. Otherwise, Checkout Sessions may be created successfully, but local webhook events will not reach your application.

**7. Configure environment variables**

**Backend configuration** - Create `backend/.env` file:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
JWT_SECRET_KEY=your-secret-key
JWT_ACCESS_EXPIRATION_MINUTES=15
REFRESH_TOKEN_DAYS=14
REFRESH_COOKIE_SECURE=false
REFRESH_COOKIE_SAME_SITE=Lax
MYSQL_USERNAME=yor-mysql-username
MYSQL_PASSWORD=your-mysql-password

# OAuth2 Configuration

GOOGLE_CLIENT_ID=your-google-id
GOOGLE_CLIENT_SECRET=your-google-secret
GITHUB_CLIENT_ID=your-github-id
GITHUB_CLIENT_SECRET=your-github-secret

# Valkey/Redis Configuration
VALKEY_HOST=localhost (default)
VALKEY_PORT=6379 (default)

# Google reCAPTCHA Secret Key
GOOGLE_RECAPTCHA_SECRET=your-recaptcha-secret-key

# Stripe Payments
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_signing_secret
FRONTEND_URL=http://localhost:3000
STRIPE_FORWARD_URL=http://localhost:8080/stripe/webhook
```

> **Note**: `JWT_ACCESS_EXPIRATION_MINUTES` is optional. If it is not set, the backend uses the default short access-token lifetime. Refresh token settings are also optional: `REFRESH_TOKEN_DAYS` controls refresh token lifetime, while `REFRESH_COOKIE_SECURE` and `REFRESH_COOKIE_SAME_SITE` control the refresh cookie flags. For local HTTP development use `REFRESH_COOKIE_SECURE=false` and `REFRESH_COOKIE_SAME_SITE=Lax`. For HTTPS production deployments use `REFRESH_COOKIE_SECURE=true` and `REFRESH_COOKIE_SAME_SITE=None`.

> **Note**: Passwordless email OTP login uses the existing mail configuration (`MAIL_USERNAME`, `MAIL_PASSWORD`) and Valkey/Redis configuration (`VALKEY_HOST`, `VALKEY_PORT`). No additional environment variables are required for this feature.

> **Important**: For `MAIL_PASSWORD`, you need to generate a Google App Password at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords). Regular Gmail passwords won't work for security reasons.

**Frontend configuration** - Create `frontend/.env` file:

```properties
VITE_SUPABASE_PROJECTURL=YourProjectURL
VITE_SUPABASE_PUBLICAPIKEY=ValueOfAPIKEY
VITE_SUPABASE_CDNURL=CDNURL_FROM_STORAGE
VITE_API_URL=Spring_URL
VITE_RECAPTCHA_SITE_KEY=your-recaptcha-site-key
```

Update `backend/src/main/resources/application.properties` to use these environment variables:

```properties
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/barbershop_with_roles
spring.datasource.username=${MYSQL_USERNAME}
spring. datasource.password=${MYSQL_PASSWORD}

# JWT / refresh token configuration
JWT_ACCESS_EXPIRATION_MINUTES=${JWT_ACCESS_EXPIRATION_MINUTES:15}
application.security.refresh-token-days=${REFRESH_TOKEN_DAYS:14}
application.security.refresh-cookie-secure=${REFRESH_COOKIE_SECURE:true}
application.security.refresh-cookie-same-site=${REFRESH_COOKIE_SAME_SITE:None}

# Mail configuration
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# Stripe configuration
stripe.api-base-url=https://api.stripe.com
stripe.secret-key=${STRIPE_SECRET_KEY}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET}
stripe.currency=pln
stripe.success-url=${FRONTEND_URL:http://localhost:3000}?payment=success
stripe.cancel-url=${FRONTEND_URL:http://localhost:3000}?payment=cancel
```

> **Security Note**: Never commit `.env` files to version control. Make sure they are included in `.gitignore`.
> Stripe secret keys (`sk_test_...`, `sk_live_...`) and webhook signing secrets (`whsec_...`) are sensitive and must only be used on the backend.

### Authentication features

YourBarbershop supports multiple authentication flows:

- **Email and password login** - traditional login with a hashed password, a short-lived access token response, and a refresh token stored in an HttpOnly cookie.
- **OAuth2 login** - social login through Google and GitHub. OAuth2 no longer exposes tokens in the redirect URL; it sets the refresh cookie and redirects the frontend to `/oauth2/redirect`.
- **Passwordless email OTP login** - users can request a one-time login code delivered to their email address and receive the same access-token plus refresh-cookie session after successful verification.

Access token and refresh token model:

1. Standard login, registration, email OTP login, and OAuth2 login create a session.
2. The backend returns a short-lived JWT `accessToken` in the JSON response.
3. The backend also sets an opaque `refresh_token` as an HttpOnly cookie. The raw refresh token is never stored in localStorage and is not exposed to JavaScript.
4. The frontend keeps the access token in memory and sends it in the `Authorization: Bearer <accessToken>` header.
5. When the access token expires, the frontend calls `POST /auth/refresh` with credentials included. The backend validates the refresh cookie, rotates/reissues the session, and returns a new access token.
6. `POST /auth/logout` revokes the refresh token and clears the refresh cookie.

Public refresh-token endpoints:

```http
POST /auth/refresh
POST /auth/logout
```

The email OTP login flow uses two public endpoints:

```http
POST /login/email-code/request
POST /login/email-code/verify
```

How it works:

1. The user enters their email address on the login page and requests a code.
2. The backend generates a 6-digit one-time code and sends it by email.
3. Only a hashed version of the code is stored in Valkey/Redis with a short TTL.
4. The user enters the code in the login form.
5. After successful verification, the backend deletes the code, returns an `AuthResponse` containing `accessToken`, `id`, and `role`, and sets the refresh token in an HttpOnly cookie.

The OTP flow also includes request cooldowns and verification attempt limits to reduce abuse. The request endpoint returns a generic success message so it does not reveal whether an email address exists in the database.

### Stripe payment flow

YourBarbershop supports online card payments for registered user orders and guest orders through Stripe Checkout.

Payment-related data is stored in a dedicated `payment` table instead of directly inside `user_order` or `guest_order`. Each payment is connected to either a registered user order or a guest order.

Supported payment methods:

- `GOTOWKA` - cash payment on site
- `KARTA_NA_MIEJSCU` - card payment on site
- `KARTA_ONLINE` - online card payment through Stripe Checkout

Supported payment statuses:

- `OCZEKUJE_NA_PLATNOSC` - payment is pending
- `OPLACONA` - payment has been successfully completed
- `NIEUDANA` - payment failed
- `WYGASLA` - Stripe Checkout Session expired
- `ZWROCONA` - payment was refunded
- `NIE_WYMAGANA` - payment is not required for a given case

Online payment flow:

1. The customer selects a service, visit date, and payment method.
2. For `KARTA_ONLINE`, the backend creates an order and a related payment record.
3. The backend creates a Stripe Checkout Session and returns `checkoutUrl` to the frontend.
4. The frontend redirects the customer to Stripe Checkout.
5. Stripe sends webhook events to `POST /stripe/webhook`.
6. After `checkout.session.completed` or `payment_intent.succeeded`, the backend updates the payment status to `OPLACONA`, stores the Stripe Payment Intent ID, and sets `paid_at`.

Local webhook listener:

```powershell
stripe listen --api-key sk_test_your_secret_key --forward-to http://localhost:8080/stripe/webhook
```

Or with Makefile:

```bash
make stripe-listen
```

You can also trigger a sample `checkout.session.completed` event from the Stripe CLI with:

```bash
make stripe-trigger
```

This is useful for checking whether the webhook endpoint accepts Stripe events. A triggered sample event may not update a specific local `payment` row unless its metadata contains a matching `paymentId`.

Use Stripe's test card for local testing:

```text
4242 4242 4242 4242
```

**8. Run the application**

```bash
# Using Makefile (recommended)
make up

# Or rebuild without Docker cache
make up-nc

# Manual Backend
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

> **Alternative**: If the Gradle command doesn't work, you can run the backend through IntelliJ IDEA:
>
> - Open the project in IntelliJ IDEA
> - Navigate to `BarbershopApplication.java`
> - Right-click and select "Run" or click the green play button

**9. Install and run the frontend**

```bash
cd frontend

pnpm install
pnpm start

# Or run in development mode with Vite
pnpm run dev
```

The application should now be running:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`

### Docker Deployment

You can use Docker Compose to run the entire application stack. First, create a .env file in the root directory of the project (use the provided **.env_example** as a template):

```bash
# ENVS FOR DOCKER

# Frontend
VITE_SUPABASE_PROJECTURL=YourProjectURL
VITE_SUPABASE_PUBLICAPIKEY=ValueOfAPIKEY
VITE_SUPABASE_CDNURL=CDNURL_FROM_STORAGE
VITE_API_URL=SPRING_API_URL
VITE_RECAPTCHA_SITE_KEY=your-recaptcha-site-key-frontend

# Backend
SPRING_DATASOURCE_URL=jdbc:mysql://database:3306/barbershop-with-roles
SPRING_DATASOURCE_USERNAME=db-username
SPRING_DATASOURCE_PASSWORD=db-password
JWT_SECRET_KEY=generate-your-secret-key
JWT_ACCESS_EXPIRATION_MINUTES=15
REFRESH_TOKEN_DAYS=14
REFRESH_COOKIE_SECURE=false
REFRESH_COOKIE_SAME_SITE=Lax
GOOGLE_RECAPTCHA_SECRET=your-recaptcha-secret-key-backend

# Stripe Payments
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_signing_secret
FRONTEND_URL=http://localhost:3000
STRIPE_FORWARD_URL=http://localhost:8080/stripe/webhook
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/payment/cancel

# Database
MYSQL_ROOT_PASSWORD=your-root-password
MYSQL_DATABASE=barbershop-with-roles

# Mail
MAIL_USERNAME=email-address
MAIL_PASSWORD=your-generated-password

# Github OAuth
GITHUB_CLIENT_ID=github-client-id
GITHUB_CLIENT_SECRET=github-client-secret

# Google OAuth
GOOGLE_CLIENT_ID=google-client-id
GOOGLE_CLIENT_SECRET=google-client-secret

### Redis/Valkey ###
VALKEY_HOST=valkey # (default for docker is valkey, default for springboot is localhost)
VALKEY_PORT=6379 # (default both)
```

> **Note**: Passwordless email OTP login uses the configured mail provider to send one-time login codes and Valkey/Redis to store short-lived hashed codes, cooldowns, and attempt counters. Refresh tokens are persisted in MySQL as hashes and sent to the browser only as HttpOnly cookies.

> **Note**: For local Stripe webhook testing with Docker, keep the backend exposed on `http://localhost:8080` and run `make stripe-listen` from your host machine. You can also run `stripe listen --api-key sk_test_your_secret_key --forward-to http://localhost:8080/stripe/webhook` manually.

Then run:

```bash
docker-compose up -d
```

**Using Makefile** - For convenience, you can use the provided Makefile commands:

```bash
# Build and start all containers
make up

# Build and start all containers WITHOUT using cache (fresh build)
make up-nc

# Stop and remove all containers and volumes
make down

# Start the local Stripe webhook listener
make stripe-listen

# Trigger a sample Stripe Checkout completion event
make stripe-trigger
```

The `make up` command automatically removes old volumes, rebuilds containers, and starts the stack. Use `make up-nc` when you want to force Docker to ignore cached layers and build everything from scratch.

The Stripe Makefile commands use variables loaded from the root `.env` file:

```env
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_FORWARD_URL=http://localhost:8080/stripe/webhook
```

Run the Stripe listener in a separate terminal while the backend is running:

```bash
make stripe-listen
```

When Stripe CLI prints a new `whsec_...` webhook signing secret, copy it to `STRIPE_WEBHOOK_SECRET` and restart the backend. Use `make stripe-trigger` only as a quick webhook smoke test; real Checkout payments are still the best way to verify payment metadata and database updates.

## Local GitHub Actions Testing with act

GitHub Actions workflows can be tested locally with [`act`](https://github.com/nektos/act). This is useful for checking workflow syntax and common CI steps before pushing changes to GitHub.

Example PowerShell commands:

```powershell
act push -W .github/workflows/frontend-build.yml -j build-frontend
act push -W .github/workflows/backend-build.yaml -j build --env-file .env.act
act push -W .github/workflows/db-migration-check.yaml -j validate-current-migrations
act push -W .github/workflows/security-audit.yaml -j audit-frontend
```

Backend integration tests use **Testcontainers**. When running the backend workflow locally with `act`, create a local `.env.act` file in the project root:

```env
TESTCONTAINERS_RYUK_DISABLED=true
TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal
```

The `.env.act` file is local-only and should not be committed. Add it to `.gitignore`:

```gitignore
.env.act
```

> **Note**: The `.env.act` file is only needed for local `act` runs. The real GitHub Actions environment should run the workflow without this local workaround.

## 📁 Project Structure

```
yourbarbershop/
├── backend/
│   ├── src/main/java/                 # Backend Java source code
│   ├── src/main/resources/
│   │   ├── data/                      # Database dump files
│   │   ├── db/migration/              # Flyway migration scripts
│   │   │   ├── dev/                   # Sample data for development
│   │   │   └── V1__...                # Core schema
│   │   ├── application.properties     # Configuration
│   │   └── application-dev.properties # Dev Profile Configuration
│   ├── .env                           # Backend environment variables (not in git)
│   └── build.gradle                   # Gradle build configuration
├── frontend/
│   ├── src/                           # React application source
│   ├── .env                           # Frontend environment variables (not in git)
│   └── package.json                   # Frontend dependencies and npm scripts
├── DockerFileBackend                  # Custom Dockerfile for Spring Boot backend
├── DockerFileFrontend                 # Custom Dockerfile for React frontend
├── docker-compose.yml                 # Docker orchestration configuration
├── .env                               # Docker environment variables (not in git)
└── Makefile                           # Docker shortcuts and commands
```

## 🧪 Frontend Testing

Frontend unit tests are written using **Vitest** and **React Testing Library (RTL)**.

To run the frontend tests, navigate to the `frontend` directory and execute:

- `pnpm test` - runs tests in watch mode.
- `pnpm test:run` - runs all tests once.

## 🗄️ Database Management (Flyway)

The database schema is managed through versioned migration files.

- **Versioned Migrations (V\_\_):** Used for permanent schema changes (tables, columns).

- **Repeatable Migrations (R\_\_):** Used for sample data and views, ensuring the dev environment is always up-to-date without version conflicts. <br><br>

The application uses MySQL with the following main tables:

- **user** - User accounts with roles (ADMIN, USER)
- **offer** - Available barbershop services
- **user_order** - Orders placed by registered users
- **guest_order** - Orders placed by guest customers
- **payment** - Payment records connected to user or guest orders, including payment method, payment status, Stripe Checkout Session ID, Payment Intent ID, amount, currency, and paid timestamp
- **password_reset_token** - Password recovery tokens
- **refresh_token** - Hashed refresh tokens used for session renewal, rotation, revocation, and logout

Email login OTP codes are stored in Valkey/Redis with TTL and are not persisted in MySQL. Refresh tokens are stored in MySQL only as hashes; the raw refresh token is sent to the browser in an HttpOnly cookie.

Portfolio images are stored in Supabase cloud storage.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Contact

**GitHub**: [@chrisitstyle](https://github.com/chrisitstyle)

---

⭐ If you find this project useful, please consider giving it a star!
