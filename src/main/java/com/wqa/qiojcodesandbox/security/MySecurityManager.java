package com.wqa.qiojcodesandbox.security;

import java.security.Permission;

/**
 * 安全管理器
 */
public class MySecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {

    }

    // 检测程序是否可执行文件
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec 权限异常：" + cmd);
    }

    // 检测程序是否可读文件
    @Override
    public void checkRead(String file) {
        if (file.contains("hutool")) {
            return;
        }
        throw new SecurityException("checkRead 权限异常：" + file);
    }

    // 检测程序是否可写文件
    @Override
    public void checkWrite(String file) {
        throw new SecurityException("checkWrite 权限异常：" + file);
    }

    // 检测程序是否可删除文件
    @Override
    public void checkDelete(String file) {
        throw new SecurityException("checkDelete 权限异常：" + file);
    }

    // 检测程序是否可连接网络
    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("checkConnect 权限异常：" + host + "-" + port);
    }
}
