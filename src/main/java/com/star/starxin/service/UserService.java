package com.star.starxin.service;

import com.star.starxin.pojo.Users;
import com.star.starxin.pojo.vo.FriendRequestVO;

import java.util.List;

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

    Integer preconditionSearchFriends(String userId, String friendUsername);

    Users queryUserInfoByUsername(String friendUsername);

    void sendFriendRequest(String myUserId, String friendUsername);

    List<FriendRequestVO> queryFriendRequestList(String acceptName);
}
