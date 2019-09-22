package com.star.starxin.service;

import com.star.starxin.pojo.Users;

/**
 * @author Star
 * @createdDate
 * @description 用户相关业务层
 */
public interface UserService {
    boolean selectUsername(String username);

    Users loginUser(String username, String password);

    Users saveUser(Users users) throws Exception;

    Users updateUserInfo(Users users) throws  Exception;
}
