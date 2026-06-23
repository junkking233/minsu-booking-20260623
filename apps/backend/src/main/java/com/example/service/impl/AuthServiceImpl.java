package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.dto.AuthUserDto;
import com.example.dto.ChangePasswordRequest;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.ProfileUpdateRequest;
import com.example.dto.RegisterRequest;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.UserMapper;
import com.example.service.AuthService;
import com.example.util.PasswordUtils;
import com.example.util.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    private final TokenUtils tokenUtils;

    public AuthServiceImpl(UserMapper userMapper, TokenUtils tokenUtils) {
        this.userMapper = userMapper;
        this.tokenUtils = tokenUtils;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
        if (user == null || !PasswordUtils.md5(request.getPassword()).equals(user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        long expiresAt = tokenUtils.expiresAtFromNow();
        String token = tokenUtils.generate(user, expiresAt);
        return new LoginResponse(token, AuthUserDto.from(user), expiresAt);
    }

    @Override
    public AuthUserDto register(RegisterRequest request) {
        User exist = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
        if (exist != null) {
            throw new BusinessException(409, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.md5(request.getPassword()));
        user.setName(StringUtils.hasText(request.getName()) ? request.getName() : request.getUsername());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        return AuthUserDto.from(user);
    }

    @Override
    public AuthUserDto getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        return AuthUserDto.from(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        if (!PasswordUtils.md5(request.getOldPassword()).equals(user.getPassword())) {
            throw new BusinessException(400, "原密码不正确");
        }

        User update = new User();
        update.setId(userId);
        update.setPassword(PasswordUtils.md5(request.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public AuthUserDto updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);

        return AuthUserDto.from(userMapper.selectById(userId));
    }
}
