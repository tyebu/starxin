package com.star.starxin.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.star.starxin.enums.MsgActionEnum;
import com.star.starxin.enums.MsgSignFlagEnum;
import com.star.starxin.enums.SearchFriendsStatusEnum;
import com.star.starxin.mapper.ChatMsgMapper;
import com.star.starxin.mapper.FriendsRequestMapper;
import com.star.starxin.mapper.MyFriendsMapper;
import com.star.starxin.mapper.UsersMapper;
import com.star.starxin.netty.ChatMsg;
import com.star.starxin.netty.DataContent;
import com.star.starxin.netty.UserChannelRel;
import com.star.starxin.pojo.FriendsRequest;
import com.star.starxin.pojo.MyFriends;
import com.star.starxin.pojo.Users;
import com.star.starxin.pojo.vo.FriendRequestVO;
import com.star.starxin.pojo.vo.MyFriendsVO;
import com.star.starxin.service.UserService;
import com.star.starxin.utils.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.Date;
import java.util.List;

/**
 * @author Star
 * @createdDate
 * @description
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private QRCodeUtils qrCodeUtils;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private FastDFSClient fastDFSClient;
    @Autowired
    private MyFriendsMapper myFriendsMapper;
    @Autowired
    private FriendsRequestMapper friendsRequestMapper;
    @Autowired
    private ChatMsgMapper chatMsgMapper;
    @Override
    public boolean selectUsername(String username) {
        Users users = new Users();
        users.setUsername(username);
        Users result = usersMapper.selectOne(users);
        if(!ObjectUtils.isEmpty(result)) {
            return true;
        }
        return false;
    }
//    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users loginUser(String username, String password) {
        Example example = new Example(Users.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", password);
        Users users = usersMapper.selectOneByExample(example);
        return users;
    }
    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @Override
    public Users saveUser(Users users) throws Exception {
        // 生成用户主键
        String userId = sid.nextShort();
        //生成唯一二维码
        String qrCode = "G://test" + userId + "qrCode.png";
        qrCodeUtils.createQRCode(qrCode, "starxin_qrcode:" + users.getUsername());
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCode);
        String s = fastDFSClient.uploadQRCode(qrcodeFile);
        users.setQrcode(s);
        users.setId(userId);
        int insert = usersMapper.insert(users);
        if(insert > 0) {
            return users;
        }
        return null;
    }
    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    @Override
    public Users updateUserInfo(Users users) throws Exception {
        int rows = usersMapper.updateByPrimaryKeySelective(users);
        if(rows > 0) {
            return selectUserById(users.getId());
        } else {
            return null;
        }
    }

    @Override
    public Integer preconditionSearchFriends(String userId, String friendUsername) {
        Users user = queryUserInfoByUsername(friendUsername);

        // 1. 搜索的用户如果不存在，返回[无此用户]
        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }

        // 2. 搜索账号是你自己，返回[不能添加自己]
        if (user.getId().equals(userId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        // 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Example mfe = new Example(MyFriends.class);
        Criteria mfc = mfe.createCriteria();
        mfc.andEqualTo("myUserId", userId);
        mfc.andEqualTo("myFriendUserId", user.getId());
        MyFriends myFriendsRel = myFriendsMapper.selectOneByExample(mfe);
        if (myFriendsRel != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public Users queryUserInfoByUsername(String friendUsername) {
        Example example = new Example(Users.class);
        Criteria ct = example.createCriteria();
        ct.andEqualTo("username", friendUsername);
        return usersMapper.selectOneByExample(example);
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {
        // 根据用户名把朋友信息查询出来
        Users friend = queryUserInfoByUsername(friendUsername);
        // 1. 查询发送好友请求记录表
        Example fre = new Example(FriendsRequest.class);
        Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", myUserId);
        frc.andEqualTo("acceptUserId", friend.getId());
        FriendsRequest friendRequest = friendsRequestMapper.selectOneByExample(fre);
        if (friendRequest == null) {
            // 2. 如果不是你的好友，并且好友记录没有添加，则新增好友请求记录
            String requestId = sid.nextShort();

            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }
    }

    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        List<FriendRequestVO> friendRequestList = friendsRequestMapper.queryFriendRequestList(acceptUserId);
        return friendRequestList;
    }

    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example fre = new Example(FriendsRequest.class);
        Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", sendUserId);
        frc.andEqualTo("acceptUserId", acceptUserId);
        friendsRequestMapper.deleteByExample(fre);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId, sendUserId);
        deleteFriendRequest(sendUserId, acceptUserId);
        Channel channel = UserChannelRel.get(sendUserId);
        if(channel != null) {
            // 使用websocket主动更新通讯录列表
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(dataContent)));
        }
        /*Channel sendChannel = UserChannelRel.get(sendUserId);
        if (sendChannel != null) {
            // 使用websocket主动推送消息到请求发起者，更新他的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            sendChannel.writeAndFlush(
                    new TextWebSocketFrame(
                            JsonUtils.objectToJson(dataContent)));
        }*/
    }

    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) throws Exception{
        if(StringUtils.isEmpty(userId)) {
            throw new CommonException(ResponseMessage.ERROR);
        }
        return myFriendsMapper.queryMyFriends(userId);
    }

    /**
     * 保存聊天消息
     * @param chatMsg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveMsg(ChatMsg chatMsg) throws Exception {
        // 初始化
        com.star.starxin.pojo.ChatMsg chatMsg2 = new com.star.starxin.pojo.ChatMsg(
                sid.nextShort(),  chatMsg.getSenderId(), chatMsg.getReceiverId(), chatMsg.getMsg(),
                MsgSignFlagEnum.unsign.type, new Date()
        );
        chatMsgMapper.insert(chatMsg2);
        return chatMsg2.getId();
    }

    /**
     * 消息批量签收
     * @param msgIdList
     */
    @Override
    public void updateMsgListSigned(List<String> msgIdList) {
        chatMsgMapper.batchMsgIdListSign(msgIdList);
    }

    private void saveFriends(String sendUserId, String acceptUserId) {
        MyFriends myFriends = new MyFriends();
        String recordId = sid.nextShort();
        myFriends.setId(recordId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriends.setMyUserId(sendUserId);
        myFriendsMapper.insert(myFriends);
    }

    private Users selectUserById(String userId) {
        Users users = usersMapper.selectByPrimaryKey(userId);
        return users;
    }
}
