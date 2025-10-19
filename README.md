# Bharat Social Media - Backend

A robust social media application backend built with Spring Boot, featuring real-time messaging, user management, and comprehensive social features.

## Features

- User authentication and authorization with JWT
- Real-time messaging with WebSocket
- Group messaging and management
- Post creation, likes, and comments
- Stories and reels functionality
- Notification system
- File upload and media handling
- RESTful API design

## Tech Stack

- Spring Boot 3.2.5
- Spring Security
- Spring WebSocket
- Spring Data JPA
- MySQL Database
- JWT Authentication
- Maven

## Author

**Bharat** - Full Stack Developer

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Installation

1. Clone the repository
2. Configure database connection in `application.properties`
3. Run the application:

```bash
mvn spring-boot:run
```

### API Documentation

The application runs on `http://localhost:5000`

### Database Configuration

Update `application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/social
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Built with ❤️ by Bharat
