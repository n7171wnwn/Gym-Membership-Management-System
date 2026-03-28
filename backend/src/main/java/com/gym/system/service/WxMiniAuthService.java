package com.gym.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WxMiniAuthService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.wx-miniapp.app-id:}")
    private String appId;
    @Value("${app.wx-miniapp.app-secret:}")
    private String appSecret;

    public boolean configured() {
        return appId != null && !appId.isEmpty() && appSecret != null && !appSecret.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public String code2SessionOpenid(String code) {
        if (!configured()) {
            throw new RuntimeException("未配置微信小程序 app-id / app-secret");
        }
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, appSecret, code);
        try {
            String raw = restTemplate.getForObject(url, String.class);
            Map<String, Object> m = objectMapper.readValue(raw, Map.class);
            if (m.get("openid") == null) {
                throw new RuntimeException("微信接口错误：" + m.get("errmsg"));
            }
            return String.valueOf(m.get("openid"));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("微信登录失败", e);
        }
    }
}
