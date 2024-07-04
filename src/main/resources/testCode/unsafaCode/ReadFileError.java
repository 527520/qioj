import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取服务器文件（文件信息泄露）
 */
public class Main {
    public static void main(String[] args) {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/application.yml";
        List<String> readAllLines = Files.readAllLines(filePath);
        System.out.println(String.join("\n", readAllLines));
    }
}