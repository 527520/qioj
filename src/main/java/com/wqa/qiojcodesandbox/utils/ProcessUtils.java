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
import java.util.concurrent.atomic.AtomicLong;

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

        // 用于记录峰值内存的原子变量
        AtomicLong maxMemory = new AtomicLong(0L);

        // 启动内存监控线程
        Thread memoryMonitorThread = new Thread(() -> {
            long pid = getProcessPid(process);
            if (pid > 0) {
                while (process.isAlive()) {
                    try {
                        long memory = getProcessMemory(pid);
                        if (memory > 0) {
                            maxMemory.updateAndGet(current -> Math.max(current, memory));
                        }
                        Thread.sleep(50); // 每50ms采样一次
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        memoryMonitorThread.setDaemon(true);
        memoryMonitorThread.start();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行获取退出码
            int exitValue = process.waitFor();// 退出码
            stopWatch.stop();
            executeMessage.setExitValue(exitValue);

            // 等待内存监控线程结束
            memoryMonitorThread.join(1000);

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
                List<String> errorCompileOutputStrList = new ArrayList<>();
                String errorCompileOutputLine;
                // 逐行读取
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorCompileOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorCompileOutputStrList, "\n"));
            }
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            executeMessage.setMemory(maxMemory.get());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            // 确保进程被销毁
            process.destroy();
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

        // 用于记录峰值内存的原子变量
        AtomicLong maxMemory = new AtomicLong(0L);

        // 启动内存监控线程
        Thread memoryMonitorThread = new Thread(() -> {
            long pid = getProcessPid(runProcess);
            if (pid > 0) {
                while (runProcess.isAlive()) {
                    try {
                        long memory = getProcessMemory(pid);
                        if (memory > 0) {
                            maxMemory.updateAndGet(current -> Math.max(current, memory));
                        }
                        Thread.sleep(50); // 每50ms采样一次
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        memoryMonitorThread.setDaemon(true);
        memoryMonitorThread.start();

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

            // 等待内存监控线程结束
            memoryMonitorThread.join(1000);

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
            executeMessage.setTime(stopWatch.getTotalTimeMillis());
            executeMessage.setMemory(maxMemory.get());
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
     * 获取进程的 PID
     * 兼容 Java 8 和更高版本
     *
     * @param process 进程对象
     * @return 进程PID，如果获取失败返回 -1
     */
    private static long getProcessPid(Process process) {
        try {
            // Java 9+ 提供了 process.pid() 方法
            // 为了兼容 Java 8，使用反射
            if (process.getClass().getName().equals("java.lang.ProcessImpl")) {
                try {
                    java.lang.reflect.Field pidField = process.getClass().getDeclaredField("pid");
                    pidField.setAccessible(true);
                    return pidField.getLong(process);
                } catch (Exception e) {
                    log.debug("无法通过反射获取进程PID: {}", e.getMessage());
                }
            }

            // 尝试使用 toString 方法解析 PID
            String processString = process.toString();
            // 格式通常是 "Process[pid=12345]" 或类似
            if (processString.contains("pid=")) {
                int start = processString.indexOf("pid=") + 4;
                int end = start;
                while (end < processString.length() && Character.isDigit(processString.charAt(end))) {
                    end++;
                }
                if (end > start) {
                    return Long.parseLong(processString.substring(start, end));
                }
            }
        } catch (Exception e) {
            log.debug("获取进程PID失败: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * 获取进程的内存占用（单位：字节）
     * 跨平台支持
     *
     * @param pid 进程ID
     * @return 内存占用字节数，如果获取失败返回 -1
     */
    private static long getProcessMemory(long pid) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                // Windows 系统
                return getProcessMemoryWindows(pid);
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
                // Linux/macOS 系统
                return getProcessMemoryUnix(pid);
            }
        } catch (Exception e) {
            log.debug("获取进程内存失败: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * 获取 Unix 系统进程的内存占用
     * 使用 ps 命令获取 RSS（Resident Set Size）
     *
     * @param pid 进程ID
     * @return 内存占用字节数
     */
    private static long getProcessMemoryUnix(long pid) {
        try {
            // 使用 ps 命令获取 RSS（单位：KB）
            ProcessBuilder pb = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-o", "rss=");
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        // RSS 单位是 KB，转换为字节
                        return Long.parseLong(line) * 1024L;
                    }
                }
            }

            process.waitFor();
        } catch (Exception e) {
            log.debug("通过 ps 命令获取内存失败: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * 获取 Windows 系统进程的内存占用
     * 使用 tasklist 命令
     *
     * @param pid 进程ID
     * @return 内存占用字节数
     */
    private static long getProcessMemoryWindows(long pid) {
        try {
            // 使用 tasklist 命令获取内存（单位：KB）
            ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH");
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(String.valueOf(pid))) {
                        // CSV 格式: "ImageName","PID","SessionName","Session#","Mem Usage"
                        String[] parts = line.split(",");
                        if (parts.length >= 5) {
                            // 提取内存使用（格式：" 12,345 K"）
                            String memStr = parts[parts.length - 1].replace("\"", "").replace("K", "").replace(" ", "").trim();
                            memStr = memStr.replace(",", ""); // 移除千位分隔符
                            if (!memStr.isEmpty()) {
                                // 单位是 KB，转换为字节
                                return Long.parseLong(memStr) * 1024L;
                            }
                        }
                    }
                }
            }

            process.waitFor();
        } catch (Exception e) {
            log.debug("通过 tasklist 命令获取内存失败: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * 获取当前已使用的内存量
     * 单位是byte
     * @return
     * @deprecated 此方法获取的是 JVM 内存，不适用于监控子进程
     */
    @Deprecated
    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
