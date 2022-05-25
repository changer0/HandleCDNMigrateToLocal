import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    /**
     * MD 目录
     */
    public static final String MD_DIR = "C:\\VNote";
    /**
     * 本地图片路径
     */
    public static final String LOCAL_IMAGE_PATH = "vx_images";

    /**
     * 本地图片声明后缀
     */
    public static final String LOCAL_IMAGE_SUFFIX = ")";

    /**
     * CDN 前缀
     */
    public static final String IMAGE_NET_CND_PREFIX = "https://gitee.com/luluzhang/ImageCDN/raw/master/blog/";

    public static final String PATTERN = "!\\[(.*)]\\((\\S*[jpg|png])";

    public static final String COOKIE = "";

    public static final List<File> failedFileList = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        File srcDirFile = new File(MD_DIR);
        Collection<File> files = FileUtils.listFiles(srcDirFile, new String[]{"md"}, true);

        //备份
        log("正在备份...");
        try {
            FileUtils.copyDirectory(srcDirFile, new File(MD_DIR + "_" + System.currentTimeMillis() + "_bak"));
        } catch (IOException e) {
            throw new RuntimeException("文件夹备份失败...");
        }
        log("备份完成.^v^");
        for (File file : files) {
            log("----开始扫描的文件: " + file.getName());
            File bakFile = new File(file.getParentFile().getAbsolutePath() + File.separator + file.getName() + ".bak");
            //提前备份
            FileUtils.copyFile(file, bakFile);
            try {
                readMDFile(file);
                FileUtils.delete(bakFile);
                log("----扫描结束: " + file.getName());
            } catch (Exception e) {
                failedFileList.add(file);
                //还原文件
                FileUtils.copyFile(bakFile, file);
                loge(e);
            }
            log("");
        }

        if (!failedFileList.isEmpty()) {
            loge("失败文件集合: ");
            for (File file : failedFileList) {
                loge(file.getAbsolutePath());
            }
        }
    }

    private static void readMDFile(File file) {

        List<String> writeStringList = new ArrayList<>();
        Pattern pattern = Pattern.compile(PATTERN);
        List<String> strings = null;
        try {
            strings = FileUtils.readLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("文件夹读取失败");
        }
        File parentFile = file.getParentFile();
        File localImagePathFile = new File(parentFile.getAbsolutePath() + File.separator + LOCAL_IMAGE_PATH);
        if (!localImagePathFile.exists()) {
            if (!localImagePathFile.mkdirs()) {
                throw new RuntimeException("文件夹创建失败, 别玩了...");
            }
        }

        for (String line : strings) {
            //匹配 CDN 前缀
            if (line.contains(IMAGE_NET_CND_PREFIX)) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    log("需要修改行: " + line);
                    String g0 = matcher.group(0);
                    String imageDisplayName = matcher.group(1);
                    String imageUrl = matcher.group(2);
//                    log("g0: " + g0);
//                    log("imageDisplayName " + imageDisplayName);
//                    log("imageUrl: " + imageUrl);

                    String[] split = imageUrl.split("/");
                    String fileName = split[split.length - 1];
//                    log("fileName: " + fileName);

                    if (!new File(localImagePathFile.getAbsolutePath() + File.separator + fileName).exists()) {
                        //开始下载
                        log("开始下载: " + imageUrl);
                        try {
                            DownloadUtil.downLoadFromUrlHttps(
                                    imageUrl,
                                    fileName,
                                    localImagePathFile.getAbsolutePath(),
                                    COOKIE
                            );
                        } catch (Exception e) {
                            throw new RuntimeException("下载失败");
                        }
                        log("下载完成: " + imageUrl);
                    } else {
                        log("文件已下载: " + imageUrl);
                    }

                    StringBuilder sb = new StringBuilder("![");
                    if (!StringUtils.isEmpty(imageDisplayName)) {
                        sb.append(imageDisplayName);
                    }
                    String s = sb.append("](").append(LOCAL_IMAGE_PATH).append("/").append(fileName).append(LOCAL_IMAGE_SUFFIX).toString();
                    writeStringList.add(s);
                } else {
                    writeStringList.add(line);
                }
            } else {
                writeStringList.add(line);
            }
        }
        try {
            FileUtils.writeLines(file, writeStringList, false);
        } catch (IOException e) {
            throw new RuntimeException("文件写入失败!");
        }
    }

    private static void log(String msg) {
        System.out.println("|" + msg);
    }

    private static void loge(String msg) {
        System.err.println("|" + msg);
    }


    private static void loge(Throwable msg) {
        System.err.println("|" + msg.getMessage());
    }

}
