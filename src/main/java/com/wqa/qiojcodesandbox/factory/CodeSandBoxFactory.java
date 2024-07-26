package com.wqa.qiojcodesandbox.factory;

import com.wqa.qiojcodesandbox.c.CCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.cpp.CppCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.java.JavaCodeSandBoxTemplate;

public interface CodeSandBoxFactory {
    CCodeSandboxTemplate createCCodeSandBox();
    JavaCodeSandBoxTemplate createJavaCodeSandBox();

    CppCodeSandboxTemplate createCppCodeSandBox();
}
