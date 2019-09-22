package com.star.starxin.service.impl;

import com.star.starxin.mapper.UsersMapper;
import com.star.starxin.pojo.Users;
import com.star.starxin.service.UserService;
import com.star.starxin.utils.FastDFSClient;
import com.star.starxin.utils.FileUtils;
import com.star.starxin.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;
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

    private Users selectUserById(String userId) {
        Users users = usersMapper.selectByPrimaryKey(userId);
        return users;
    }
}
