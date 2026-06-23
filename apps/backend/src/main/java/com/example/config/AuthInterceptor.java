package com.example.config;

import com.example.exception.BusinessException;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.util.TokenSubject;
import com.example.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenUtils tokenUtils;

    private final UserMapper userMapper;

    public AuthInterceptor(TokenUtils tokenUtils, UserMapper userMapper) {
        this.tokenUtils = tokenUtils;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }

        TokenSubject tokenSubject = tokenUtils.parse(authorization.substring(7));
        User user = userMapper.selectById(tokenSubject.getUserId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }

        TokenSubject subject = new TokenSubject(user.getId(), user.getUsername(), user.getRole(), tokenSubject.getExpiresAt());
        request.setAttribute("currentUser", subject);
        checkPermission(request.getRequestURI(), request.getMethod(), subject);
        return true;
    }

    private void checkPermission(String path, String method, TokenSubject subject) {
        // /api/admin/** 需要 ADMIN 角色
        if (path.startsWith("/api/admin") && !"ADMIN".equals(subject.getRole())) {
            throw new BusinessException(403, "无权访问管理接口");
        }

        // /api/users/** 需要 ADMIN 角色
        if (path.startsWith("/api/users") && !"ADMIN".equals(subject.getRole())) {
            throw new BusinessException(403, "无权访问用户管理接口");
        }

        // /api/orders/** 创建订单和我的订单需要 USER 角色
        if (path.startsWith("/api/orders")) {
            if (path.matches("/api/orders/?$") && "POST".equalsIgnoreCase(method)) {
                if (!"USER".equals(subject.getRole()) && !"ADMIN".equals(subject.getRole())) {
                    throw new BusinessException(403, "无权创建订单");
                }
            }
            if (path.contains("/my") && !"USER".equals(subject.getRole()) && !"ADMIN".equals(subject.getRole())) {
                throw new BusinessException(403, "无权查看个人订单");
            }
        }

        // /api/favorites/** 需要 USER 角色
        if (path.startsWith("/api/favorites") && !"USER".equals(subject.getRole()) && !"ADMIN".equals(subject.getRole())) {
            throw new BusinessException(403, "无权访问收藏功能");
        }

        // /api/reviews POST 需要 USER 角色
        if (path.startsWith("/api/reviews") && "POST".equalsIgnoreCase(method)) {
            if (!"USER".equals(subject.getRole()) && !"ADMIN".equals(subject.getRole())) {
                throw new BusinessException(403, "无权创建评价");
            }
        }
    }
}
