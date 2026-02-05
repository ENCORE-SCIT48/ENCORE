package com.encore.encore.global.config;

import com.encore.encore.domain.user.entity.User;
import com.encore.encore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. DB에서 이메일로 사용자 찾기
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자가 없습니다: " + email));

        // 찾은 사용자 정보를 시큐리티가 이해할 수 있는 객체로 변환해서 반환
        return new CustomUserDetails(user);
    }
}
