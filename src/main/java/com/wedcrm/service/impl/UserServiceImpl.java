package com.wedcrm.service.impl;

import com.wedcrm.dto.request.UserRequestDTO;
import com.wedcrm.dto.response.UserResponseDTO;
import com.wedcrm.dto.dashboard.UserSummaryDTO;
import com.wedcrm.entity.User;
import com.wedcrm.enums.Role;
import com.wedcrm.mapper.UserMapper;
import com.wedcrm.repository.UserRepository;
import com.wedcrm.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${wedcrm.upload.dir:uploads/avatars}")
    private String uploadDir;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserSummaryDTO> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toSummaryDTO);
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setEmailVerified(false);
        user = userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(UUID id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        userMapper.updateEntity(request, user);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user = userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void changePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public String uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String fileName = "avatar_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            String avatarUrl = "/uploads/avatars/" + fileName;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar avatar", e);
        }
    }
}