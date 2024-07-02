package com.wqa.qioj.judge.codesandbox;

import com.wqa.qioj.judge.codesandbox.impl.ExampleCodeSandBoxImpl;
import com.wqa.qioj.judge.codesandbox.impl.RemoteCodeSandBoxImpl;
import com.wqa.qioj.judge.codesandbox.impl.ThirdPartyCodeSandBoxImpl;

/**
 * 代码沙箱静态工厂（根据字符串参数创建指定沙箱实例）
 */
public class CodeSandBoxFactory {

    /**
     * 创建代码沙箱实例
     *
     * @param type 沙箱类型
     * @return
     */
    public static CodeSandBox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandBoxImpl();
            case "remote":
                return new RemoteCodeSandBoxImpl();
            case "thirdParty":
                return new ThirdPartyCodeSandBoxImpl();
            default:
                return new ExampleCodeSandBoxImpl();
        }
    }
}
