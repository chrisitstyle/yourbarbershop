# YourBarbershop 💈

A comprehensive barbershop management application built as a Bachelor's Degree Thesis project. YourBarbershop streamlines daily operations for barbershop owners, from appointment scheduling to service management.

## ✨ Features

The application provides comprehensive management tools:

- **User Management** - Handle customer profiles and administrator accounts
- **Appointment System** - Schedule and manage customer visits efficiently
- **Service Catalog** - Create and manage service offerings with pricing
- **Order Processing** - Track service orders and customer history
- **Secure Authentication** - JWT-based authorization with password hashing
- **Bot Protection** - Integrated Google reCAPTCHA v2 to secure registration and password recovery flows.
- **Role-Based Access Control** - Two roles: administrators and users (customers)
- **Photo Gallery** - Showcase barber portfolio and work examples stored in Supabase
- **Password Recovery** - Secure password reset functionality
- **Guest Orders** - Allow non-registered customers to book appointments
- **Email Notifications System** - Automated appointment confirmations sent directly to customers' email addresses
- **Automated Database Migrations** - Robust schema management and versioning with **Flyway**.
- **Integration Testing** - Robust testing suite ensuring system reliability using **Testcontainers**.

## 📋 Roadmap

- [x] Password reset functionality
- [x] Email notifications system
- [x] reCAPTCHA Bot Protection
- [ ] Customizable user avatars
- [ ] SMS appointment reminders
- [ ] Analytics dashboard

## 🛠️ Technologies

### Backend

- **Java 25** - LTS version
- **Spring Boot 4.0.0**
- **Flyway** - Database migrations and schema versioning
- **Valkey (Redis)** - High-performance data structure store used for efficient caching
- **OAuth2 & JWT** - Secure authentication with external providers and JSON Web Tokens
- **Google reCAPTCHA API** - Server-side validation of user interactions
- **Gradle** - Build automation tool
- **MySQL** - Relational database management
- **Lombok** - Reduce boilerplate code

### Frontend

- **React** - Modern UI library
- **Bootstrap** - Responsive design framework
- **Vite** - Fast build tool and development server
- **React Google reCAPTCHA** - Component for easy reCAPTCHA integration

### Infrastructure

- **Docker** - Containerization for easy deployment using custom multi-stage builds (_DockerFileBackend_, _DockerFileFrontend_)
- **Testcontainers** - Used for spinning up real Docker containers (MySQL, Redis/Valkey) during integration tests to ensure environment parity
- **Supabase** - Cloud storage for barber portfolio images and photo gallery

## 🚀 Getting Started

### Prerequisites

- Java 25 JDK
- Node.js
- MySQL 8.0+
- Valkey server (Docker files provide)
- Supabase account (for photo storage)
- Google reCAPTCHA Keys (Site Key and Secret Key)
- Docker (optional)

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/chrisitstyle/yourbarbershop.git
cd yourbarbershop
```

**2.Database Initialization**

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

**6. Configure environment variables**

**Backend configuration** - Create `backend/.env` file:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
JWT_SECRET_KEY=your-secret-key
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
```

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

# Mail configuration
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

> **Security Note**: Never commit `.env` files to version control. Make sure they are included in `.gitignore`.

**7. Run the application**

```bash
# Using Makefile (recommended)
make up / make up-nc

# Manual Backend
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

> **Alternative**: If the Gradle command doesn't work, you can run the backend through IntelliJ IDEA:
>
> - Open the project in IntelliJ IDEA
> - Navigate to `BarbershopApplication.java`
> - Right-click and select "Run" or click the green play button

**8. Install and run the frontend**

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
GOOGLE_RECAPTCHA_SECRET=your-recaptcha-secret-key-backend

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

# Stop and remove all containers
make down
```

The `make up` command will automatically rebuild containers and remove old volumes before starting. Use `make up-nc` when you want to force Docker to ignore cached layers and build everything from scratch.

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

## 🗄️ Database Management (Flyway)

The database schema is managed through versioned migration files.

- **Versioned Migrations (V\_\_):** Used for permanent schema changes (tables, columns).

- **Repeatable Migrations (R\_\_):** Used for sample data and views, ensuring the dev environment is always up-to-date without version conflicts. <br><br>

The application uses MySQL with the following main tables:

- **user** - User accounts with roles (ADMIN, USER)
- **offer** - Available barbershop services
- **user_order** - Orders placed by registered users
- **guest_order** - Orders placed by guest customers
- **password_reset_token** - Password recovery tokens

Portfolio images are stored in Supabase cloud storage.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Contact

**GitHub**: [@chrisitstyle](https://github.com/chrisitstyle)

---

⭐ If you find this project useful, please consider giving it a star!
