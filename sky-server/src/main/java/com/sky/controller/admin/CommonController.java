package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/15 16:26
 */
@RestController
@RequestMapping("/admin/common")
@Api("通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传,上传到 阿里 OSS 服务
     * @param file
     * @return
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件到阿里云OSS上{}",file);
        try {
            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            // 开始取得文件名后缀 .png
            String fileName = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID() + fileName;
            // 获取文件的访问路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败: {}",e);
        }
        return Result.error("文件上传失败");
    }
}
