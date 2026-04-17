package com.wqa.qiojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wqa.qiojcodesandbox.c.CDockerCodeSandBox;
import com.wqa.qiojcodesandbox.c.CNativeCodeSandBox;
import com.wqa.qiojcodesandbox.cpp.CppDockerCodeSandBox;
import com.wqa.qiojcodesandbox.cpp.CppNativeCodeSandBox;
import com.wqa.qiojcodesandbox.java.JavaDockerCodeSandBox;
import com.wqa.qiojcodesandbox.java.JavaNativeCodeSandBox;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;
import com.wqa.qiojcodesandbox.python.PythonDockerCodeSandBox;
import com.wqa.qiojcodesandbox.python.PythonNativeCodeSandBox;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 代码沙箱测试类
 */
public class CodeSandboxTest {

    /**
     * 测试 Java Native 沙箱
     */
    @Test
    public void testJavaNativeCodeSandbox() {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("java");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("Java Native 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 Java Docker 沙箱
     */
    @Test
    public void testJavaDockerCodeSandbox() {
        JavaDockerCodeSandBox javaDockerCodeSandBox = new JavaDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("java");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("Java Docker 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 C Native 沙箱
     */
    @Test
    public void testCNativeCodeSandbox() {
        CNativeCodeSandBox cNativeCodeSandBox = new CNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("c");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.c", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = cNativeCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("C Native 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 C Docker 沙箱
     */
    @Test
    public void testCDockerCodeSandbox() {
        CDockerCodeSandBox cDockerCodeSandBox = new CDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("c");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.c", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = cDockerCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("C Docker 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 C++ Native 沙箱
     */
    @Test
    public void testCppNativeCodeSandbox() {
        CppNativeCodeSandBox cppNativeCodeSandBox = new CppNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("cpp");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.cpp", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = cppNativeCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("C++ Native 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 C++ Docker 沙箱
     */
    @Test
    public void testCppDockerCodeSandbox() {
        CppDockerCodeSandBox cppDockerCodeSandBox = new CppDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("cpp");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.cpp", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = cppDockerCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("C++ Docker 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 Python Native 沙箱
     */
    @Test
    public void testPythonNativeCodeSandbox() {
        PythonNativeCodeSandBox pythonNativeCodeSandBox = new PythonNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("python3");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.py", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = pythonNativeCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("Python Native 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 Python Docker 沙箱
     */
    @Test
    public void testPythonDockerCodeSandbox() {
        PythonDockerCodeSandBox pythonDockerCodeSandBox = new PythonDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("python3");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.py", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = pythonDockerCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("Python Docker 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }

    /**
     * 测试 Python2 Docker 沙箱
     */
    @Test
    public void testPython2DockerCodeSandbox() {
        PythonDockerCodeSandBox pythonDockerCodeSandBox = new PythonDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("python2");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.py", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);

        ExecuteCodeResponse executeCodeResponse = pythonDockerCodeSandBox.executeCode(executeCodeRequest);

        System.out.println("Python2 Docker 沙箱测试结果：");
        System.out.println("状态：" + executeCodeResponse.getStatus());
        System.out.println("输出：" + executeCodeResponse.getOutputList());
        System.out.println("执行时间：" + executeCodeResponse.getJudgeInfo().getTime() + "ms");
        System.out.println("内存占用：" + executeCodeResponse.getJudgeInfo().getMemory() + " bytes");
        System.out.println("消息：" + executeCodeResponse.getMessage());

        assert executeCodeResponse.getStatus() != null;
        assert executeCodeResponse.getOutputList() != null;
    }
}
