<template>
  <div class="register-container">
    <h2 class="register-title">注册账号</h2>
    <p class="register-subtitle">欢迎加入问卷调查系统</p>

    <div class="register-form-container">
      <el-form
        ref="emailRegForm"
        :model="accountForm"
        :rules="emailRegRules"
        label-width="0px"
        status-icon
        class="custom-register-form"
      >
        <el-form-item label="" prop="email" class="custom-form-item">
          <el-input
            v-model="accountForm.email"
            prefix-icon="el-icon-message"
            autocomplete="off"
            placeholder="请输入邮箱"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item label="" prop="password" class="custom-form-item">
          <el-input
            v-model="accountForm.password"
            autocomplete="off"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            show-password
            class="custom-input"
          />
        </el-form-item>
        <el-form-item label="" prop="code" class="custom-form-item verification-code-item">
          <div class="verification-code-container">
            <el-input
              v-model="accountForm.code"
              autocomplete="off"
              class="code-input custom-input"
              maxlength="4"
              oninput="value=value.replace(/[^\d]/g,'')"
              placeholder="请输入验证码"
            >
              <font-icon slot="prefix" class="el-input__icon fa fa-shield" />
            </el-input>
            <el-button
              :disabled="emailValidateCodeBtn"
              class="verification-code-btn"
              type="primary"
              @click="sendEmailCodeHandle"
            >
              {{ emailValidateCodeBtnText }}
            </el-button>
          </div>
        </el-form-item>
        <div class="form-actions">
          <el-button class="register-btn" type="primary" @click="emailRegHandle">注册</el-button>
          <div class="back-to-login">
            <span>已有账号？</span>
            <el-link class="login-link" @click="$emit('success')">返回登录</el-link>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>
<script>
// 引入组件
import constants from '@/utils/constants'

export default {
  name: 'Register',
  data() {
    return {
      regType: 'regEmail',
      accountForm: {
        email: '',
        phoneNumber: '',
        password: ''
      },
      emailValidateCodeBtn: false,
      emailValidateCodeBtnText: '发送验证码',
      phoneValidateCodeBtn: false,
      phoneValidateCodeBtnText: '发送验证码',
      phoneRegRules: {
        phoneNumber: [
          { required: true, trigger: 'blur', message: '请输入手机号' },
          {
            pattern: /^(?:0|86|\+86)?1[3456789]\d{9}$/,
            message: '请输入正确的手机号'
          }
        ],
        password: [
          { required: true, trigger: 'blur', message: '请输入新密码' },
          {
            pattern: constants.passwordReg,
            message: constants.passwordRegDesc
          }
        ],
        code: { required: true, trigger: 'blur', message: '请输入验证码' }
      },
      emailRegRules: {
        email: [
          { required: true, trigger: 'blur', message: '请输入邮箱' },
          {
            pattern: /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/,
            message: '请输入正确的邮箱'
          }
        ],
        password: [
          { required: true, trigger: 'blur', message: '请输入新密码' },
          {
            pattern: constants.passwordReg,
            message: constants.passwordRegDesc
          }
        ],
        code: { required: true, trigger: 'blur', message: '请输入验证码' }
      }
    }
  },
  created() {},
  destroyed() {},
  methods: {
    sendEmailCodeHandle() {
      this.$refs['emailRegForm'].validateField('email', (err) => {
        if (!err) {
          this.emailValidateCodeBtn = true
          this.$api.get(`/register/email/code?email=${this.accountForm.email}`).then(() => {
            this.msgSuccess('验证码发送成功，5分钟内有效')
            this.emailValidateCodeBtn = true
            let count = 60
            let timer = setInterval(() => {
              count--
              this.emailValidateCodeBtnText = count + 's后重新发送'
              if (count == 0) {
                this.emailValidateCodeBtnText = '发送验证码'
                this.emailValidateCodeBtn = false
                clearInterval(timer)
              }
            }, 1000)
          })
        }
      })
    },
    sendPhoneCodeHandle() {
      this.$refs['phoneRegForm'].validateField('phoneNumber', (err) => {
        if (!err) {
          this.phoneValidateCodeBtn = true
          this.$api.get(`/register/phone/code?phoneNumber=${this.accountForm.phoneNumber}`).then(() => {
            this.msgSuccess('验证码发送成功，5分钟内有效')
            this.phoneValidateCodeBtn = true
            let count = 60
            let timer = setInterval(() => {
              count--
              this.phoneValidateCodeBtnText = count + 's后重新发送'
              if (count == 0) {
                this.phoneValidateCodeBtnText = '发送验证码'
                this.phoneValidateCodeBtn = false
                clearInterval(timer)
              }
            }, 1000)
          })
        }
      })
    },
    phoneRegHandle() {
      this.$refs['phoneRegForm'].validate((valid) => {
        if (valid) {
          this.$api.post('/register/phone', this.accountForm).then(() => {
            this.registerSuccessHandle()
          })
        } else {
          return false
        }
      })
    },
    emailRegHandle() {
      this.$refs['emailRegForm'].validate((valid) => {
        if (valid) {
          this.$api.post('/register/email', this.accountForm).then(() => {
            this.registerSuccessHandle()
          })
        } else {
          return false
        }
      })
    },
    registerSuccessHandle() {
      this.msgSuccess('注册成功，快去登录吧')
      setTimeout(() => {
        this.$emit('success')
      }, 1000)
    }
  }
}
</script>
<style lang="scss" scoped>
.register-container {
  width: 100%;
  animation: fadeIn 0.8s ease-out;
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  text-align: center;
}

.register-subtitle {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
  text-align: center;
}

.register-form-container {
  margin-top: 20px;
}

.custom-register-form {
  width: 100%;
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

.verification-code-container {
  display: flex;
  align-items: center;
}

.code-input {
  flex: 1;
  margin-right: 10px;
}

.verification-code-btn {
  height: 45px;
  border-radius: 8px;
  background: linear-gradient(45deg, #1890ff, #36cfc9);
  border: none;
  transition: all 0.3s ease;
  padding: 0 15px;

  &:hover {
    background: linear-gradient(45deg, #36cfc9, #1890ff);
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(24, 144, 255, 0.2);
  }

  &:disabled {
    background: #c0c4cc;
    color: #fff;
  }
}

.form-actions {
  margin-top: 30px;
}

.register-btn {
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

.back-to-login {
  text-align: center;
  font-size: 14px;
  color: #606266;

  .login-link {
    margin-left: 5px;
    font-weight: 500;
    color: #1890ff;
    transition: all 0.3s ease;

    &:hover {
      color: #36cfc9;
      transform: translateY(-1px);
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
