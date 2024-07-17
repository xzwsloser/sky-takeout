package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import org.springframework.stereotype.Service;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/17 21:14
 */
@Service
public interface UserService {

    User wxLogin(UserLoginDTO userLoginDTO);

}
