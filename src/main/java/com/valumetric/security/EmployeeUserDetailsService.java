package com.valumetric.security;

import com.valumetric.document.Employee;
import com.valumetric.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Employee 기반 UserDetailsService (MongoDB 버전)
 */
@Service
@RequiredArgsConstructor
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ID 또는 이메일로 조회
        Employee employee = employeeRepository.findById(username)
                .or(() -> employeeRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("사원을 찾을 수 없습니다: " + username));

        return new User(
                employee.getId(),
                employee.getPassword(),
                employee.getIsEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())));
    }
}
