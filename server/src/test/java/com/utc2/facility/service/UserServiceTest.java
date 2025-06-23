package com.utc2.facility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.utc2.facility.dto.request.UserCreationRequest;
import com.utc2.facility.dto.response.UserResponse;
import com.utc2.facility.entity.User;
import com.utc2.facility.entity.Role;
import com.utc2.facility.exception.AppException;
import com.utc2.facility.exception.ErrorCode;
import com.utc2.facility.repository.RoleRepository;
import com.utc2.facility.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RoleRepository roleRepository;

    private UserCreationRequest request;
    private UserResponse userResponse;
    private User user;

    @BeforeEach
    void initData() {
        request = UserCreationRequest.builder()
                .userId("6451071004")
                .username("bienbeo")
                .email("6451071004@st.utc2.edu.vn")
                .fullName("Bien Beo")
                .roleName("USER")
                .build();

        userResponse = UserResponse.builder()
                .id("abcxyz")
                .userId("6451071004")
                .username("bienbeo")
                .fullName("Bien Beo")
                .roleName("USER")
                .email("6451071004@st.utc2.edu.vn")
                .avatar("https://example.com/avatar.jpg")
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();

        user = User.builder()
                .id("abcxyz")
                .userId("6451071004")
                .username("bienbeo")
                .fullName("Bien Beo")
                .email("6451071004@st.utc2.edu.vn")
                .role(Role.builder().name(com.utc2.facility.enums.Role.USER).build())
                .avatar("https://example.com/avatar.jpg")
                .createdAt(LocalDate.now().atStartOfDay())
                .updatedAt(LocalDate.now().atStartOfDay())
                .build();

        when(roleRepository.findByName(any())).thenReturn(
                Role.builder().name(com.utc2.facility.enums.Role.USER).build()
        );

    }

    @BeforeEach
    void setupAuthentication() {
        var auth = new UsernamePasswordAuthenticationToken(
                "adminUser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userRepository.existsByUserId(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // WHEN
        var response = userService.createUser(request);

        // THEN
        assertThat(response.getUserId()).isEqualTo("6451071004");
    }

    @Test
    void createUser_userExisted_fail() {
        // GIVEN
        when(userRepository.existsByUserId(anyString())).thenReturn(true);

        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createUser(request));

        // THEN
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTED);
    }

}
