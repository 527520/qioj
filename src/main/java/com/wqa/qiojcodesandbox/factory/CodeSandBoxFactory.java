package com.wqa.qiojcodesandbox.factory;

import com.wqa.qiojcodesandbox.c.CCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.cpp.CppCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.java.JavaCodeSandBoxTemplate;
import com.wqa.qiojcodesandbox.python.PythonCodeSandboxTemplate;

public interface CodeSandBoxFactory {
    CCodeSandboxTemplate createCCodeSandBox();
    JavaCodeSandBoxTemplate createJavaCodeSandBox();
    CppCodeSandboxTemplate createCppCodeSandBox();
    PythonCodeSandboxTemplate createPythonCodeSandBox();
}
