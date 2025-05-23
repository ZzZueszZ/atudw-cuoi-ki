# An Toàn Ứng Dụng Web - Demo Lỗ Hổng Bảo Mật (Nhóm 4)

Ứng dụng web Spring Boot này được xây dựng nhằm mục đích minh họa các lỗ hổng bảo mật web phổ biến và cách phòng chống chúng. Dự án được tạo ra cho mục đích học tập và nghiên cứu.

**LƯU Ý QUAN TRỌNG:** Đây là một ứng dụng được thiết kế cố ý có lỗ hổng bảo mật. **TUYỆT ĐỐI KHÔNG TRIỂN KHAI ỨNG DỤNG NÀY TRÊN MÔI TRƯỜNG PRODUCTION HOẶC BẤT KỲ MÔI TRƯỜNG NÀO CÓ THỂ TRUY CẬP CÔNG KHAI MÀ KHÔNG CÓ BIỆN PHÁP BẢO VỆ NGHIÊM NGẶT.**

## Các Lỗ Hổng Được Minh Họa

Ứng dụng này minh họa các lỗ hổng sau:

1.  **Cross-Site Scripting (XSS)**
    *   **Reflected XSS:** Mã độc được chèn vào URL và thực thi khi nạn nhân truy cập URL đó.
    *   **Stored XSS:** Mã độc được lưu trữ trên máy chủ (ví dụ: trong cơ sở dữ liệu, tin nhắn) và thực thi khi người dùng truy cập trang chứa mã độc đó.
2.  **File Upload Vulnerabilities (Lỗ Hổng Tải Tệp Lên)**
    *   Cho phép tải lên các tệp nguy hiểm (web shells, tệp chứa mã XSS).
    *   Không kiểm tra loại tệp, kích thước tệp.
    *   Sử dụng tên tệp gốc, có thể dẫn đến Path Traversal.
3.  **Path Traversal (Directory Traversal)**
    *   Cho phép truy cập các tệp và thư mục bên ngoài thư mục web root dự kiến bằng cách thao túng các tham số đầu vào chứa đường dẫn tệp.
4.  **SQL Injection (SQLi)**
    *   Cho phép kẻ tấn công chèn các câu lệnh SQL tùy ý vào các truy vấn cơ sở dữ liệu, có thể dẫn đến rò rỉ dữ liệu, sửa đổi hoặc xóa dữ liệu.
5.  **Command Injection (OS Command Injection)**
    *   Cho phép kẻ tấn công thực thi các lệnh hệ điều hành tùy ý trên máy chủ thông qua việc khai thác các hàm hoặc ứng dụng gọi lệnh hệ thống với đầu vào không được kiểm soát.

## Công Nghệ Sử Dụng

*   **Java:** Ngôn ngữ lập trình chính.
*   **Spring Boot:** Framework để xây dựng ứng dụng web Java một cách nhanh chóng.
*   **Thymeleaf:** Template engine để tạo các trang HTML động.
*   **Maven:** Công cụ quản lý dự án và build.
*   **H2 Database:** Cơ sở dữ liệu trong bộ nhớ (sử dụng cho demo SQLi).
*   **Bootstrap 5:** Framework CSS để thiết kế giao diện người dùng.

## Cài Đặt và Chạy Ứng Dụng

### Yêu cầu:

*   Java Development Kit (JDK) phiên bản 17 trở lên.
*   Apache Maven.

### Các bước cài đặt:

1.  **Clone repository (Nếu có):**
    ```bash
    git clone <URL_REPO_CUA_BAN>
    cd <TEN_THU_MUC_REPO>
    ```
    Hoặc tải mã nguồn dưới dạng ZIP và giải nén.

2.  **Build dự án:**
    Mở terminal hoặc command prompt trong thư mục gốc của dự án và chạy lệnh:
    ```bash
    mvn clean install
    ```

3.  **Chạy ứng dụng:**
    Có nhiều cách để chạy ứng dụng:
    *   **Sử dụng Maven Spring Boot plugin:**
        ```bash
        mvn spring-boot:run
        ```
    *   **Chạy file JAR đã build (trong thư mục `target`):**
        ```bash
        java -jar target/demo-0.0.1-SNAPSHOT.jar
        ```
        (Tên file JAR có thể khác tùy thuộc vào cấu hình dự án của bạn).
    *   **Chạy từ IDE (ví dụ: IntelliJ IDEA, Eclipse):** Mở dự án như một Maven project và chạy lớp chính `DemoApplication.java` (trong `src/main/java/.../demo`).

4.  **Truy cập ứng dụng:**
    Sau khi ứng dụng khởi động thành công (thường trên cổng 8080), mở trình duyệt và truy cập:
    `http://localhost:8080/`

## Cách Sử Dụng

1.  Truy cập trang chủ (`http://localhost:8080/`) để xem danh sách các lỗ hổng được minh họa.
2.  Chọn một lỗ hổng từ danh sách để vào trang demo tương ứng.
3.  Mỗi trang demo thường có hai phiên bản:
    *   **Phiên bản không an toàn (Unsafe/Vulnerable):** Cho phép bạn thử nghiệm và khai thác lỗ hổng.
    *   **Phiên bản an toàn (Safe/Secure):** Minh họa cách phòng chống lỗ hổng đó.
4.  Làm theo các hướng dẫn và gợi ý trên từng trang để thử các payload tấn công khác nhau và quan sát kết quả.

## Cấu Trúc Thư Mục (Sơ lược)

```
.demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/         # Mã nguồn Java (Controllers, Entities, Services, etc.)
│   │   │   ├── controller/                # Các Spring MVC Controllers cho từng lỗ hổng
│   │   │   ├── model/                     # Các đối tượng Entity (ví dụ: User cho SQLi)
│   │   │   └── DemoApplication.java       # Lớp chính của Spring Boot
│   │   └── resources/
│   │       ├── static/                    # Các tệp tĩnh (CSS, JS, Images) - ít dùng trong demo này
│   │       ├── templates/                 # Các template Thymeleaf (.html)
│   │       │   ├── fragments/             # Các đoạn HTML tái sử dụng (ví dụ: header)
│   │       │   ├── xss/                   # Templates cho XSS
│   │       │   ├── fileupload/            # Templates cho File Upload
│   │       │   ├── pathtraversal/         # Templates cho Path Traversal
│   │       │   ├── sqli/                  # Templates cho SQL Injection
│   │       │   ├── commandinjection/      # Templates cho Command Injection
│   │       │   └── index.html             # Trang chủ
│   │       └── application.properties     # Cấu hình ứng dụng (ví dụ: cổng server, H2 DB)
│   └── test/                              # Mã nguồn cho tests (nếu có)
├── target/                                # Thư mục chứa kết quả build (bao gồm file .jar)
├── uploads/                               # Thư mục lưu trữ các tệp tải lên (tạo tự động)
│   ├── safe/
│   └── unsafe/
├── pom.xml                                # Tệp cấu hình Maven
└── README.md                              # Tệp này
```

## Từ Chối Trách Nhiệm

Ứng dụng này được cung cấp "NGUYÊN TRẠNG" và chỉ dành cho mục đích giáo dục. Người tạo ra ứng dụng không chịu trách nhiệm cho bất kỳ việc sử dụng sai mục đích hoặc thiệt hại nào có thể phát sinh từ việc sử dụng hoặc thử nghiệm ứng dụng này.

Hãy sử dụng một cách có trách nhiệm!
