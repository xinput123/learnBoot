package com.xinput.learn.stock.util;

import com.google.common.collect.Lists;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 读取 resources 目录下文件的工具类
 */
public class ResourceFileUtils {

    /**
     * 方法1：使用 ClassPathResource 读取文件（推荐）
     *
     * @param fileName 文件名，如 "code.txt" 或 "data/code.txt"
     * @return 文件内容，每行一个元素
     */
    public static List<String> readFileFromClasspath(String fileName) {
        List<String> lines = Lists.newArrayList();
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * 方法2：使用 ClassLoader 读取文件
     *
     * @param fileName 文件名
     * @return 文件内容
     */
    public static List<String> readFileByClassLoader(String fileName) {
        List<String> lines = Lists.newArrayList();
        try {
            InputStream inputStream = ResourceFileUtils.class.getClassLoader()
                    .getResourceAsStream(fileName);
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * 方法3：读取整个文件为一个字符串
     *
     * @param fileName 文件名
     * @return 文件全部内容
     */
    public static String readFileAsString(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 方法4：判断文件是否存在
     *
     * @param fileName 文件名
     * @return 是否存在
     */
    public static boolean fileExists(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

}
