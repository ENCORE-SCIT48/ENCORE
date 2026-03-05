package com.encore.encore.global.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용 비밀번호 BCrypt 해시 (개발 시에만 사용, 배포 전 삭제 권장)
 * 앱 기동 시 한 번만 생성해서 고정 → 매번 같은 해시 반환. 이 값을 SQL @pwd_hash / UPDATE 에 넣으면 됨.
 */
@RestController
@RequestMapping("/dev")
public class DevPasswordHashController {

    /** 한 번만 생성해서 계속 같은 값 반환 (바뀌지 않음) */
    private final String password123Hash;

    public DevPasswordHashController(PasswordEncoder passwordEncoder) {
        this.password123Hash = passwordEncoder.encode("password123");
    }

    @GetMapping("/bcrypt-password123")
    public String getPassword123Hash() {
        return password123Hash;
    }
}
