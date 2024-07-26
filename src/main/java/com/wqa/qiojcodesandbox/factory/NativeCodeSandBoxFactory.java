package com.wqa.qiojcodesandbox.factory;

import com.wqa.qiojcodesandbox.c.CCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.c.CNativeCodeSandBox;
import com.wqa.qiojcodesandbox.cpp.CppCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.cpp.CppNativeCodeSandBox;
import com.wqa.qiojcodesandbox.java.JavaCodeSandBoxTemplate;
import com.wqa.qiojcodesandbox.java.JavaNativeCodeSandBox;

public class NativeCodeSandBoxFactory implements CodeSandBoxFactory{
    @Override
    public CCodeSandboxTemplate createCCodeSandBox() {
        return new CNativeCodeSandBox();
    }

    @Override
    public JavaCodeSandBoxTemplate createJavaCodeSandBox() {
        return new JavaNativeCodeSandBox();
    }

    @Override
    public CppCodeSandboxTemplate createCppCodeSandBox() {
        return new CppNativeCodeSandBox();
    }
}
