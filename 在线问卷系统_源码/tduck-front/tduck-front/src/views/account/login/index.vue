<template>
  <div class="login-container">
    <div class="login-background">
      <div class="login-overlay"></div>
    </div>

    <div class="login-card">
      <div class="login-card-header">
        <div class="login-logo">
          <img src="@/assets/images/custom-logo.svg" alt="Logo" class="logo-image" />
        </div>
        <h1 class="login-title">
          <span class="hello">Hello</span>
          <span class="comma">,</span>
          <span class="welcome">欢迎使用问卷调查系统</span>
        </h1>
      </div>

      <div class="login-card-body">
        <div v-if="formType == 'login'" class="login-tabs-container">
          <div class="login-tabs-header">
            <div :class="['login-tab', loginType === 'account' ? 'active' : '']" @click="loginType = 'account'">
              <i class="el-icon-user"></i> 账号密码登录
            </div>
            <div v-if="enableWx" :class="['login-tab', loginType === 'wx' ? 'active' : '']" @click="loginType = 'wx'">
              <i class="fab fa-weixin"></i> 微信扫码登录
            </div>
          </div>

          <div class="login-tabs-content">
            <!-- 账号密码登录 -->
            <div v-show="loginType === 'account'" class="login-tab-pane">
              <el-form
                ref="accountLoginForm"
                :model="accountForm"
                :rules="accountLoginRules"
                class="account-login-form"
                hide-required-asterisk
                label-position="top"
                size="small"
                status-icon
                @keyup.enter.native="loginHandle"
              >
                <el-form-item prop="account" class="custom-form-item">
                  <el-input
                    v-model="accountForm.account"
                    autocomplete="off"
                    placeholder="请输入手机号/邮箱"
                    prefix-icon="el-icon-user-solid"
                    class="custom-input"
                  />
                </el-form-item>
                <el-form-item prop="password" class="custom-form-item">
                  <el-input
                    v-model="accountForm.password"
                    autocomplete="off"
                    placeholder="请输入密码"
                    prefix-icon="el-icon-lock"
                    show-password
                    class="custom-input"
                  />
                </el-form-item>
                <div class="form-actions">
                  <el-button type="primary" class="login-btn" @click="loginHandle" v-re-click> 登录 </el-button>
                  <div class="form-links">
                    <el-link class="link-btn" @click="toForgetPwdHandle">忘记密码</el-link>
                    <span class="link-divider">|</span>
                    <el-link class="link-btn" @click="formType = 'reg'">立即注册</el-link>
                  </div>
                </div>
              </el-form>
            </div>

            <!-- 微信扫码登录 -->
            <div v-show="loginType === 'wx' && enableWx" class="login-tab-pane wx-login-pane">
              <div class="wx-login">
                <div class="qrcode-container">
                  <el-image
                    v-loading="wxQrCodeLoading"
                    :src="wxLoginQrCode"
                    class="wx-login-qrcode"
                    fit="fill"
                    @load="wxQrCodeLoading = false"
                  />
                </div>
                <p class="qrcode-tip">请使用微信扫一扫登录</p>
                <el-button type="text" class="refresh-btn" @click="getLoginWxQrCode">
                  <i class="el-icon-refresh"></i> 刷新二维码
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 注册表单 -->
        <register v-if="formType == 'reg'" class="register-form" @success="registerSuccessHandle" />
      </div>

      <div class="login-card-footer">
        <p class="footer-text">问卷调查系统 © {{ new Date().getFullYear() }}</p>
        <p class="footer-desc">本系统提供了丰富的问卷调查功能，支持多种问卷类型和数据分析</p>
      </div>
    </div>
  </div>
</template>

<script>
import { getCurrentDomain } from '@/utils'
// 引入组件
import constants from '@/utils/constants'
import Register from '../register/index.vue'

