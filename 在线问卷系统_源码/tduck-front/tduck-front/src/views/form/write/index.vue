<template>
  <div class="form-page">
    <h1 id="inActiveTime" style="display: none" />
    <!-- 返回按钮，只在用户登录且不是分享链接时显示 -->
    <div v-if="isLoggedIn && !isShared" class="back-button-container">
      <el-button type="primary" icon="el-icon-arrow-left" size="small" @click="goToHome">返回</el-button>
    </div>

    <!-- 错误状态 -->
    <div v-cloak v-if="writeStatus == 0" class="form-status-container">
      <el-result :title="writeNotStartPrompt" icon="error" />
    </div>

    <!-- 密码输入状态 -->
    <div v-cloak v-if="writeStatus == 3" class="form-status-container">
      <el-card class="pwdinput-card text-center">
        <div slot="header" class="clearfix">
          <span>请输入密码填写</span>
        </div>
        <el-input v-model="writePassword" placeholder="请输入密码填写" />
        <el-button class="width50 mt20" type="primary" @click="handleCheckWritePwd">确定</el-button>
      </el-card>
    </div>

    <!-- 表单填写状态 -->
    <div v-if="writeStatus == 1" class="form-content" :class="{ 'shared-mode': true }">
      <!-- 用户信息悬浮卡片 - 包含表单元信息 -->
      <user-info-input
        ref="userInfoInput"
        v-if="formConfig.formKey"
        :form-info="{
          formCreator,
          formCreateTime,
          formStats
        }"
      />

      <!-- 表单内容 -->
      <biz-project-form
        v-if="formConfig.formKey"
        ref="bizProjectForm"
        :form-config="formConfig"
        @submit="submitForm"
        class="form-body"
        v-floating-submit
      />
    </div>

    <!-- 提交成功状态 -->
    <div v-cloak v-if="writeStatus == 2" class="form-status-container">
      <div v-if="userFormSetting.submitShowType === 2" v-html="userFormSetting.submitShowCustomPageContent" />
      <el-result v-else :title="'提交成功'" icon="success" />
    </div>
  </div>
</template>

<script>
import Cookies from 'js-cookie'
import { BizProjectForm } from 'tduck-form-generator'
import 'tduck-form-generator/dist/TduckForm.css'
import '@/assets/styles/form/questionnaire.scss'
import { getQueryString } from '@/utils'
import constants from '@/utils/constants'
import { removeFormData } from '@/utils/db'
import { getAuthorizationUrl, getAuthorizationUserInfo, getWxSignature } from '@/api/project/wxmp'
import { isWxEnv, onlyWxOpenHandle, setWxConfig } from './wx'
import TduckFormMixin from '../TduckFormMixin'
import UserInfoInput from './UserInfoInput'
import { createFormResultRequest, publicCreateFormResultRequest, viewFormResultRequest } from '@/api/project/data'
import { getProjectRequest } from '@/api/project/form'
import { checkWritePwdRequest, getPublicSettingsRequest, getWriteSettingStatusRequest } from '@/api/project/setting'
import { getFormReportRequest } from '@/api/project/report'
import { jumpUrl } from './SubmitJump'
import { checkWriteCountCap, saveWriteCount } from '@/views/form/write/DeviceWriteCount'
import { mapGetters } from 'vuex'

const uaParser = require('ua-parser-js')
const ua = uaParser(navigator.userAgent)

require('@/utils/ut')

