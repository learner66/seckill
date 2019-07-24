package com.zhang.spike.redis;

public interface KeyPrefix {
    public int expireSeconds();
    public  String  getPrefix();
}
