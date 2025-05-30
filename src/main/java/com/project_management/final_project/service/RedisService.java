package com.project_management.final_project.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {
    void set(String key, Object value);
    void set(String key, Object value, long timeout, TimeUnit unit);
    Object get(String key);
    boolean delete(String key);
    boolean hasKey(String key);
} 