export default {
  name: 'WriteView',
  components: {
    BizProjectForm,
    UserInfoInput
  },
  mixins: [TduckFormMixin],
  props: {
    // 1：公开填写 2：指定成员填写
    writeType: {
      type: Number,
      default: 1
    },
    // 是否是分享链接访问
    isShared: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      formKey: '',
      writePassword: '',
      inActiveTime: 0,
      formConfig: {
        formKey: '',
        preview: false,
        showBtns: true,
        supportText: '在线问卷',
        showItemNumber: true // 显示题目序号
      },
      formTitle: '问卷调查',
      formDescription: '',
      formCreateTime: '',
      formCreator: null,
      writeStatus: 1,
      // 不允许填写的提示信息
      writeNotStartPrompt: '',
      userFormSetting: {},
      // 微信授权地址
      wxAuthorizationUrl: '',
      wxAuthorizationCode: '',
      wxUserInfo: {},
      wxSignature: {},
      // 表单统计数据
      formStats: {
        viewCount: 0,
        completeCount: 0,
        completeRate: 0,
        avgCompleteTime: 0
      },
      // 待提交表单数据
      submitFormData: {
        completeTime: null,
        formKey: null,
        submitOs: ua.os.name,
        submitBrowser: ua.browser.name,
        submitUa: ua,
        wxUserInfo: null,
        wxOpenId: null,
        originalData: null
      },
      // 提交后返回的数据
      submitResult: {
        // 随机编号
        randomNumberText: '',
        // 考试成绩
        examScoreText: ''
      }
    }
  },
  computed: {
    ...mapGetters({
      getUserInfo: 'user/userInfo'
    }),
    // 判断用户是否登录
    isLoggedIn() {
      return !!this.$store.getters['user/isLogin']
    }
  },
  async created() {
    this.formKey = this.$route.query.key || this.$route.params.key
    // cookie获取微信用户信息
    this.getCookieAuthorizationUserInfo()
    // 微信授权重定向到该页 会携带code参数
    let wxCode = getQueryString('code')
    if (wxCode) {
      this.wxAuthorizationCode = wxCode
      await this.getWxAuthorizationUserInfo()
    }
    // 微信逻辑授权结束 授权地址
    this.getWxAuthorizationUrl()
    // 检查是否能进入填写
    this.queryProjectSettingStatus()
    // 查询相关设置 检查是否能打开等
    let settingRes = await this.queryProjectSetting()
    // 获取表单基本信息
    this.getFormBasicInfo()
    // 非微信环境不加载微信相关 加快访问速度
    if (constants.enableWx && isWxEnv()) {
      // 加载微信相关 获取签名
      getWxSignature({ url: window.location.href }).then((res) => {
        this.wxSignature = res.data
        setWxConfig(this.wxSignature, settingRes.data)
      })
    }
    viewFormResultRequest(this.formKey).then(() => {})
  },
  methods: {
    // 返回用户首页
    goToHome() {
      this.$router.push('/home')
    },
    // 获取表单基本信息
    getFormBasicInfo() {
      getProjectRequest(this.formKey).then((res) => {
        if (res.data) {
          this.formTitle = res.data.name
          this.formDescription = res.data.description
          this.formCreateTime = res.data.createTime
          // 设置表单创建者信息
          this.formCreator = {
            userId: res.data.userId,
            userName: res.data.createUserName || res.data.userName || '未知用户'
          }
          console.log('表单创建者信息:', res.data)
          // 获取表单统计数据
          this.getFormStats()
        }
      })
    },

    // 获取表单统计数据
    getFormStats() {
      getFormReportRequest({ formKey: this.formKey })
        .then((res) => {
          if (res.data) {
            this.formStats = res.data
            // 确保数据存在，避免出现NaN
            this.formStats.completeCount = this.formStats.completeCount || 0
            this.formStats.viewCount = this.formStats.viewCount || 0

            // 计算完成率
            if (this.formStats.completeCount && this.formStats.viewCount) {
              let rate = this.formStats.completeCount / this.formStats.viewCount
              this.formStats.completeRate = rate > 1 ? 100 : Math.round(rate * 100)
            } else {
              this.formStats.completeRate = 0
            }
          }
        })
        .catch((err) => {
          console.error('获取表单统计数据失败:', err)
        })
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知时间'
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    queryProjectSettingStatus() {
      // 是否能进入填写
      getWriteSettingStatusRequest({
        formKey: this.formKey,
        type: this.writeType,
        wxOpenId: this.wxUserInfo ? this.wxUserInfo.openid : ''
      }).then((res) => {
        if (res.msg) {
          this.writeNotStartPrompt = res.msg
          this.writeStatus = 0
        }
      })
    },
    getCookieAuthorizationUserInfo() {
      // cookie缓存拿一次 没有的话调接口拿
      let wxUserInfo = Cookies.get('wxUserInfo')
      if (wxUserInfo) {
        wxUserInfo = JSON.parse(wxUserInfo)
        console.log(wxUserInfo)
        if (wxUserInfo.openid) {
          this.wxUserInfo = wxUserInfo
        }
      }
    },
    getWxAuthorizationUserInfo() {
      // 根据code 获取用户授权信息
      let wxAuthorizationCode = this.wxAuthorizationCode
      return getAuthorizationUserInfo({
        code: wxAuthorizationCode
      }).then((res) => {
        if (res.data) {
          console.log('wxuser')
          this.wxUserInfo = res.data
          Cookies.set('wxUserInfo', JSON.stringify(res.data))
          console.log(this.wxUserInfo)
        }
      })
    },
    getWxAuthorizationUrl() {
      // 获取微信授权url地址
      getAuthorizationUrl({ url: window.location.href }).then((res) => {
        if (res.data) {
          this.wxAuthorizationUrl = res.data
        }
      })
    },
    async queryProjectSetting() {
      // 填写设置
      let res = await getPublicSettingsRequest(this.formKey)
      this.formConfig.formKey = this.formKey
      if (res.data) {
        this.userFormSetting = res.data
        this.formConfig.setting = res.data
        // 设备填写次数校验
        this.checkDeviceWriteLimit()
        // 仅在微信环境打开
        if (res.data && res.data.onlyWxWrite) {
          // 记录微信用户信息
          if (res.data.recordWxUser && !this.wxAuthorizationCode) {
            if (Object.keys(this.wxUserInfo).length === 0) {
              location.href = this.wxAuthorizationUrl
            }
          } else {
            onlyWxOpenHandle()
          }
        }
        // 开启了密码填写
        if (res.data && res.data.passwordWriteStatus) {
          this.writeStatus = 3
        }
      }
      return res
    },
    /**
     * 检查设备填写次数
     */
    checkDeviceWriteLimit() {
      if (this.userFormSetting.deviceWriteCountLimitStatus) {
        const flag = checkWriteCountCap(this.formKey, this.userFormSetting.deviceWriteCountLimit)
        if (flag) {
          this.writeNotStartPrompt = this.userFormSetting.deviceWriteCountLimitText
          this.writeStatus = 0
        }
        return flag
      }
    },
    handleCheckWritePwd() {
      checkWritePwdRequest({
        formKey: this.formKey,
        password: this.writePassword
      }).then((res) => {
        this.writeStatus = 1
      })
    },
    async submitForm(data) {
      try {
        // 验证用户信息
        const userInfo = await this.$refs.userInfoInput.validate()

        // 完成时间
        this.submitFormData.completeTime = document.getElementById('inActiveTime').innerText
        this.submitFormData.wxUserInfo = this.wxUserInfo
        this.submitFormData.wxOpenId = this.wxUserInfo ? this.wxUserInfo.openid : ''
        this.submitFormData.originalData = data.formModel
        this.submitFormData.formKey = this.formKey
        this.submitFormData.formId = data.formId
        this.submitFormData.formType = this.$refs.bizProjectForm.formConf.formType

        // 添加用户名和邮箱信息
        this.submitFormData.userName = userInfo.userName
        this.submitFormData.userEmail = userInfo.userEmail

        let res = null
        if (this.writeType === 1) {
          res = await publicCreateFormResultRequest(this.submitFormData)
        } else {
          res = await createFormResultRequest(this.submitFormData)
        }
        this.handleSubmitSuccess(res.data)
      } catch (error) {
        this.$message.error(error)
      }
    },
    // 提交成功后处理
    handleSubmitSuccess(data) {
      this.submitResult = data
      // 删除临时缓存数据
      removeFormData(this.formKey)
      // 如果开启了设备填写次数限制 则保存记录
      saveWriteCount(this.formKey)

      // 保存已回答的问卷到本地存储
      try {
        // 获取现有的已回答问卷记录
        let answeredForms = {}
        const answeredFormsStr = localStorage.getItem('answeredForms')
        if (answeredFormsStr) {
          answeredForms = JSON.parse(answeredFormsStr)
        }

        // 添加当前问卷
        answeredForms[this.formKey] = true

        // 保存回本地存储
        localStorage.setItem('answeredForms', JSON.stringify(answeredForms))
      } catch (error) {
        console.error('保存已回答问卷记录失败:', error)
      }

      // 移除悬浮提交按钮
      const floatingBtn = document.querySelector('.fixed-submit-btn')
      if (floatingBtn && floatingBtn.parentNode) {
        floatingBtn.parentNode.removeChild(floatingBtn)
      }

      this.writeStatus = 2
      if (this.userFormSetting.submitJump) {
        jumpUrl(this.userFormSetting.submitJumpUrl, data.id)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.form-page {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.back-button-container {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
}

.form-status-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 70vh;
}

.form-content {
  width: 100%;
  max-width: 1200px; /* 增加宽度 */
  margin: 20px auto;
  padding: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center; /* 居中显示 */

  /* 为左侧悬浮卡片留出空间 */
  @media (min-width: 1200px) {
    &.shared-mode {
      margin-left: 300px; /* 为左侧信息卡片留出空间 */
    }
  }
}

.form-header {
  text-align: center;
  margin-bottom: 20px; /* 减小下边距 */
  background-color: #fff;
  padding: 15px 20px; /* 减小上下内边距 */
  border-radius: 8px 8px 0 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: relative; /* 添加相对定位 */

  h2 {
    font-size: 22px; /* 减小字体 */
    color: #333;
    margin-bottom: 8px;
    font-weight: 500;
  }

  .description {
    font-size: 14px;
    color: #666;
    line-height: 1.5;
    margin-bottom: 15px;
  }

  .form-meta-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 10px;
    border-top: 1px dashed #eee;
    font-size: 13px;
    color: #606266;

    .meta-item {
      display: flex;
      align-items: center;

      i {
        margin-right: 5px;
      }

      .el-tag {
        margin-right: 5px;
      }
    }
  }

  /* 分享模式下的头部样式 */
  &.integrated-header {
    margin-bottom: 0;
    padding: 0;
    background-color: transparent;
    box-shadow: none;
    border-radius: 0;
  }
}

.form-body {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 60px; /* 为固定按钮留出空间 */
  width: 100%;
  max-width: 800px; /* 控制表单宽度 */
}

.pwdinput-card {
  width: 350px;
  margin: 0 auto;
  min-height: 200px;
}

::v-deep .form-body {
  .form-item-wrapper {
    position: relative;
    margin-bottom: 20px;
    padding: 15px 20px;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      border-color: #e6f7ff;
    }

    .item-number {
      position: absolute;
      left: -12px;
      top: 15px;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #1890ff;
      color: white;
      border-radius: 50%;
      font-weight: bold;
      font-size: 13px;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
      z-index: 10;
    }

    .el-form-item__label {
      font-size: 15px;
      font-weight: 500;
      color: #333;
      padding-left: 10px;
    }

    .el-form-item__content {
      padding-left: 10px;
    }

    .el-form-item {
      margin-bottom: 0;
    }
  }

  .form-btn-wrapper {
    margin-top: 40px;
    margin-bottom: 60px; /* 为固定按钮留出空间 */
    text-align: center;

    .el-button {
      display: none; /* 隐藏原按钮 */
    }
  }

  /* 添加固定在底部的提交按钮和底部渐变效果 */
  &::after {
    content: '';
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 70px;
    background: linear-gradient(to top, rgba(245, 247, 250, 0.95), rgba(245, 247, 250, 0));
    z-index: 999;
    pointer-events: none;
    backdrop-filter: blur(2px);
  }

  .fixed-submit-btn {
    position: fixed;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    padding: 12px 60px;
    font-size: 16px;
    font-weight: 500;
    border-radius: 6px;
    background-color: #409eff;
    color: white;
    border: none;
    box-shadow: 0 6px 16px rgba(24, 144, 255, 0.35);
    z-index: 1001; /* 提高z-index确保按钮始终在最上层 */
    cursor: pointer;
    transition: all 0.3s ease;
    width: auto;
    min-width: 180px;
    text-align: center;
    margin-bottom: 10px; /* 增加底部间距 */

    &:hover {
      transform: translateX(-50%) translateY(-3px);
      box-shadow: 0 8px 20px rgba(24, 144, 255, 0.45);
      background-color: #66b1ff;
    }
  }
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .form-content {
    margin: 0;
    max-width: 100%;

    &.shared-mode {
      margin-left: 0;
    }
  }

  .form-body {
    max-width: 100%;
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .form-body {
    padding: 15px;
    border-radius: 0;
  }

  ::v-deep .form-body {
    .form-item-wrapper {
      padding: 12px;
      margin-bottom: 15px;

      .item-number {
        width: 22px;
        height: 22px;
        font-size: 12px;
        left: -10px;
        top: 12px;
      }
    }
  }
}
</style>
