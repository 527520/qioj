package com.wqa.qiojcodesandbox.factory;

import com.wqa.qiojcodesandbox.c.CCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.c.CDockerCodeSandBox;
import com.wqa.qiojcodesandbox.cpp.CppCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.cpp.CppDockerCodeSandBox;
import com.wqa.qiojcodesandbox.java.JavaCodeSandBoxTemplate;
import com.wqa.qiojcodesandbox.java.JavaDockerCodeSandBox;

public class DockerCodeSandBoxFactory implements CodeSandBoxFactory{
    @Override
    public CCodeSandboxTemplate createCCodeSandBox() {
        return new CDockerCodeSandBox();
    }

    @Override
    public JavaCodeSandBoxTemplate createJavaCodeSandBox() {
        return new JavaDockerCodeSandBox();
    }

    @Override
    public CppCodeSandboxTemplate createCppCodeSandBox() {
        return new CppDockerCodeSandBox();
    }
}
