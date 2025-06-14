package com.tduck.cloud.api.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.tduck.cloud.api.web.interceptor.AuthorizationInterceptor;
import com.tduck.cloud.api.web.interceptor.NoRepeatSubmitInterceptor;
import com.tduck.cloud.api.web.resolver.LoginUserHandlerMethodArgumentResolver;
import com.tduck.cloud.storage.cloud.OssStorageConfig;
import com.tduck.cloud.storage.cloud.OssStorageFactory;
import com.tduck.cloud.storage.enums.OssTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.List;

/**
 * web mvc 配置
 *
 * @author smalljop
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * html静态资源 js静态资源 css静态资源
     */
    private final List<String> staticResources = Lists.newArrayList("/**/*.html",
            "/**/*.js",
            "/**/*.css",
            "/**/*.woff",
            "/**/*.ttf");
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    @Autowired
    private NoRepeatSubmitInterceptor noRepeatSubmitInterceptor;
    @Autowired
    private LoginUserHandlerMethodArgumentResolver loginUserHandlerMethodArgumentResolver;

    /**
     * 配置本地文件上传的虚拟路径和静态化的文件生成路径
     * 备注：这是一种图片上传访问图片的方法，实际上也可以使用nginx反向代理访问图片
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        OssStorageConfig config = OssStorageFactory.getConfig();
        if (ObjectUtil.isNotNull(config) && OssStorageFactory.getConfig().getOssType() == OssTypeEnum.LOCAL) {
            // 文件上传
            String uploadFolder = config.getUploadFolder();
            // 未配置路径时 使用jar所在目录作为文件默认存储目录
            if (StrUtil.isBlank(uploadFolder)) {
                ApplicationHome ah = new ApplicationHome(OssStorageFactory.class);
                uploadFolder = ah.getDir().getAbsolutePath() + "/images";
                log.info("未配置上传路径，使用默认路径: {}", uploadFolder);
            } else {
                log.info("使用配置的上传路径: {}", uploadFolder);
            }

            // 确保目录存在
            File uploadDir = new File(uploadFolder);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (created) {
                    log.info("成功创建上传目录: {}", uploadDir.getAbsolutePath());
                } else {
                    log.error("无法创建上传目录: {}", uploadDir.getAbsolutePath());
                }
            }

            uploadFolder = StringUtils.appendIfMissing(uploadFolder, File.separator);
            String resourceLocation = "file:" + uploadFolder;
            log.info("添加资源处理器: pattern={}, location={}", config.getAccessPathPattern(), resourceLocation);
            registry.addResourceHandler(config.getAccessPathPattern())
                    .addResourceLocations(resourceLocation);
        }

        // 这句不要忘了，否则项目默认静态资源映射会失效
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        // swagger 配置
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 所有路径都被拦截
        registry.addInterceptor(noRepeatSubmitInterceptor).addPathPatterns("/**").excludePathPatterns(staticResources);
        registry.addInterceptor(authorizationInterceptor).addPathPatterns("/**").excludePathPatterns(staticResources);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserHandlerMethodArgumentResolver);
    }
}
