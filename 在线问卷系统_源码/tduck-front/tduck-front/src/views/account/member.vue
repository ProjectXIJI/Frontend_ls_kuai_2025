<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <div class="profile-header">
        <h2 class="profile-title">个人信息</h2>
      </div>

      <div v-if="userInfo" class="profile-content">
        <!-- Avatar section -->
        <div class="profile-avatar-section">
          <div class="avatar-container" @click="openAvatarUpload">
            <el-avatar class="profile-avatar" :size="120" :src="userInfo.avatar || ''" :key="avatarKey">
              <img v-if="!userInfo.avatar" src="@/assets/images/default-avatar.png" alt="默认头像" />
            </el-avatar>
            <div class="avatar-overlay">
              <i class="el-icon-camera"></i>
              <span>更换头像</span>
            </div>
          </div>
          <div class="profile-username">{{ userInfo.name }}</div>
        </div>

        <!-- Info section -->
        <div class="profile-info-section">
          <div class="info-row">
            <div class="info-label">昵称</div>
            <div class="info-value">{{ userInfo.name }}</div>
            <el-button type="primary" size="small" class="action-btn" @click="editNameDialogVisible = true"
              >修改</el-button
            >
          </div>

          <div class="info-row">
            <div class="info-label">账号</div>
            <div class="info-value">{{ userInfo.email }}</div>
            <el-button type="primary" size="small" class="action-btn" @click="emailDialogVisible = true"
              >绑定</el-button
            >
          </div>

          <div class="info-row">
            <div class="info-label">密码</div>
            <div class="info-value">******</div>
            <el-button type="primary" size="small" class="action-btn" @click="pwdDialogVisible = true">修改</el-button>
          </div>

          <div class="info-row">
            <div class="info-label">手机号</div>
            <div class="info-value">{{ userInfo.phoneNumber || '未绑定' }}</div>
            <el-button type="primary" size="small" class="action-btn" @click="phoneDialogVisible = true">
              {{ userInfo.phoneNumber ? '修改' : '绑定' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Dialogs -->
    <el-dialog
      title="修改用户名"
      :visible.sync="editNameDialogVisible"
      width="400px"
      center
      custom-class="custom-dialog"
    >
      <el-form ref="updateNameForm" :model="userInfoForm" :rules="userInfoRules" label-width="80px">
        <el-form-item label="新用户名" prop="name">
          <el-input v-model="userInfoForm.name" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editNameDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="
            () => {
              this.$refs['updateNameForm'].validateField('name', (err) => {
                if (!err) {
                  this.editNameDialogVisible = false
                  this.updateUserHandle()
                }
              })
            }
          "
          >保存</el-button
        >
      </span>
    </el-dialog>

    <el-dialog title="修改密码" :visible.sync="pwdDialogVisible" width="400px" center custom-class="custom-dialog">
      <el-form ref="updatePassWordForm" :model="userPwdForm" :rules="userPwdRules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="userPwdForm.oldPassword" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="userPwdForm.password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="repeatPassword">
          <el-input v-model="userPwdForm.repeatPassword" placeholder="请重复输入密码" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="
            () => {
              this.$refs['updatePassWordForm'].validate((valid) => {
                if (valid) {
                  this.pwdDialogVisible = false
                  this.updateUserPwdHandle()
                }
              })
            }
          "
          >确定</el-button
        >
      </span>
    </el-dialog>

    <el-dialog title="修改邮箱" :visible.sync="emailDialogVisible" width="400px" center custom-class="custom-dialog">
      <el-form ref="updateEmailForm" :model="userInfoForm" :rules="userInfoRules" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userInfoForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="emailDialogVisible = false">取消</el-button>
        <el-button
          v-prevent-re-click
          type="primary"
          @click="
            () => {
              this.$refs['updateEmailForm'].validateField('email', (err) => {
                if (!err) {
                  this.emailDialogVisible = false
                  this.sendUpdateEmail()
                }
              })
            }
          "
          >发送验证邮件</el-button
        >
      </span>
    </el-dialog>

    <el-dialog title="修改手机号" :visible.sync="phoneDialogVisible" width="400px" center custom-class="custom-dialog">
      <el-form ref="updatePhoneForm" :model="userInfoForm" :rules="userInfoRules" label-width="100px">
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="userInfoForm.phoneNumber" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="verification-code-input">
            <el-input v-model="userInfoForm.code" placeholder="请输入验证码" />
            <el-button v-prevent-re-click @click="sendUpdatePhoneNumber">
              {{ phoneValidateCodeBtnText }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="phoneDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="
            () => {
              this.$refs['updatePhoneForm'].validateField(['phoneNumber', 'code'], (err) => {
                if (!err) {
                  this.phoneDialogVisible = false
                  this.updateUserPhoneHandle()
                }
              })
            }
          "
          >确定</el-button
        >
      </span>
    </el-dialog>

    <!-- Avatar Upload Modal -->
    <div v-if="showUploadAvatar" class="avatar-upload-modal" @click.self="closeAvatarUpload">
      <div class="avatar-modal-content">
        <div class="avatar-modal-header">
          <h3>更换头像</h3>
          <button class="close-button" @click="closeAvatarUpload">
            <i class="el-icon-close"></i>
          </button>
        </div>

        <div class="avatar-modal-body">
          <div class="current-avatar">
            <el-avatar :size="100" :src="userInfo.avatar || ''" :key="'modal-' + avatarKey">
              <img v-if="!userInfo.avatar" src="@/assets/images/default-avatar.png" alt="默认头像" />
            </el-avatar>
          </div>

          <my-upload
            v-model="showUploadCropper"
            field="file"
            :width="300"
            :height="300"
            :url="getUploadUrl()"
            :headers="getUploadHeader()"
            img-format="png"
            @crop-upload-success="cropUploadSuccess"
            :params="{ type: 'avatar' }"
            :size="2048"
            :imgMove="true"
            :full="false"
            :language="{
              hint: '点击或拖拽图片到此区域',
              loading: '正在上传...',
              noSupported: '浏览器不支持该操作',
              success: '上传成功',
              fail: '上传失败',
              preview: '预览',
              btn: {
                off: '取消',
                back: '上一步',
                save: '确定'
              }
            }"
          />
        </div>

        <div class="avatar-modal-footer">
          <el-button @click="closeAvatarUpload">取消</el-button>
          <el-button type="primary" @click="closeAvatarUpload">确定</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import myUpload from 'vue-image-crop-upload'
import constants from '@/utils/constants'
import FontIcon from '@/components/FontIcon'

export default {
  name: 'MemberView',
  components: {
    FontIcon,
    myUpload
  },
  data() {
    let validateRePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.userPwdForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    // 添加avatarKey用于强制刷新头像
    const avatarKey = Date.now()
    return {
      phoneValidateCodeBtnText: '发送验证码',
      userInfoRules: {
        name: [{ required: true, trigger: 'blur', message: '请输入昵称' }],
        email: [
          { required: true, trigger: 'blur', message: '请输入邮箱' },
          {
            pattern: /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/,
            message: '请输入正确的邮箱'
          }
        ],
        phoneNumber: [
          { required: true, trigger: 'blur', message: '请输入手机号' },
          {
            pattern: /^(?:0|86|\+86)?1[3456789]\d{9}$/,
            message: '请输入正确的手机号'
          }
        ],
        code: [{ required: true, trigger: 'blur', message: '请输入验证码' }]
      },
      userPwdRules: {
        oldPassword: [
          { required: true, trigger: 'blur', message: '请输入旧密码' },
          {
            pattern: constants.passwordReg,
            message: constants.passwordRegDesc
          }
        ],
        password: [
          { required: true, trigger: 'blur', message: '请输入新密码' },
          {
            pattern: constants.passwordReg,
            message: constants.passwordRegDesc
          }
        ],
        repeatPassword: [{ required: true, trigger: 'blur', validator: validateRePass }]
      },
      editNameDialogVisible: false,
      pwdDialogVisible: false,
      phoneDialogVisible: false,
      emailDialogVisible: false,
      userInfo: {},
      userInfoForm: {},
      userPwdForm: {
        oldPassword: '',
        password: '',
        repeatPassword: ''
      },
      showUploadAvatar: false,
      showUploadCropper: false
    }
  },
  created() {
    this.queryUserInfo()
  },
  methods: {
    queryUserInfo() {
      this.$api.get('/user/current/detail').then((res) => {
        if (res.data) {
          this.userInfo = res.data
          this.userInfoForm = { ...res.data }
          this.$store.dispatch('user/update', this.userInfo)
        }
      })
    },
    getUploadHeader() {
      return {
        token: this.$store.getters['user/isLogin']
      }
    },
    getUploadUrl() {
      return `${process.env.VUE_APP_API_ROOT}/user/file/upload`
    },
    updateUserPwdHandle() {
      this.$refs['updatePassWordForm'].validate((valid) => {
        if (valid) {
          this.$api.post('/user/update/password', this.userPwdForm).then((res) => {
            if (res.data) {
              this.msgSuccess('修改成功')
              this.userPwdForm = {
                oldPassword: '',
                password: '',
                repeatPassword: ''
              }
              this.queryUserInfo()
            }
          })
        }
      })
    },
    sendUpdateEmail() {
      this.$api.get('/user/update-email/msg', { params: { email: this.userInfoForm.email } }).then((res) => {
        if (res.data) {
          this.msgSuccess('发送成功,请去您的邮箱查看')
        }
      })
    },
    sendUpdatePhoneNumber() {
      this.$refs['updatePhoneForm'].validateField('phoneNumber', (err) => {
        if (!err) {
          this.$api.get(`/user/update-phone/code?phoneNumber=${this.userInfoForm.phoneNumber}`).then(() => {
            this.msgSuccess('验证码发送成功，5分钟内有效')
            let count = 60
            let timer = setInterval(() => {
              count--
              this.phoneValidateCodeBtnText = count + 's后重新发送'
              if (count == 0) {
                this.phoneValidateCodeBtnText = '发送验证码'
                clearInterval(timer)
              }
            }, 1000)
          })
        }
      })
    },
    updateUserHandle() {
      this.$api
        .post('/user/update', this.userInfoForm)
        .then((res) => {
          if (res.data) {
            if (!this.userInfoForm.avatar) {
              this.msgSuccess('保存成功')
            }
            this.queryUserInfo()
          }
        })
        .catch((err) => {
          console.error('Update failed:', err)
          this.$message.error('更新失败，请重试')
        })
    },
    updateUserPhoneHandle() {
      this.$api.post('/user/update/phone-number', this.userInfoForm).then(() => {
        this.msgSuccess('修改成功')
        this.queryUserInfo()
      })
    },
    openAvatarUpload() {
      this.showUploadAvatar = true
      setTimeout(() => {
        this.showUploadCropper = true
      }, 300)
    },
    closeAvatarUpload() {
      this.showUploadCropper = false
      this.showUploadAvatar = false
    },
    cropUploadSuccess(res) {
      this.showUploadCropper = false
      this.showUploadAvatar = false

      const loading = this.$loading({
        lock: true,
        text: '更新头像中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      const newAvatar = res.data
      this.userInfo.avatar = newAvatar
      this.userInfoForm.avatar = newAvatar
      // 更新avatarKey强制刷新头像
      this.avatarKey = Date.now()
      this.updateUserHandle()

      setTimeout(() => {
        loading.close()
      }, 500)
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  padding: 30px 15px;
  background-color: #f5f7fa;
}

.profile-card {
  width: 100%;
  max-width: 800px;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }
}

.profile-header {
  padding: 20px 25px 0;
}

.profile-title {
  position: relative;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding-left: 12px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 18px;
    background: linear-gradient(135deg, #1890ff, #36cfc9);
    border-radius: 2px;
  }
}

.profile-content {
  display: flex;
  flex-direction: column;
  padding: 0 25px 25px;

  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-start;
  }
}

.profile-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 10px;
  background-color: #f9fafc;

  @media (min-width: 768px) {
    width: 200px;
    margin-right: 30px;
    margin-bottom: 0;
  }
}

.avatar-container {
  position: relative;
  width: 120px;
  height: 120px;
  margin-bottom: 15px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);

    .avatar-overlay {
      opacity: 1;
    }
  }

  .profile-avatar {
    width: 100%;
    height: 100%;
  }

  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: rgba(0, 0, 0, 0.5);
    opacity: 0;
    transition: opacity 0.3s ease;

    i {
      font-size: 24px;
      color: white;
      margin-bottom: 5px;
    }

    span {
      color: white;
      font-size: 12px;
      padding: 3px 8px;
      background: rgba(0, 0, 0, 0.3);
      border-radius: 10px;
    }
  }
}

