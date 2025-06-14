package com.tduck.cloud.common.sms;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.tduck.cloud.common.util.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author : smalljop
 * @description : 腾讯云短信
 * @create : 2020-12-15 10:33
 **/
@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class AliyunSmsServiceImpl extends SmsService {

    private Client client;

    public AliyunSmsServiceImpl(SmsPlatformProperties properties) {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(properties.getSecretId())
                // 您的AccessKey Secret
                .setAccessKeySecret(properties.getSecretKey());
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        try {
            client = new Client(config);
            this.properties = properties;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String templateId, Map<String, Object> templateParams) throws Exception {
        // 1.发送短信
        SendSmsRequest sendReq = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(properties.getSign())
                .setTemplateCode(templateId)
                .setTemplateParam(JsonUtils.objToJson(templateParams));
        SendSmsResponse sendResp = client.sendSms(sendReq);
        log.debug("阿里云短信发送结果：{}", JsonUtils.objToJson(sendResp));
        String code = sendResp.body.code;
        Assert.isTrue(StrUtil.equals(code, "OK"), sendResp.body.message);
        return StrUtil.equals(code, "OK");
    }

}
