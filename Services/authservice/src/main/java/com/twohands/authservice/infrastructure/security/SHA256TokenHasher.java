package com.twohands.authservice.infrastructure.security;

import com.twohands.authservice.application.auth.port.TokenHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class SHA256TokenHasher implements TokenHasher {

    // Override the hash method to securely generate a one-way cryptographic representation of the given token.
    // Override method hash để tạo ra một chuỗi đại diện mã hóa một chiều an toàn cho token đầu vào.
    @Override
    public String hash(String token) {
        try {
            
            // Instantiate a MessageDigest object configured specifically for the SHA-256 hashing algorithm.
            // Khởi tạo một object MessageDigest được config chuyên biệt cho thuật toán băm SHA-256.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Convert the plain token into a UTF-8 byte array and execute the cryptographic hashing operation.
            // Convert chuỗi token gốc thành mảng byte chuẩn UTF-8 và thực thi operation băm dữ liệu (hashing).
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            
            // Format the resulting raw byte array into a human-readable Hexadecimal string and return it.
            // Format mảng byte raw kết quả thành một chuỗi Hexadecimal dễ đọc và return về.
            return HexFormat.of().formatHex(hashBytes);
            
        } catch (NoSuchAlgorithmException e) {
            
            // Catch the checked exception in the rare case where the JVM environment lacks support for SHA-256.
            // Catch checked exception trong trường hợp hiếm hoi môi trường JVM không support thuật toán SHA-256.
            
            // Wrap the error into an unchecked IllegalStateException to fail fast without forcing method signature changes.
            // Wrap lỗi này thành một unchecked IllegalStateException để fail-fast mà không ép phải thay đổi method signature.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
