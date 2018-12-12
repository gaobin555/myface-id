package com.orbbec.utils;

import com.orbbec.base.BaseApplication;
import com.orbbec.constant.Constant;
import com.orbbec.model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 获取本地数据库中数据
 * create by qyg on 2018/10/15.
 */
public class UserDataUtil {

    private static Map<Integer, User> userMap = new HashMap<>();
    private static ArrayList<User> userList = new ArrayList<>();

    /**
     * 根据personId获取User对象
     *
     * @param id
     * @return
     */
    public static User getUserById(String id) {
        DataSource dataSource = new DataSource(BaseApplication.getContext());
        return dataSource.getUserByPersonId(id);
    }

    /**
     * 获取数据库中注册用户数量
     *
     * @return
     */
    public static int getUserCount() {
        DataSource dataSource = new DataSource(BaseApplication.getContext());
        ArrayList<User> allUser = dataSource.getAllUser();
        if (null == allUser || allUser.size() == 0) {
            return 0;
        } else {
            return allUser.size();
        }
    }

    /**
     * 清理数据表
     */
    public static void clearDb() {
        DataSource dataSource = new DataSource(BaseApplication.getContext());
        List<User> userList = dataSource.getAllUser();
        for (int i = 0; i < userList.size(); i++) {
            String imgPath = Constant.ImagePath + userList.get(i).getPersonId() + ".jpg";
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                imgFile.delete();
            }
        }
        userMap.clear();
        dataSource.clearTable();
    }

    /**
     * 更新数据源
     *
     * @return
     */
    public static ArrayList<User> updateDataSource() {
        DataSource dataSource = new DataSource(BaseApplication.getContext());
        userMap.clear();
        userList.clear();
        userList = dataSource.getAllUser();
        File file = new File(Constant.ImagePath);
        file.mkdirs();
        for (int i = 0; i < userList.size(); i++) {
            String imgPath = Constant.ImagePath + userList.get(i).getPersonId() + ".jpg";
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                userList.get(i).setHead(imgPath);
            }
            userMap.put(Integer.valueOf(userList.get(i).getPersonId()), userList.get(i));
        }
        return userList;
    }

    /**
     * 重载更新数据源方法，返回usermap对象
     *
     * @param needUserMap
     * @return
     */
    public static Map<Integer, User> updateDataSource(boolean needUserMap) {

        long time = System.currentTimeMillis();
        DataSource dataSource = new DataSource(BaseApplication.getContext());
        userMap.clear();
        userList.clear();
        userList = dataSource.getAllUser();
        File file = new File(Constant.ImagePath);
        file.mkdirs();
        for (int i = 0; i < userList.size(); i++) {
            String imgPath = Constant.ImagePath + userList.get(i).getPersonId() + ".jpg";
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                userList.get(i).setHead(imgPath);
            }
            userMap.put(Integer.valueOf(userList.get(i).getPersonId()), userList.get(i));
        }
        return userMap;
    }

    /**
     * 根据personId获取注册名称
     *
     * @param personId
     * @return
     */
    public static String getNameFromPersonId(int personId) {
        if (personId > 0 && userMap.containsKey(personId)) {
            User user = userMap.get(personId);
            return user.getName();
        }
        return "";
    }
}
