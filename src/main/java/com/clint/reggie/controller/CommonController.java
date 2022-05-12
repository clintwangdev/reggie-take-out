package com.clint.reggie.controller;

import com.clint.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    /**
     * 文件路径
     */
    @Value("${reggie.filepath}")
    private String filepath;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        log.info("开始文件上传, 文件名为: {}", file.getOriginalFilename());
        String oldFilename = file.getOriginalFilename();
        assert oldFilename != null;
        String[] split = oldFilename.split("\\.");
        // 获取文件后缀名
        String fileSuffix = split[1];
        // 生成新文件名
        String newFilename = UUID.randomUUID() + "." + fileSuffix;

        File dir = new File(filepath);
        if (!dir.exists()) {
            // 目录不存在，创建目录
            dir.mkdirs();
        }

        file.transferTo(new File(filepath + newFilename));
        return R.success(newFilename);
    }

    /**
     * 文件下载
     *
     * @param name     文件名
     * @param response 响应对象
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try (
                FileInputStream fileInputStream = new FileInputStream(filepath + name);
                ServletOutputStream outputStream = response.getOutputStream()
        ) {
            // 设置响应类型
            response.setContentType("image/jpeg");

            // 通过字节输入流读取图片
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                // 写入前端页面
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
