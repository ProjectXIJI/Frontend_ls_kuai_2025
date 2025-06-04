package com.tduck.cloud.wx.mp.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.common.util.JsonUtils;
import com.tduck.cloud.common.util.SpringContextUtils;
import com.tduck.cloud.envconfig.constant.ConfigConstants;
import com.tduck.cloud.envconfig.service.SysEnvConfigService;
import com.tduck.cloud.wx.mp.handler.*;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

import static me.chanjar.weixin.common.api.WxConsts.EventType;
import static me.chanjar.weixin.common.api.WxConsts.EventType.SUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.EventType.UNSUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.CustomerService.*;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.POI_CHECK_NOTIFY;

/**
 * wechat mp configuration
 *
 * @author Binary Wang(https://github.com/binarywang)
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(WxMpProperties.class)
public class WxMpConfiguration {
    private final LogHandler logHandler;
    private final NullHandler nullHandler;
    private final KfSessionHandler kfSessionHandler;
    private final StoreCheckNotifyHandler storeCheckNotifyHandler;
    private final LocationHandler locationHandler;
    private final MenuHandler menuHandler;
    private final MsgHandler msgHandler;
    private final UnsubscribeHandler unsubscribeHandler;
    private final SubscribeHandler subscribeHandler;
    private final ScanHandler scanHandler;
    private final WxMpProperties properties;
    private final SysEnvConfigService sysEnvConfigService;

    @Bean
    @ConditionalOnBean(SysEnvConfigService.class)
    public WxMpService wxMpService() {
        String mpConfigJson = sysEnvConfigService.getValueByKey(ConfigConstants.WX_MP_ENV_CONFIG);
        WxMpService service = new WxMpServiceImpl();
        if (StrUtil.isBlank(mpConfigJson)) {
            // Log the missing configuration
            System.out.println("WeChat MP configuration is missing. Please configure it in the system settings.");
            return service;
        }
        WxMpProperties.MpConfig configs = JsonUtils.jsonToObj(mpConfigJson, WxMpProperties.MpConfig.class);
        if (ObjectUtil.isNull(configs)) {
            System.out.println("Failed to parse WeChat MP configuration. Please check the configuration format.");
            return service;
        }

        // Check if appId and secret are provided
        if (StrUtil.isBlank(configs.getAppId()) || StrUtil.isBlank(configs.getSecret())) {
            System.out.println("WeChat MP appId or secret is missing. Please provide both in the configuration.");
            return service;
        }

        try {
            setWxMpConfig(service, configs);
        } catch (Exception e) {
            System.out.println("Failed to set WeChat MP configuration: " + e.getMessage());
        }
        return service;
    }

    /**
     * 设置微信公众号配置信息
     *
     * @param wxMpService 微信公众号服务
     * @param configs     微信公众号配置信息
     */
    public static void setWxMpConfig(WxMpService wxMpService, WxMpProperties.MpConfig configs) {
        try {
            if (configs == null) {
                System.out.println("WeChat MP configuration is null");
                return;
            }

            if (StrUtil.isBlank(configs.getAppId())) {
                System.out.println("WeChat MP appId is missing");
                return;
            }

            if (StrUtil.isBlank(configs.getSecret())) {
                System.out.println("WeChat MP secret is missing");
                return;
            }

            WxMpDefaultConfigImpl wxMpRedisConfig = new WxMpDefaultConfigImpl();
            wxMpRedisConfig.setAppId(configs.getAppId());
            wxMpRedisConfig.setSecret(configs.getSecret());
            wxMpRedisConfig.setToken(configs.getToken());
            wxMpRedisConfig.setAesKey(configs.getAesKey());

            wxMpService.setMultiConfigStorages(CollUtil.newArrayList(configs).stream().map(a -> {
                WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
                config.setAppId(a.getAppId());
                config.setSecret(a.getSecret());
                config.setToken(a.getToken());
                config.setAesKey(a.getAesKey());
                return config;
            }).collect(Collectors.toMap(WxMpDefaultConfigImpl::getAppId, a -> a, (o, n) -> o)));

            // Verify the configuration was set correctly
            if (wxMpService.getWxMpConfigStorage() == null) {
                System.out.println("Failed to set WeChat MP configuration. WxMpConfigStorage is null.");
            } else {
                System.out.println("WeChat MP configuration set successfully for appId: " + configs.getAppId());
            }
        } catch (Exception e) {
            System.out.println("Error setting WeChat MP configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

        // 记录所有事件的日志 （异步执行）
        newRouter.rule().handler(this.logHandler).next();

        // 接收客服会话管理事件
        newRouter.rule().async(false).msgType(EVENT).event(KF_CREATE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_CLOSE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_SWITCH_SESSION).handler(this.kfSessionHandler).end();

        // 门店审核事件
        newRouter.rule().async(false).msgType(EVENT).event(POI_CHECK_NOTIFY).handler(this.storeCheckNotifyHandler)
                .end();

        // 自定义菜单事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.CLICK).handler(this.menuHandler).end();

        // 点击菜单连接事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.VIEW).handler(this.nullHandler).end();

        // 关注事件
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(this.subscribeHandler).end();

        // 取消关注事件
        newRouter.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE).handler(this.unsubscribeHandler).end();

        // 上报地理位置事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.LOCATION).handler(this.locationHandler).end();

        // 接收地理位置消息
        newRouter.rule().async(false).msgType(XmlMsgType.LOCATION).handler(this.locationHandler).end();

        // 扫码事件
        newRouter.rule().async(false).msgType(EVENT).event(EventType.SCAN).handler(this.scanHandler).end();

        // 默认
        newRouter.rule().async(false).handler(this.msgHandler).end();

        return newRouter;
    }

}
