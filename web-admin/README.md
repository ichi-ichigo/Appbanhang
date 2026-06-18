# Smarteshop Web Admin

Trang admin web dạng dashboard, lấy cảm hứng từ ArchitectUI và dùng Bootstrap 4.

## Chạy

Mở trực tiếp `index.html` hoặc chạy server tĩnh:

```powershell
cd web-admin
python -m http.server 5500
```

Sau đó mở `http://localhost:5500`.

## Firebase

Trang này dùng Firebase Web SDK và collection `products`.

Các field chính:

- `id`
- `name`
- `category`
- `brand`
- `price`
- `stock`
- `imageUrl`
- `thumbnailUrl`
- `imageUrls`
- `description`

Firestore Rules cần cho phép tài khoản/trang admin đọc ghi collection `products`.
