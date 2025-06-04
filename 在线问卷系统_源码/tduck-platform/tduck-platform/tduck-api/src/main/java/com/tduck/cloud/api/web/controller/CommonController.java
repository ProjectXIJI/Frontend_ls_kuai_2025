package com.tduck.cloud.api.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.IdUtil;
import com.tduck.cloud.api.annotation.NotLogin;
import com.tduck.cloud.common.util.AsyncProcessUtils;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.storage.cloud.OssStorageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author : smalljop
 * @description : 通用
 * @create : 2021/06/08 15:32
 **/
@RestController
@RequestMapping("/common/")
@Slf4j
public class CommonController {

    /**
     * 获取异步处理进度
     */
    @GetMapping("/process")
    public Result<Object> getProcess(@RequestParam String key) {
        return Result.success(AsyncProcessUtils.getProcess(key));
    }

    /**
     * 上传
     *
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    @NotLogin
    public Result<String> avatar(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            log.info("开始上传通用文件, 文件名: {}", file.getOriginalFilename());

            if (!file.isEmpty()) {
                // 构建文件路径
                String path = new StringBuffer(IdUtil.simpleUUID())
                        .append(CharUtil.DOT)
                        .append(FileUtil.extName(file.getOriginalFilename())).toString();

                log.info("通用文件存储路径: {}", path);

                // 上传文件
                String url = OssStorageFactory.getStorageService().upload(file.getInputStream(), path);
                log.info("通用文件上传成功, 访问地址: {}", url);

                return Result.success(url);
            }
            return Result.failed("上传文件为空，请选择有效文件");
        } catch (Exception e) {
            log.error("通用文件上传失败: {}", e.getMessage(), e);
            return Result.failed("上传文件异常，请联系管理员: " + e.getMessage());
        }
    }

}
