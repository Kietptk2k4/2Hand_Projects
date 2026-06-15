# Skeleton Service

**Template tham chiếu** để khởi tạo microservice Spring Boot mới trong monorepo 2Hands — **không phải** service chạy production.

Dựa trên cấu trúc `auth-service` (package `com.twohands.auth_service`, Flyway `V1__init_auth_tables.sql`) dùng làm điểm copy khi tạo service mới.

---

## Mục đích

| Dùng cho | Không dùng cho |
|----------|----------------|
| Copy package layout Clean Architecture | Deploy staging/production |
| Tham chiếu `build.gradle`, Security, Flyway setup | Thay thế auth-service |
| Onboarding dev mới | API gateway hay client gọi trực tiếp |

---

## Nội dung có sẵn

- **Gradle:** Spring Boot 3.5, Java 21, JPA, Redis, OAuth2 client, Flyway, Security, Actuator
- **Port mặc định:** `3001` (trùng auth — **đổi port** nếu chạy song song)
- **Migration:** `src/main/resources/db/migration/V1__init_auth_tables.sql` (schema auth mẫu)

---

## Khi tạo service mới từ skeleton

1. Copy thư mục → `Services/<tên-service>/`
2. Đổi `settings.gradle` `rootProject.name`
3. Rename package `auth_service` → `<tên>_service`
4. Tạo migration DB riêng + port trong `application.yml`
5. Thêm README, `.env.example`, `.env.docker.example`, docs FR theo `.cursor/rules/`
6. Thêm `Dockerfile` + đăng ký trong `Infrastructure/docker-compose.apps.yml` nếu service chạy trong Docker stack
6. Đăng ký port trong [monorepo README](../../README.md)

---

## Chạy (chỉ để thử template)

```bash
cd Services/skeleton-service
# Cấu hình DB/Redis tương tự auth-service — tránh conflict port 3001 với auth-service đang chạy
./gradlew bootRun
```

---

## Tài liệu chuẩn chung

- [`docs/architecture/system-architecture.md`](../../docs/architecture/system-architecture.md)
- [`docs/engineering_rules/backend-convention.md`](../../docs/engineering_rules/backend-convention.md)
- Service production tương ứng: [`Services/auth-service/README.md`](../auth-service/README.md)