export default {
  name: 'LoginPage',
  components: {
    Register
  },
  data() {
    const validateAccount = (rule, value, callback) => {
      const reg1 = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/
      const reg2 = /^(?:0|86|\+86)?1[3456789]\d{9}$/
      if (reg1.test(value) || reg2.test(value)) {
        callback()
      } else {
        callback(new Error('请输入正确的账号'))
      }
    }
    return {
      formType: 'login',
      loginType: 'wx',
      agreeProtocol: '',
      accountLoginRules: {
        account: [
          { required: true, trigger: 'blur', message: '请输入账号' },
          { trigger: 'blur', validator: validateAccount }
        ],
        password: [
          {
            required: true,
            trigger: 'blur',
            message: '请输入新密码'
          },
          {
            pattern: constants.passwordReg,
            message: constants.passwordRegDesc
          }
        ]
      },
      accountForm: {
        email: '',
        phoneNumber: '',
        password: ''
      },
      wxQrCodeLoading: true,
      wxLoginQrCode: '',
      wxLoginId: '',
      refreshWxQrcodeTimer: null,
      wxQrcodeResultTimer: null,
      qqLoginAuthorizeUrl: '',
      enableWx: false
    }
  },
  computed: {},
  watch: {},
  async created() {
    let sysRes = await this.$api.get('/public/systemInfoConfig')
    let { openWxMpLogin } = JSON.parse(sysRes.data)
    this.enableWx = openWxMpLogin || false
    if (this.enableWx) {
      const self = this
      this.getLoginWxQrCode()
      this.refreshWxQrcodeTimer = setInterval(() => {
        if (self.loginType == 'account') {
          // 如果是账号登录模式就暂停轮询
          return
        }
        this.getLoginWxQrCode()
      }, 5 * 60 * 1000)
      this.wxQrcodeResultTimer = setInterval(() => {
        if (self.loginType == 'account') {
          // 如果是账号登录模式就暂停轮询
          return
        }
        this.getLoginWxQrCodeResult()
      }, 5 * 1000)
      this.getQQLoginAuthorizeUrl()
    } else {
      this.loginType = 'account'
    }
  },
  destroyed() {
    clearInterval(this.refreshWxQrcodeTimer)
    clearInterval(this.wxQrcodeResultTimer)
  },
  methods: {
    // 获取微信登录二维码
    getLoginWxQrCode() {
      this.wxQrCodeLoading = true
      this.$api.get('/login/wx/qrcode').then((res) => {
        this.wxLoginQrCode = res.data.qrCodeUrl
        this.wxLoginId = res.data.loginId
      })
    },
    // 忘记密码
    toForgetPwdHandle() {
      this.$router.push({ path: '/forget/password' })
    },
    // qq登录授权地址
    getQQLoginAuthorizeUrl() {
      let reUrl = getCurrentDomain() + '/redirect/qqlogin'
      this.$api
        .get('/login/qq/authorize/url', {
          params: { redirectUri: reUrl }
        })
        .then((res) => {
          this.qqLoginAuthorizeUrl = res.data
        })
    },
    redirectUrl(url) {
      location.href = url
    },
    getLoginWxQrCodeResult() {
      this.$api
        .get('/login/wx/qrcode/result', {
          params: { loginId: this.wxLoginId }
        })
        .then((res) => {
          if (res.data) {
            this.loginSuccessHandle(res.data)
          }
        })
    },
    loginSuccessHandle(data) {
      this.msgSuccess('登录成功')
      this.$store.dispatch('user/login', data).then(() => {
        // 重置状态
        this.$store.dispatch('global/loginExpired', false).then(() => {})
        // 登录成功后路由跳回
        if (this.$route.query.redirect) {
          this.$router.replace({
            path: this.$route.query.redirect
          })
        } else {
          if (window.history.length <= 1) {
            this.$router.push({ path: '/home' })
          } else {
            this.$router.push({ path: '/home' })
          }
        }
      })
    },
    registerSuccessHandle() {
      this.formType = 'login'
      this.loginType = 'account'
    },
    loginHandle() {
      this.$refs['accountLoginForm'].validate((valid) => {
        if (valid) {
          this.$api
            .request({
              url: '/login/account',
              method: 'post',
              data: this.accountForm
            })
            .then((res) => {
              this.loginSuccessHandle(res.data)
            })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: #f5f7fa;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('~@/assets/images/survey-bg.jpg') no-repeat center center;
  background-size: cover;
  z-index: 0;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(2px);
  }
}

.login-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.3), rgba(54, 207, 201, 0.3));
  z-index: 1;
}

.login-card {
  position: relative;
  z-index: 2;
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  backdrop-filter: blur(10px);
  animation: fadeInUp 0.8s ease-out;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 15px 60px rgba(0, 0, 0, 0.25);
    transform: translateY(-5px);
  }
}

