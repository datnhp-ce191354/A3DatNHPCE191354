# A3DatNHPCE191354

Bài Assignment 03 môn SBA301: website đặt phòng với Spring Boot 3 REST API, Spring Data JPA, Spring Security/JWT, SQL Server và ReactJS. Hai project nằm trong [`backend/`](backend/) và [`frontend/`](frontend/).

## Chức năng

| Vai trò | Chức năng |
| --- | --- |
| Khách vãng lai | Xem, tìm kiếm và xem chi tiết phòng, xem loại phòng mà không cần đăng nhập |
| Khách hàng | Đăng ký, đăng nhập email/mật khẩu, cập nhật hồ sơ, đặt một hoặc nhiều phòng theo ngày, xem lịch sử đặt phòng |
| Nhân viên | Tìm kiếm và quản lý khách hàng, loại phòng, phòng, phiếu đặt phòng và chi tiết đặt phòng |

Khi xóa phòng chưa có giao dịch, hệ thống xóa bản ghi. Nếu phòng đã có chi tiết đặt phòng, hệ thống chuyển phòng sang `INACTIVE` để giữ lịch sử. Đặt phòng kiểm tra ngày và tình trạng trùng lịch; giá được tính trên số đêm của từng phòng.

## Yêu cầu chạy

- Java 21 và Maven 3.9+
- Node.js 20.19+ và npm
- Microsoft SQL Server đang chạy, cho phép kết nối JDBC

## 1. Tạo database

Tạo database `A3DatNHPCE191354` trong SQL Server. Có thể chạy file [`backend/sql/create-database.sql`](backend/sql/create-database.sql) bằng SQL Server Management Studio hoặc chạy câu lệnh:

```sql
CREATE DATABASE A3DatNHPCE191354;
```

JPA (`ddl-auto=update`) tự tạo/cập nhật bảng khi backend kết nối thành công vào database. Tài khoản SQL Server cần quyền tạo bảng. Dùng các biến môi trường sau để cấu hình kết nối:

| Biến | Giá trị mặc định / ý nghĩa |
| --- | --- |
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=A3DatNHPCE191354;encrypt=true;trustServerCertificate=true` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | Trống; cần đặt theo SQL Server trên máy |
| `STAFF_EMAIL`, `STAFF_PASSWORD`, `STAFF_NAME` | Tài khoản nhân viên trong `application.properties` (mặc định cho demo: `staff@hotel.local` / `Staff@12345`) |
| `JWT_SECRET` | Khóa ký JWT, tối thiểu 32 byte; nên đặt riêng ngoài môi trường local |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |

Ví dụ PowerShell trước khi chạy backend:

```powershell
$env:DB_PASSWORD = 'your-sql-server-password'
$env:JWT_SECRET = 'replace-with-a-random-secret-at-least-32-bytes'
cd backend
mvn spring-boot:run
```

Backend mặc định ở `http://localhost:8080`.

## 2. Chạy React

Mở terminal thứ hai:

```powershell
cd frontend
npm install
npm run dev
```

Mở `http://localhost:5173`. Mặc định frontend gọi `http://localhost:8080`; có thể đặt `VITE_API_BASE_URL` trong file `frontend/.env` dựa trên [`frontend/.env.example`](frontend/.env.example).

## API chính

| Method | Endpoint | Quyền |
| --- | --- | --- |
| GET | `/api/rooms`, `/api/rooms/{id}`, `/api/room-types` | Công khai |
| POST | `/api/auth/register`, `/api/auth/login` | Công khai |
| GET, PUT | `/api/customers/me` | Customer JWT |
| GET, POST | `/api/bookings/me` | Customer JWT |
| GET, POST, PUT, DELETE | `/api/staff/customers`, `/api/staff/rooms`, `/api/staff/room-types`, `/api/staff/bookings` | Staff JWT |

Các endpoint theo ID dùng `/{id}`. JWT truyền bằng header `Authorization: Bearer <token>`. Xem request mẫu và biến token trong [`postman/A3DatNHPCE191354.postman_collection.json`](postman/A3DatNHPCE191354.postman_collection.json).

## Kiểm tra

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

Các bài test backend dùng H2 riêng, không sửa dữ liệu SQL Server. Để kiểm tra luồng hoàn chỉnh, chạy backend/frontend và thử collection Postman hoặc thao tác trên giao diện.
