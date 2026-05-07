package com.wedcrm.service;

import com.wedcrm.dto.request.UserRequestDTO;
import com.wedcrm.dto.response.UserResponseDTO;
import com.wedcrm.dto.dashboard.UserSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    Page<UserSummaryDTO> listUsers(Pageable pageable);
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO getUserById(UUID id);
    UserResponseDTO updateUser(UUID id, UserRequestDTO request);
    void deleteUser(UUID id);
    void changePassword(UUID id, String newPassword);
    String uploadAvatar(String email, MultipartFile file);
}
