# TRUST_CHAT

A trust-based chat system with policy enforcement for educational environments.

## Overview

TRUST_CHAT is a Java web application that enables secure messaging between users (admin, teachers, and students) with built-in policy engine to enforce chat rules and restrictions.

## Features

- User authentication (register/login)
- Role-based access control (admin, teacher, student)
- Real-time messaging
- Policy engine for content filtering
- Admin dashboard for managing policies
- Message logging and audit trail

## Tech Stack

- Java 11
- Servlet/JSP
- MySQL 8.0
- Tomcat 7/9
- Maven

## Project Structure

```
TRUST_CHAT/
├── src/main/java/com/trustchat/
│   ├── servlet/        # HTTP request handlers
│   ├── model/          # Data models
│   ├── dao/           # Data access layer
│   ├── engine/        # Policy engine
│   └── util/          # Utilities
├── src/main/webapp/
│   ├── WEB-INF/jsp/   # JSP pages
│   └── css/           # Stylesheets
├── sql/
│   └── schema.sql     # Database schema
└── pom.xml
```

## Setup

### Prerequisites

- Java 11+
- MySQL 8.0+
- Maven 3.6+

### Database Setup

1. Create the database:
```sql
CREATE DATABASE trust_chat;
```

2. Run the schema:
```bash
mysql -u root -p trust_chat < sql/schema.sql
```

### Configuration

Update database connection in `DBUtil.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/trust_chat";
private static final String DB_USER = "your_username";
private static final String DB_PASS = "your_password";
```

### Build and Run

```bash
mvn clean package
mvn tomcat7:run
```

Access at: http://localhost:9090/trust-chat

## Default Users

| Role     | Username   | Password    |
|----------|------------|-------------|
| admin    | admin      | admin123    |
| teacher  | teacher1   | teacher123  |
| student  | student1   | student123  |

## Policy Types

- **keyword**: Block specific words in messages
- **time**: Restrict messaging to certain hours
- **user_role**: Control communication between user roles

## API Endpoints

- `/login` - User login
- `/register` - User registration
- `/chat` - Chat interface
- `/dashboard` - User dashboard
- `/admin` - Admin panel

## License

MIT