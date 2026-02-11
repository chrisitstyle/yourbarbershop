# YourBarbershop 💈

A comprehensive barbershop management application built as a Bachelor's Degree Thesis project. YourBarbershop streamlines daily operations for barbershop owners, from appointment scheduling to service management.

## ✨ Features

The application provides comprehensive management tools:

- **User Management** - Handle customer profiles and administrator accounts
- **Appointment System** - Schedule and manage customer visits efficiently
- **Service Catalog** - Create and manage service offerings with pricing
- **Order Processing** - Track service orders and customer history
- **Secure Authentication** - JWT-based authorization with password hashing
- **Role-Based Access Control** - Two roles: administrators and users (customers)
- **Photo Gallery** - Showcase barber portfolio and work examples stored in Supabase
- **Password Recovery** - Secure password reset functionality
- **Guest Orders** - Allow non-registered customers to book appointments

## 📋 Roadmap

- [x] Password reset functionality
- [ ] Email notifications system
- [ ] Customizable user avatars
- [ ] SMS appointment reminders
- [ ] Analytics dashboard

## 🛠️ Technologies

### Backend

- **Java 25** - LTS version
- **Spring Boot 4.0.0**
- **Gradle** - Build automation tool
- **MySQL** - Relational database management
- **Lombok** - Reduce boilerplate code
- **JWT** - JSON Web Tokens for authentication

### Frontend

- **React** - Modern UI library
- **Bootstrap** - Responsive design framework
- **Vite** - Fast build tool and development server

### Infrastructure

- **Supabase** - Cloud storage for barber portfolio images and photo gallery
- **Docker** - Containerization for easy deployment

## 🚀 Getting Started

### Prerequisites

- Java 25 JDK
- Node.js
- MySQL 8.0+
- Supabase account (for photo storage)
- Docker (optional)

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/chrisitstyle/yourbarbershop.git
cd yourbarbershop
```

2. **Set up the database**

```bash
# Create MySQL database
mysql -u root -p
CREATE DATABASE barbershop_with_roles;

# Import the database schema and sample data
USE barbershop_with_roles;
SOURCE backend/src/main/resources/data/barbershop-with-roles_dump.sql;
```

> **Note**: The SQL dump includes sample users for testing:
>
> - **Admin**: `admin@test.com` / `test1234`
> - **User (Customer)**: `johndoe@example.com` / `test1234`

3. **Configure Supabase**

```bash
# Create a Supabase project at https://supabase.com
# Create a storage bucket for portfolio images
# Copy your Supabase Project URL, Public API Key, and CDN URL from storage
```

4. **Configure environment variables**

**Backend configuration** - Create `backend/.env` file:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
JWT_SECRET_KEY=your-secret-key
MYSQL_USERNAME=yor-mysql-username
MYSQL_PASSWORD=your-mysql-password
```

> **Important**: For `MAIL_PASSWORD`, you need to generate a Google App Password at [myaccount.google. com/apppasswords](https://myaccount.google.com/apppasswords). Regular Gmail passwords won't work for security reasons.

**Frontend configuration** - Create `frontend/.env` file:

```env
VITE_SUPABASE_PROJECTURL=YourProjectURL
VITE_SUPABASE_PUBLICAPIKEY=ValueOfAPIKEY
VITE_SUPABASE_CDNURL=CDNURL_FROM_STORAGE
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

5. **Run the backend**

```bash
cd backend
./gradlew bootRun
```

> **Alternative**: If the Gradle command doesn't work, you can run the backend through IntelliJ IDEA:
>
> - Open the project in IntelliJ IDEA
> - Navigate to `BarbershopApplication.java`
> - Right-click and select "Run" or click the green play button

6. **Install and run the frontend**

```bash
cd frontend
npm install
npm start

# Or run in development mode with Vite
npm run dev
```

The application should now be running:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`

### Docker Deployment

You can use Docker Compose to run the entire application stack:

```bash
docker-compose up -d
```

**Using Makefile** - For convenience, you can use the provided Makefile commands:

```bash
# Build and start all containers
make up

# Stop and remove all containers
make down
```

The `make up` command will automatically rebuild containers and remove old volumes before starting.

## 📁 Project Structure

```
yourbarbershop/
├── backend/
│   ├── src/main/java/              # Backend Java source code
│   ├── src/main/resources/
│   │   ├── data/                   # Database dump files
│   │   └── application.properties  # Configuration
│   ├── .env                        # Backend environment variables (not in git)
│   └── build.gradle                # Gradle build configuration
├── frontend/
│   ├── src/                        # React application source
│   ├── .env                        # Frontend environment variables (not in git)
│   └── package.json
├── docker-compose.yml              # Docker configuration
└── Makefile                        # Docker shortcuts
```

## 🗄️ Database Schema

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
