package com.wqa.qiojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.wqa.qiojcodesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProcessUtils {

    /**
     * 执行进程并获取信息
     *
     * @param process
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process process, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        long initialMemory = getUsedMemory();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行获取退出码
            int exitValue = process.waitFor();// 退出码
            stopWatch.stop();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 分批获取进程的输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                String compileOutputLine;
                // 逐行读取
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));
            } else { // 异常退出
                System.out.println(opName + "失败：" + exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                List<String> compileOutputStrList = new ArrayList<>();
                String compileOutputLine;
                // 逐行读取
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(compileOutputStrList, "\n"));

                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                List<String> errorCompileOutputStrList = new ArrayList<>();                String errorCompileOutputLine;
                // 逐行读取
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorCompileOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorCompileOutputStrList, "\n"));
            }
            long finalMemory = getUsedMemory();
            // 计算内存使用量，单位字节，转换成kb需要除以1024
            long memoryUsage = finalMemory - initialMemory;
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            executeMessage.setMemory(memoryUsage);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息
     *
     * @param runProcess
     * @param args
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args) throws IOException {
        // 向控制台输入程序
        ExecuteMessage executeMessage = new ExecuteMessage();
        //记录程序还未执行的内存使用量
        long initialMemory = getUsedMemory();
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(runProcess.getOutputStream())) {
            String[] arguments = args.split(" ");
            for (String arg : arguments) {
                outputStreamWriter.write(arg);
                outputStreamWriter.write("\n");
            }
            // 相当于按了回车，执行输入的发送
            outputStreamWriter.flush();
            //记录程序开始执行时间
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            int exitCode = runProcess.waitFor();
            stopWatch.stop();
            executeMessage.setExitValue(exitCode);
            if (exitCode == 0) {
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, '\n'));
            } else {
                //异常退出
                System.out.println("失败：错误码：" + exitCode);
                //运行正常输出流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(), StandardCharsets.UTF_8));
                List<String> outputStrList = new ArrayList<>();
                //进行逐行读取
                String complieOutLine;
                while ((complieOutLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(complieOutLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(outputStrList, '\n'));
                //分批获取错误输出
                BufferedReader bufferedReaderError = new BufferedReader(new InputStreamReader(runProcess.getErrorStream(), StandardCharsets.UTF_8));
                //逐行读取
                List<String> errorOutputStrList = new ArrayList<>();
                String complieOutLineError;
                while ((complieOutLineError = bufferedReaderError.readLine()) != null) {
                    errorOutputStrList.add(complieOutLineError);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorOutputStrList, '\n'));

            }
            long finalMemory = getUsedMemory();
            // 计算内存使用量，单位字节，转换成kb需要除以1024
            long memoryUsage = finalMemory - initialMemory;
            executeMessage.setTime(stopWatch.getTotalTimeMillis());
            executeMessage.setMemory(memoryUsage);
        } catch (Exception e) {
            // 使用日志框架记录异常
            log.error("执行交互式进程出错", e);
        } finally {
            // 记得资源的释放，否则会卡死
            runProcess.destroy();
        }
        return executeMessage;
    }

    public static ExecuteMessage runInteractProcessAndGetMessageAnother(Process runProcess, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try (OutputStream outputStream = runProcess.getOutputStream(); OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream); InputStream inputStream = runProcess.getInputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            // 将参数写入进程的输出流
            String[] arguments = args.split(" ");
            for (String arg : arguments) {
                outputStreamWriter.write(arg);
                outputStreamWriter.write("\n");
            }
            outputStreamWriter.flush();

            // 从进程的输入流读取输出
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine).append("\n");
            }
            executeMessage.setMessage(compileOutputStringBuilder.toString());

        } catch (IOException e) {
            // 在此处处理潜在的 I/O 错误
            e.printStackTrace();
            executeMessage.setErrorMessage(e.getMessage());
        } finally {
            // 确保进程已销毁
            runProcess.destroy();
        }
        return executeMessage;
    }

    /**
     * 获取当前已使用的内存量
     * 单位是byte
     * @return
     */
    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
