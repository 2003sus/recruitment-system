package com.example.recruitmentbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.recruitmentbackend.entity.HrUser;
import com.example.recruitmentbackend.mapper.HrUserMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final HrUserMapper mapper;

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public AuthService(HrUserMapper mapper) {
        this.mapper = mapper;
    }

    public String login(String username, String password) {
        HrUser user = mapper.selectOne(new QueryWrapper<HrUser>()
                .eq("username", username)
                .eq("password", password));
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = UUID.randomUUID().toString();
        tokens.put(token, username);
        return token;
    }

    public boolean checkToken(String token) {
        return token != null && tokens.containsKey(token);
    }
}