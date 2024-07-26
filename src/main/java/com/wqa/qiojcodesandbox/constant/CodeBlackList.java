package com.wqa.qiojcodesandbox.constant;

import java.util.Arrays;
import java.util.List;

public enum CodeBlackList {

    /**
     * Java代码黑名单
     * 黑名单检测通常用于辅助安全策略，而不是作为唯一的安全手段
     */
    JAVA_SENSITIVE_WORD_LIST(Arrays.asList(
            // 文件操作相关
            "Files", "File", "FileInputStream", "FileOutputStream", "RandomAccessFile", "FileReader", "FileWriter", "FileChannel", "FileLock", "Path", "Paths", "File.createTempFile", "File.createTempDirectory", "ZipInputStream", "ZipOutputStream",

            // 网络相关
            "Socket", "ServerSocket", "DatagramSocket", "InetAddress", "URL", "URLConnection", "HttpURLConnection", "SocketChannel", "ServerSocketChannel", "DatagramChannel", "SocketPermission", "ServerSocketPermission",

            // 系统命令执行相关
            "exec", "Runtime.getRuntime().exec", "ProcessBuilder", "SecurityManager", "System.exit", "Runtime.getRuntime().halt", "SecurityManager.checkExec",

            // 反射相关
            "Class.forName", "Method.invoke", "sun.reflect.", "java.lang.reflect.", "Unsafe", "sun.misc.Unsafe", "sun.reflect.Unsafe", "Proxy",

            // 数据库相关
            "Statement", "PreparedStatement", "CallableStatement", "DataSource", "Connection", "ResultSet", "Hibernate", "JPA", // 防止使用 ORM 框架执行不安全的数据库操作
            "createStatement", "prepareStatement", "prepareCall",

            // 不安全的操作
            "Unsafe", "sun.misc.Unsafe", "sun.reflect.Unsafe",

            // 加密解密相关
            "Cipher", "MessageDigest", "KeyGenerator", "KeyPairGenerator", "SecretKeyFactory", "KeyStore", "SecureRandom", "java.security.",

            // 序列化相关
            "ObjectInputStream", "ObjectOutputStream", "Serializable", "Externalizable", "readObject", "writeObject",

            // 线程相关
            "Thread", "Runnable", "Executor", "ExecutorService", "ThreadPoolExecutor", "ThreadGroup", "ThreadLocal", "Thread.sleep", "Thread.yield", "Thread.stop", "Thread.suspend", "Thread.resume", "java.util.concurrent.",

            // 安全管理器相关
            "SecurityManager",

            // 其他可能导致安全问题的操作
            "System.load", "System.loadLibrary", // 防止加载本地库
            "JNI", "Java Native Interface", // 防止使用 JNI 调用本地代码
            "Unsafe.allocateMemory", "Unsafe.freeMemory", // 直接内存操作
            "System.getProperties", "System.setProperty", // 系统属性操作
            "System.getenv", // 获取环境变量
            "System.console", // 控制台访问
            "Runtime.addShutdownHook", // 添加关闭钩子
            "Runtime.load", "Runtime.loadLibrary" // 加载本地库
    )),

    /**
     * C语言相关敏感词
     */
    C_SENSITIVE_WORD_LIST(Arrays.asList(
            // 文件操作相关
            "fopen", "fclose", "fread", "fwrite", "fscanf", "fprintf", "fseek", "ftell", "rewind", "remove", "rename", "tmpfile", "mkstemp",

            // 网络相关
            "socket", "bind", "connect", "listen", "accept", "send", "recv", "setsockopt", "getsockopt", "inet_addr", "htons", "htonl", "ntohs", "ntohl",

            // 系统命令执行相关
            "system", "execvp", "execv", "execl", "execlp", "popen", "pclose",

            // 数据库相关
            // C语言中通常通过库如 MySQL C API, SQLite等实现数据库操作，以下为一些示例函数
            "mysql_query", "sqlite3_exec",

            // 加密解密相关
            // C语言使用 libcrypto (OpenSSL)等库进行加密解密操作
            "EVP_EncryptInit", "EVP_DecryptInit",

            // 线程相关
            "pthread_create", "pthread_join", "pthread_detach", "pthread_exit",

            // 其他可能导致安全问题的操作
            "dlopen", "dlsym", // 动态加载库
            "getenv", // 环境变量操作
            "atexit", // 添加退出时执行的函数
            "mmap", "munmap", // 直接内存映射操作
            "system" // 执行外部命令
    ));

    private final List<String> sensitiveWords;

    CodeBlackList(List<String> sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }

    public List<String> getSensitiveWords() {
        return sensitiveWords;
    }
}
