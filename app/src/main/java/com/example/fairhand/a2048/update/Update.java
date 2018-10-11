package com.example.fairhand.a2048.update;

/**
 * Created by FairHand on 2018/10/10.<br />
 * 更新实体类
 */
public class Update {
    
    // 返回JSON样例数据
    //    "new_version": "0.8.20031",
    //    "new_version_code": "1.1",
    //    "apk_file_url": "https://raw.githubusercontent.com/kylechandev/2048TR/master/2048TR.apk",
    //    "update_log": "1，测试。\r\n2，测试。\r\n3，测试。\r\n4，测试。\r\n5，测试。",
    //    "target_size": "3.5M",
    //    "constraint": false
    
    /**
     * 新版本信息
     */
    private String new_version;
    
    /**
     * 新版本号
     */
    private String new_version_code;
    
    /**
     * APK下载地址
     */
    private String apk_file_url;
    
    /**
     * 更新日志
     */
    private String update_log;
    
    /**
     * APK大小
     */
    private String target_size;
    
    /**
     * 是否强制更新
     */
    private boolean constraint;
    
    public String getNew_version() {
        return new_version;
    }
    
    public String getNew_version_code() {
        return new_version_code;
    }
    
    public String getApk_file_url() {
        return apk_file_url;
    }
    
    public String getUpdate_log() {
        return update_log;
    }
    
    public String getTarget_size() {
        return target_size;
    }
    
    public boolean getConstraint() {
        return constraint;
    }
}
