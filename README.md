# Blog REST API 

## Chức năng yêu cầu
1. Tạo REST API: GET, POST, PUT, DELETE cho resource `blogs` và quản lý `users`.
2. Xác thực & phân quyền dùng Spring Security + JWT.
   - User có trường `role` (ADMIN / USER).
   - Đăng ký, đăng nhập sinh JWT token.
   - USER chỉ được xem / tạo / sửa / xoá blog của chính mình.
   - ADMIN có toàn quyền (xem tất cả blog, xoá user, còn USER chỉ được xem/cập nhật blog của chính mình).

## Tài khoản seed mặc định
| Username | Password   | Role  |
|----------|------------|-------|
| admin    | admin123   | ADMIN |
| user1    | user1123   | USER  |
| user2    | user2123   | USER  |


## luồng xác thực
1. Đăng ký `POST /api/auth/register`
2. Đăng nhập `POST /api/auth/login` nhận JWT.
3. Gửi JWT ở header `Authorization: Bearer <token>` cho các request bảo vệ.

## POSTMAN

### Đăng ký 
<img width="1116" height="879" alt="Screenshot 2025-09-29 134249" src="https://github.com/user-attachments/assets/f211fadf-c44a-4eeb-bf3c-e8e17b2f0eb8" />

### Đăng nhập (lấy token)
<img width="1051" height="905" alt="Screenshot 2025-09-29 132032" src="https://github.com/user-attachments/assets/42dcbbad-c067-4a3a-89cb-e26b71516cfb" />

### Tạo blog
<img width="1116" height="890" alt="Screenshot 2025-09-29 133606" src="https://github.com/user-attachments/assets/9c6a7a67-9422-4f85-a611-2fb9884d2737" />


### Lấy danh sách blog của chính user (USER chỉ thấy blog của mình)
<img width="1119" height="939" alt="Screenshot 2025-09-29 132111" src="https://github.com/user-attachments/assets/c09a4051-7699-4365-aa32-433f11d61123" />


### Cập nhật blog
<img width="1116" height="834" alt="Screenshot 2025-09-29 133009" src="https://github.com/user-attachments/assets/deb1303a-ef19-40a4-af1d-c511afb5b609" />


### Xoá blog
<img width="1121" height="897" alt="Screenshot 2025-09-29 133630" src="https://github.com/user-attachments/assets/d9d52c7b-b428-4baf-8588-aa3266731759" />
### Danh sách user
<img width="1117" height="900" alt="Screenshot 2025-09-29 134402" src="https://github.com/user-attachments/assets/e445ca8a-269f-4bfb-9b30-7e76da56124b" />

### Admin xoá user
<img width="1122" height="905" alt="Screenshot 2025-09-29 133245" src="https://github.com/user-attachments/assets/0d83bd9d-cc84-4b45-9e0f-50ecc4138c01" />
