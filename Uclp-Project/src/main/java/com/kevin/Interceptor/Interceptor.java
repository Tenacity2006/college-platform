package com.kevin.Interceptor;

import com.kevin.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.kevin.Utils.JWTUtil;

import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
public class Interceptor implements HandlerInterceptor {

    // 定义白名单（双重保险）
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/login",
            "/register",
            "/css/**",
            "/js/**",
            "/images/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        //1.放行白名单路径
        for (String path : WHITE_LIST) {
            if (uri.startsWith(path)) {
                return true;
            }
        }
        //2.获取 Token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            log.warn("缺少Token| IP:{} | 路径:{}", ip, uri);
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1,\"msg\":\"未登录或Token缺失\",\"data\":null}");
            return false;
        }

        //3.解析Token
        try {
            Integer userId = JWTUtil.getUserIdFromToken(token);
            Integer userRole = JWTUtil.getRoleFromToken(token);
            Integer userStatus = JWTUtil.getStatusFromToken(token);

        //4.封禁判断
            if (userStatus != null && userStatus == 1) {
                log.warn("账号被封禁 | ID:{} | 路径:{}", userId, uri);
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":1,\"msg\":\"账号已被封禁\",\"data\":null}");
                return false;
            }

        //5.权限判断
            if (uri.contains("/ban/")) {
                if (userRole == null || userRole != 1) {
                    log.warn("权限不足 | ID:{} | 角色:{} | 路径:{}", userId, userRole, uri);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":1,\"msg\":\"权限不足\",\"data\":null}");
                    return false;
                }
            }

        // 6.构造User对象放入Request(方便Controller层使用)
            User currentUser = new User();
            currentUser.setId(userId);
            currentUser.setRole(userRole);
            currentUser.setStatus(userStatus);
            request.setAttribute("currentUser", currentUser);
            log.info("放行| ID:{} | 角色:{} | 路径:{}", userId, userRole, uri);
            return true;
        } catch (Exception e) {
            log.warn("Token无效| IP:{} | 错误:{}", ip, e.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1,\"msg\":\"Token无效\",\"data\":null}");
            return false;
        }
    }
}

