# Plan: Thiết kế giao diện ứng dụng Onis Japanese (Blue Modern Theme)

Tôi sẽ thực hiện thiết kế lại toàn bộ giao diện theo phong cách hiện đại, sử dụng tông màu xanh biển làm chủ đạo, tập trung vào trải nghiệm người dùng mượt mà và trực quan.

## 1. Cập nhật Theme & Màu sắc
- Định nghĩa lại bảng màu `Color.kt` với các sắc độ xanh biển (Deep Blue, Sky Blue) kết hợp với màu nền sáng sạch sẽ.
- Cấu hình `Theme.kt` để áp dụng bảng màu mới và các kiểu chữ (Typography) hiện đại.

## 2. Thiết kế các màn hình chức năng (Dữ liệu Mock)
### A. Trang Home (Dashboard)
- Ô tìm kiếm nổi bật với tính năng phân tích nhanh bên dưới.
- Hiển thị kết quả phân tích: Kanji, âm đọc, nghĩa và ví dụ.
- Thêm các thẻ gợi ý nội dung học tập.

### B. Trang AudioToText
- Khu vực tải lên file audio/video với hiệu ứng trực quan.
- Hiển thị kịch bản (script) sau khi "phân tích" (mock).
- Các công cụ đi kèm: Ghi âm, phát lại, trích xuất từ vựng.

### C. Trang ImageToText
- Giao diện tích hợp camera/gallery.
- Hiển thị ảnh mẫu và vùng văn bản được nhận diện.
- Phân tích chi tiết các từ vựng xuất hiện trong ảnh.

### D. Trang Kanji (Handwriting)
- Bảng vẽ tay (Canvas) để người dùng luyện viết và nhận diện.
- Hiển thị thông tin chi tiết về Kanji sau khi viết: Thứ tự nét, bộ thủ, JLPT level.

### E. Trang Flashcard
- Hệ thống thẻ học (Flashcard) sinh động.
- Tính năng vuốt/chạm để xem nghĩa.
- Quản lý bộ từ vựng từ lịch sử tra cứu hoặc thư viện.

## 3. Điều hướng (Navigation)
- Cập nhật `AppLayout.kt` với thanh điều hướng dưới (Bottom Navigation) có icon hiện đại và hiệu ứng chuyển trang mượt mà.

## 4. Kiểm tra và Hoàn thiện
- Đảm bảo tất cả các màn hình đều tuân thủ Material Design 3.
- Sử dụng hình ảnh mẫu chất lượng cao từ Unsplash (via Coil/AsyncImage).
- Tối ưu hóa layout cho các kích thước màn hình Android khác nhau.
