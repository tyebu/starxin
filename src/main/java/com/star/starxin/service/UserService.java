package com.star.starxin.service;

import com.star.starxin.netty.ChatMsg;
import com.star.starxin.pojo.Users;
import com.star.starxin.pojo.vo.FriendRequestVO;
import com.star.starxin.pojo.vo.MyFriendsVO;

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

    void deleteFriendRequest(String sendUserId, String acceptUserId);

    void passFriendRequest(String sendUserId, String acceptUserId);

    List<MyFriendsVO> queryMyFriends(String userId) throws Exception;

    /**
     * 保存聊天消息到数据库
     * @param chatMsg
     * @return
     * @throws Exception
     */
    String saveMsg(ChatMsg chatMsg) throws Exception;

    /**
     * 消息批量签收
     * @param msgIdList
     */
    void updateMsgListSigned(List<String> msgIdList);
}
