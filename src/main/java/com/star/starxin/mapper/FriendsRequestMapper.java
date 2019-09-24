package com.star.starxin.mapper;

import com.star.starxin.pojo.FriendsRequest;
import com.star.starxin.pojo.vo.FriendRequestVO;
import com.star.starxin.utils.MyMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface FriendsRequestMapper extends MyMapper<FriendsRequest> {
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
}