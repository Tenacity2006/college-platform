package com.kevin.Utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {
    // 1.秘钥
    private static final String SECRET_KEY = "dGhpcy1pcy1hLXZlcnktbG9uZy1hbmQtc2VjdXJlLXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLTIwMjQ=";
    // 2.过期时间（24h）
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;
    /**
     * 生成 Token
     * @param id 用户ID
     * @param role 用户角色
     * @return JWT 字符串
     */
    public static String generateToken(Integer id, Integer role,Integer status) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        claims.put("status", status);
        return Jwts.builder()
                .setClaims(claims)           // 设置自定义数据
                .setSubject("user-" + id)    // 主题
                .setIssuedAt(new Date())     // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 签名算法和密钥
                .compact();
    }
    /**
     * 解析 Token
     * @param token JWT 字符串
     * @return Claims 对象 (包含 id, role 等信息)
     * @throws Exception 如果 Token 过期或无效，会抛出异常
     */
    public static Claims parseToken(String token) throws Exception {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)   // 设置密钥进行验证
                .parseClaimsJws(token)       // 解析并验证签名
                .getBody();                  // 获取负载数据
    }

    //从Token中获取用户ID
    public static Integer getUserIdFromToken(String token) throws Exception {
        Claims claims = parseToken(token);
        // 注意：JWT 存的是 Object，需要强转
        return ((Number) claims.get("id")).intValue();
    }
    //从 Token中获取用户角色
    public static Integer getRoleFromToken(String token) throws Exception {
        Claims claims = parseToken(token);
        return ((Number) claims.get("role")).intValue();
    }
    //从 Token中获取用户状态
    public static Integer getStatusFromToken(String token) throws Exception {
        Claims claims = parseToken(token);
        return ((Number) claims.get("status")).intValue();
    }
}


