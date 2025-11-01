# Auth-Service - Hệ Thống Xác Thực Người Dùng

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/[username]/auth-service/actions) [![Coverage](https://img.shields.io/badge/coverage-95%25-brightgreen.svg)](https://codecov.io/gh/[username]/auth-service) [![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot) [![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)

## 📋 Mô Tả
Auth-Service là một microservice backend xử lý xác thực và ủy quyền cho ứng dụng e-commerce (dựa trên các role như ADMIN, SELLER, CUSTOMER). Xây dựng bằng **Spring Boot 3.x**, sử dụng **JWT** cho token-based authentication, **MariaDB** làm database chính, **Redis** cho caching/session/OTP, và **Kafka** cho event streaming. Service hỗ trợ đăng ký/đăng nhập qua email/password, quản lý role/permission, cleanup token hết hạn tự động, và tích hợp Feign client để gọi các service khác (như user-service).

Dự án tập trung vào bảo mật (OAuth2 Resource Server, BCrypt encoding) và scalability (batch processing, scheduling).

### 🏗️ Architecture
Kiến trúc microservices với Auth-Service làm core cho authentication. Các thành phần chính:
- **Communication**: REST API (Feign cho inter-service), Kafka cho events (NotificationEvent).
- **Database**: MariaDB (JPA/Hibernate), Redis (caching OTP, sessions).
- **Security**: JWT (HS512), Role-based Access Control (RBAC) với permissions.
- **Deployment**: Docker + Kubernetes (giả định), port 8080.

*(Diagram mẫu - thay bằng Draw.io nếu cần. Dưới là Mermaid code, GitHub sẽ render tự động:)*

```mermaid
graph TD
    A[Client/App] -->|REST API| B[Auth-Service (Port 8080)]
    B -->|JWT Decode| C[CustomJwtDecoder]
    B -->|Save/Invalidate| D[Redis (OTP/Session)]
    B -->|Persist User/Role| E[MariaDB (auth_db)]
    B -->|Events| F[Kafka (NotificationEvent)]
    B -->|Feign Client| G[User-Service (Port 8082)]
    H[Batch Job] -->|Cleanup| I[InvalidatedToken Table]
    style B fill:#f9f,stroke:#333,stroke-width:2px
```

## ✨ Tính Năng Chính
- **Authentication**: Đăng nhập email/password, refresh token, logout (invalidate token), verify JWT/OTP.
- **Authorization**: RBAC với roles (ADMIN, SELLER, CUSTOMER) và permissions (e.g., CREATE_USER, VIEW_PRODUCT).
- **User Management**: Tạo/cập nhật user, assign/revoke role, change password.
- **OTP & Registration**: Gửi/verify OTP qua email, auto-register với pending data in Redis.
- **Batch Processing**: Tự động cleanup token hết hạn (scheduled job, configurable cron).
- **Integration**: Feign cho user-service, Kafka cho notifications, S3 upload (permission-based).
- **Error Handling**: Standardized ApiResponse với ErrorCode (e.g., UNAUTHORIZED, INCORRECT_OTP).

## 🛠️ Tech Stack
| Component          | Technology                  | Details                                      |
|--------------------|-----------------------------|----------------------------------------------|
| **Language/Framework** | Java 17+ / Spring Boot 3.x | REST Controllers, JPA, Security              |
| **Database**       | MariaDB                     | JPA entities (User, Role, Permission, InvalidatedToken) |
| **Cache/Session**  | Redis                       | OTP storage, token invalidation              |
| **Messaging**      | Apache Kafka                | NotificationEvent (channel, recipient, template) |
| **Security**       | Spring Security (OAuth2)    | JWT (Nimbus), BCrypt, Method Security (@PreAuthorize) |
| **Client**         | OpenFeign                   | Calls to user-service (e.g., /users/create)  |
| **Utils**          | Lombok, Jackson, Nimbus JOSE | DTOs, JSON handling, JWT parsing             |
| **Batch/Scheduling** | Spring Batch/Scheduler     | InvalidTokenCleanupJob (cron-based)          |

## 🚀 Cài Đặt & Chạy
### Yêu Cầu
- Java 17+ / Maven 3.6+.
- Docker (cho MariaDB, Redis, Kafka).
- Environment vars: `DBMS_CONNECTION`, `JWT_SIGNERKEY`, `FEIGN_USER` (xem application.yml).

### Bước 1: Clone Repo
```bash
git clone https://github.com/shopping-ecommerce/auth-service.git
cd auth-service
```

### Bước 2: Setup Môi Trường
```bash
# Copy env files đưa vào application.yml
cp src/main/resources/application.yml application.yml

# Build project
mvn clean install

# Setup Docker services (MariaDB, Redis, Kafka)
docker-compose up -d  # Sử dụng docker-compose.yml nếu có
```

### Bước 3: Chạy Service
```bash
# Run với Maven
mvn spring-boot:run

# Hoặc JAR
java -jar target/auth-service-*.jar
```

- Port mặc định: **8080**.
- Test endpoints: Sử dụng Postman/Swagger (http://localhost:8080/swagger-ui.html nếu enable).

Ví dụ test login:
```bash
curl -X POST http://localhost:8080/authentication/login-email-password \
  -H "Content-Type: application/json" \
  -d '{"email":"admin","password":"admin"}'
```

### Bước 4: Test & Debug
```bash
# Run tests
mvn test

# Check logs
tail -f logs/application.log  # Hoặc console output
```

- Admin default: email `admin`, password `admin` (tạo tự động qua ApplicationInitConfig).
- Verify: `GET /authentication/verify-jwt` với header `Authorization: Bearer <jwt>`.

## 📚 Tài Liệu
- **API Docs**: Sử dụng SpringDoc OpenAPI (Swagger UI tại `/swagger-ui.html`).
- **Endpoints**:
  | Method | Endpoint                          | Description         | Auth Required    |
  |--------|-----------------------------------|---------------------|------------------|
  | POST   | `/authentication/login-email-password` | Đăng nhập          | No               |
  | POST   | `/authentication/register`        | Đăng ký user        | No               |
  | POST   | `/authentication/verifyOTP`       | Xác thực OTP        | No               |
  | GET    | `/users`                          | Lấy tất cả users    | Yes (ADMIN)      |
  | PUT    | `/users/{id}`                     | Cập nhật user       | Yes (UPDATE_USER)|
  | POST   | `/roles`                          | Tạo role            | Yes              |
- **Deployment Guide**: Xem `docs/deploy.md` (Kubernetes manifests cho microservices).
- **Contributing Guide**: Xem `CONTRIBUTING.md`.

## 🤝 Đóng Góp
- Tuân thủ code style: Checkstyle, Lombok annotations.
- Test coverage >80% trước merge.
  Pull requests welcome! Báo issue nếu bug hoặc feature request.

## 👥 Liên Hệ
- Author: [Hồ Huỳnh Hoài Thịnh] ([@github-hohuynhhoaithinh](https://github.com/hohuynhhoaithinh))
- Email: [hohuynhhoaithinh@gmail.com]
---

*Cảm ơn bạn đã sử dụng Auth-Service! 🚀*