.profile-username {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.profile-info-section {
  flex: 1;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: #f9fafc;
    border-radius: 6px;
    border-bottom-color: transparent;
    transform: translateX(5px);
  }
}

.info-label {
  width: 80px;
  font-weight: 500;
  color: #606266;
}

.info-value {
  flex: 1;
  color: #303133;
  padding: 0 10px;
}

.action-btn {
  padding: 6px 15px;
  border-radius: 20px;
  font-size: 12px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 10px rgba(24, 144, 255, 0.2);
  }
}

/* Custom Dialog Styles */
:deep(.custom-dialog) {
  border-radius: 10px;
  overflow: hidden;

  .el-dialog__header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    background-color: #f9fafc;
  }

  .el-dialog__body {
    padding: 20px;
  }

  .el-dialog__footer {
    padding: 15px 20px;
    border-top: 1px solid #f0f0f0;
    background-color: #f9fafc;
  }
}

/* Avatar Upload Modal */
.avatar-upload-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
  animation: fadeIn 0.3s ease;
}

.avatar-modal-content {
  width: 500px;
  max-width: 90vw;
  background-color: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease;
}

.avatar-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  h3 {
    margin: 0;
    font-size: 18px;
    color: #303133;
  }

  .close-button {
    background: transparent;
    border: none;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background-color: #f5f7fa;
    }

    i {
      font-size: 20px;
      color: #909399;
    }
  }
}

