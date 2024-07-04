import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 向服务器写入文件（植入危险程序）
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
        Process process = Runtime.getRuntime().exec(filePath);
        process.waitFor();
        BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errorCompileOutputLine;
        // 逐行读取
        while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
            System.out.println(errorCompileOutputLine + "\n");
        }
        System.out.println("危险程序执行成功");
    }
}