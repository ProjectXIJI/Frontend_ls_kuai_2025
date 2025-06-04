package com.tduck.cloud.storage.cloud;

import cn.hutool.core.io.FileUtil;
import com.tduck.cloud.storage.exception.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 本地文件存储
 *
 * @author : smalljop
 * @since : 2018-10-18 14:01
 */
@Slf4j
public class LocalStorageService extends OssStorageService {
    public LocalStorageService(OssStorageConfig config) {
        this.config = config;
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        try {
            log.info("上传文件到本地服务器，存储路径: {}, 文件相对路径: {}", config.getUploadFolder(), path);

            // 确保上传文件夹存在
            File uploadFolder = FileUtil.file(config.getUploadFolder());
            if (!FileUtil.exist(uploadFolder)) {
                log.info("创建上传根目录: {}", uploadFolder.getAbsolutePath());
                FileUtil.mkdir(uploadFolder);
            }

            // 构建完整的文件路径
            File file = FileUtil.file(config.getUploadFolder() + File.separator + path);

            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (!FileUtil.exist(parentDir)) {
                log.info("创建文件父目录: {}", parentDir.getAbsolutePath());
                FileUtil.mkdir(parentDir);
                boolean created = parentDir.exists();
                if (!created) {
                    log.error("无法创建目录: {}", parentDir.getAbsolutePath());
                    throw new StorageException("无法创建文件目录，请检查权限或路径是否正确");
                }
            }

            // 写入文件
            FileUtil.writeFromStream(inputStream, file);
            log.info("文件上传成功: {}", file.getAbsolutePath());

            return config.getDomain() + "/" + path;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new StorageException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String upload(byte[] data, String path) {
        try {
            log.info("上传文件到本地服务器，存储路径: {}, 文件相对路径: {}", config.getUploadFolder(), path);

            // 确保上传文件夹存在
            File uploadFolder = FileUtil.file(config.getUploadFolder());
            if (!FileUtil.exist(uploadFolder)) {
                log.info("创建上传根目录: {}", uploadFolder.getAbsolutePath());
                FileUtil.mkdir(uploadFolder);
            }

            // 构建完整的文件路径
            File file = FileUtil.file(config.getUploadFolder() + File.separator + path);

            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (!FileUtil.exist(parentDir)) {
                log.info("创建文件父目录: {}", parentDir.getAbsolutePath());
                FileUtil.mkdir(parentDir);
                boolean created = parentDir.exists();
                if (!created) {
                    log.error("无法创建目录: {}", parentDir.getAbsolutePath());
                    throw new StorageException("无法创建文件目录，请检查权限或路径是否正确");
                }
            }

            // 写入文件
            FileUtil.writeBytes(data, file);
            log.info("文件上传成功: {}", file.getAbsolutePath());

            return config.getDomain() + "/" + path;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new StorageException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            String fullPath = config.getUploadFolder() + File.separator + path;
            log.info("删除文件: {}", fullPath);
            FileUtil.del(fullPath);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
        }
    }
}
