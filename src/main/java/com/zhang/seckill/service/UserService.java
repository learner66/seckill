package com.zhang.seckill.service;

import com.zhang.seckill.dao.UserDao;
import com.zhang.seckill.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    UserDao userDao;
    public User getById(int id){
        return userDao.getById(id);
    }

    public int Insert(User user){
        return userDao.insert(user);
    }
}