.avatar-modal-body {
  padding: 20px;

  .current-avatar {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
  }
}

.avatar-modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #f9fafc;
}

.verification-code-input {
  display: flex;

  .el-input {
    margin-right: 10px;
  }

  .el-button {
    white-space: nowrap;
  }
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes slideUp {
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

<style lang="scss">
/* Avatar uploader global styles */
.avatar-modal {
  .vicp-wrap {
    box-shadow: none !important;
    border: none !important;
  }

  .vicp-step1 .vicp-drop-area {
    border: 2px dashed #d9e1ec !important;
    border-radius: 8px !important;
    background-color: #f5f7fa !important;
    transition: all 0.3s ease !important;
    padding: 30px !important;

    &:hover {
      border-color: #409eff !important;
      background-color: #ecf5ff !important;
    }

    .vicp-icon1 {
      color: #409eff !important;
    }

    .vicp-hint {
      color: #606266 !important;
      margin-top: 10px !important;
    }
  }

  .vicp-step2 {
    .vicp-crop {
      background-color: #f5f7fa !important;
      border-radius: 8px !important;
      overflow: hidden !important;
    }

    .vicp-preview {
      .vicp-preview-item {
        border: 2px solid #fff !important;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
        border-radius: 4px !important;
        overflow: hidden !important;

        &:hover {
          border-color: #409eff !important;
        }
      }
    }

    .vicp-range input[type='range'] {
      -webkit-appearance: none !important;
      height: 6px !important;
      background: #e4e7ed !important;
      border-radius: 3px !important;

      &::-webkit-slider-thumb {
        -webkit-appearance: none !important;
        width: 16px !important;
        height: 16px !important;
        background: #409eff !important;
        border-radius: 50% !important;
        border: 2px solid #fff !important;
        box-shadow: 0 0 5px rgba(0, 0, 0, 0.2) !important;
      }
    }

    .vicp-operate a {
      border-radius: 4px !important;

      &:last-child {
        background-color: #409eff !important;

        &:hover {
          background-color: #66b1ff !important;
        }
      }
    }
  }
}

/* Hide original close button */
.vicp-close {
  display: none !important;
}
</style>
