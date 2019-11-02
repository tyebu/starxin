package com.star.starxin.controller.user;

import com.alibaba.fastjson.JSON;
import com.star.starxin.enums.OperatorFriendRequestTypeEnum;
import com.star.starxin.enums.SearchFriendsStatusEnum;
import com.star.starxin.pojo.Users;
import com.star.starxin.pojo.bo.UsersBO;
import com.star.starxin.pojo.vo.FriendRequestVO;
import com.star.starxin.pojo.vo.MyFriendsVO;
import com.star.starxin.pojo.vo.UsersVO;
import com.star.starxin.service.UserService;
import com.star.starxin.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Star
 * @createdDate
 * @description
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private FastDFSClient fastDFSClient;
    @RequestMapping("/registeOrLogin")
    public StarJSONResult registOrLogin(@RequestBody Users users) {
        Users userResult = null;
        System.out.println(JSON.toJSONString(users));
        // 0. 判断用户名和密码不能为空
        if (StringUtils.isBlank(users.getUsername())
                || StringUtils.isBlank(users.getPassword())) {
            return StarJSONResult.errorMsg("用户名或密码不能为空...");
        }

        // 1. 判断用户名是否存在，如果存在就登录，如果不存在则注册
        boolean usernameIsExist = userService.selectUsername(users.getUsername());
        // 若存在就登陆
        if(usernameIsExist) {
            try {
                userResult = userService.loginUser(users.getUsername(), MD5Utils.getMD5Str(users.getPassword()));
            } catch (Exception e) {
                e.printStackTrace();
                return StarJSONResult.errorMsg("系统异常");
            }
            if(ObjectUtils.isEmpty(userResult)) {
                return StarJSONResult.errorMsg("用户名或密码错误");
            }
        } else {
            // 若不存在就注册
            users.setNickname(users.getUsername());
            users.setFaceImage("");
            users.setFaceImageBig("");
            try {
                users.setPassword(MD5Utils.getMD5Str(users.getPassword()));
            } catch (Exception e) {
                e.printStackTrace();
                return StarJSONResult.errorMsg("系统异常");
            }
            try {
                userResult = userService.saveUser(users);
            } catch (Exception e) {
                e.printStackTrace();
                return StarJSONResult.errorMsg("系统异常");
            }
        }
        UsersVO usersVO = new UsersVO();
        System.out.println(JSON.toJSONString(userResult));
        BeanUtils.copyProperties(userResult, usersVO);
        return StarJSONResult.ok(usersVO);
    }

    @RequestMapping("/uploadFaceBaseImg")
    public StarJSONResult uploadFaceBaseImg(@RequestBody UsersBO userBO) {
        // 获取前台传来的base64字符串
        String faceData = userBO.getFaceData();
        String imgName = userBO.getUserId();
        String imgPath  ="E:\\" + imgName + "userBase64.png";
        Users userResult = new Users();
        try {
            FileUtils.base64ToFile(imgPath, faceData);
            MultipartFile multipartFile = FileUtils.fileToMultipart(imgPath);
            String uploadPath = fastDFSClient.uploadBase64(multipartFile);
            System.out.println(uploadPath);

            //获取缩略图的url
            String[] split = uploadPath.split("\\.");
            String thump = "_80x80.";
            String thumpPath = split[0]+thump+split[1];
            Users users = new Users();
            // 更新用户头像
            users.setId(userBO.getUserId());
            users.setFaceImageBig(uploadPath);
            users.setFaceImage(thumpPath);

            userResult = userService.updateUserInfo(users);

        } catch (Exception e) {
            e.printStackTrace();
            return StarJSONResult.errorMsg("上传头像失败...");
        }
        if(ObjectUtils.isEmpty(userResult)) {
            return StarJSONResult.errorMsg("上传失败");
        }
        return StarJSONResult.ok(userResult);
    }
    @RequestMapping("/updateNickname")
    public StarJSONResult updateNickname(@RequestBody UsersBO usersBO) {
        Users users = new Users();
        users.setId(usersBO.getUserId());
        users.setNickname(usersBO.getNickname());
        try {
            Users users1 = userService.updateUserInfo(users);
            return StarJSONResult.ok(users1);
        } catch (Exception e) {
            e.printStackTrace();
            return StarJSONResult.errorMsg("修改失败");
        }
    }
    @RequestMapping("/searchFriends")
    public StarJSONResult searchFriend(String userId, String friendUsername) {
        // 0. 判断 myUserId friendUsername 不能为空
        if (StringUtils.isBlank(userId)
                || StringUtils.isBlank(friendUsername)) {
            return StarJSONResult.errorMsg("");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.preconditionSearchFriends(userId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user, userVO);
            return StarJSONResult.ok(userVO);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return StarJSONResult.errorMsg(errorMsg);
        }
    }
    /**
     * @Description: 发送添加好友的请求
     */
    @PostMapping("/addFriendRequest")
    public StarJSONResult addFriendRequest(String myUserId, String friendUsername)
            throws Exception {

        // 0. 判断 myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return StarJSONResult.errorMsg("");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return StarJSONResult.errorMsg(errorMsg);
        }

        return StarJSONResult.ok();
    }
    /**
     * @Description: 查询好友请求列表
     */
    @PostMapping("/queryFriendRequest")
    public StarJSONResult queryFriendRequest(String userId) {
        List<FriendRequestVO> friendRequestVVOList = userService.queryFriendRequestList(userId);
        return StarJSONResult.ok(friendRequestVVOList);
    }
    /**
     * @Description: 接受方 通过或者忽略朋友请求
     */
    @PostMapping("/operFriendRequest")
    public StarJSONResult operFriendRequest(String acceptUserId, String sendUserId,
                                             Integer operType) {

        // 0. acceptUserId sendUserId operType 判断不能为空
        if (StringUtils.isBlank(acceptUserId)
                || StringUtils.isBlank(sendUserId)
                || operType == null) {
            return StarJSONResult.errorMsg("");
        }

        // 1. 如果operType 没有对应的枚举值，则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return StarJSONResult.errorMsg("");
        }

        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            // 2. 判断如果忽略好友请求，则直接删除好友请求的数据库表记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type) {
            // 3. 判断如果是通过好友请求，则互相增加好友记录到数据库对应的表
            //	   然后删除好友请求的数据库表记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        List<MyFriendsVO> myFirends = new ArrayList<>();
        // 4. 数据库查询好友列表
        try {
            myFirends = userService.queryMyFriends(acceptUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return StarJSONResult.ok(myFirends);
    }
    @PostMapping("/getFriendsList")
    public StarJSONResult getFriendsList(String userId) {
        try {
            List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
            return StarJSONResult.ok(myFriends);
        } catch (Exception e) {
            e.printStackTrace();
            return StarJSONResult.errorMsg(ResponseMessage.ERROR);
        }
    }
}
