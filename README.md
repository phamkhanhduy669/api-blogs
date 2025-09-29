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

# Đăng ký 
Header "Content-Type: application/json" 



# Đăng nhập (lấy token)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"Password1"}' | jq -r '.token')

echo $TOKEN

# Tạo blog
curl -X POST http://localhost:8080/api/blogs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Bai viet 1","content":"Noi dung..."}'

# Lấy danh sách blog của chính user (USER chỉ thấy blog của mình)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/blogs

# Cập nhật blog
curl -X PUT http://localhost:8080/api/blogs/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Tieu de moi","content":"Noi dung moi"}'

# Xoá blog
curl -X DELETE http://localhost:8080/api/blogs/1 \
  -H "Authorization: Bearer $TOKEN"

# Admin xoá user
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

curl -X DELETE http://localhost:8080/api/users/user1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Lưu ý
- Thay `jq` bằng cách thủ công copy token nếu bạn không cài `jq`.
- Có thể mở rộng: pagination, search, refresh token, audit log.