.login-card-header {
  padding: 30px 30px 10px;
  text-align: center;
}

.login-logo {
  margin-bottom: 20px;

  .logo-image {
    height: 50px;
    animation: fadeIn 1s ease-out;
  }
}

.login-title {
  margin: 0 0 20px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;

  .hello {
    font-size: 32px;
    font-weight: bold;
    background: linear-gradient(45deg, #1890ff, #36cfc9);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: fadeIn 1s ease-out;
  }

  .comma {
    font-size: 32px;
    font-weight: bold;
    color: #303133;
    margin: 0 5px;
  }

  .welcome {
    display: block;
    font-size: 20px;
    font-weight: 500;
    color: #606266;
    margin-top: 5px;
    animation: fadeIn 1.2s ease-out;
  }
}

.login-card-body {
  padding: 0 30px 30px;
}

.login-tabs-container {
  margin-bottom: 20px;
}

.login-tabs-header {
  display: flex;
  margin-bottom: 25px;
  border-bottom: 1px solid #ebeef5;
}

.login-tab {
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;

  &:hover {
    color: #1890ff;
  }

  &.active {
    color: #1890ff;

    &::after {
      content: '';
      position: absolute;
      bottom: -1px;
      left: 20%;
      width: 60%;
      height: 3px;
      background: linear-gradient(45deg, #1890ff, #36cfc9);
      border-radius: 3px;
    }
  }

  i {
    margin-right: 5px;
  }
}

.login-tab-pane {
  animation: fadeIn 0.5s ease-out;
}

.custom-form-item {
  margin-bottom: 25px;
}

.custom-input {
  ::v-deep .el-input__inner {
    height: 45px !important;
    line-height: 45px !important;
    border-radius: 8px;
    background: #f5f7fa !important;
    border: 1px solid #e4e7ed;
    padding-left: 45px;
    transition: all 0.3s ease;

    &:focus {
      border-color: #1890ff;
      background: #fff !important;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
    }
  }

  ::v-deep .el-input__prefix {
    left: 15px;
    color: #909399;
  }
}

.form-actions {
  margin-top: 30px;
}

.login-btn {
  width: 100%;
  height: 45px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(45deg, #1890ff, #36cfc9);
  border: none;
  margin-bottom: 20px;
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(45deg, #36cfc9, #1890ff);
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(24, 144, 255, 0.2);
  }
}

.form-links {
  display: flex;
  justify-content: center;
  align-items: center;

  .link-btn {
    font-size: 14px !important;
    color: #606266;
    transition: all 0.3s ease;

    &:hover {
      color: #1890ff;
      transform: translateY(-1px);
    }
  }

  .link-divider {
    margin: 0 10px;
    color: #dcdfe6;
  }
}

.wx-login-pane {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}

.wx-login {
  text-align: center;
}

.qrcode-container {
  margin-bottom: 15px;
}

.wx-login-qrcode {
  width: 180px;
  height: 180px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
  }
}

.qrcode-tip {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
}

.refresh-btn {
  color: #1890ff;
  font-size: 14px;

  i {
    margin-right: 5px;
  }
}

.login-card-footer {
  padding: 20px 30px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

.footer-text {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 5px;
}

.footer-desc {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
