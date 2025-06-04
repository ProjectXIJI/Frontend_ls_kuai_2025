<template>
  <div class="project-square-container">
    <!-- 顶部导航栏 - 已注释掉
    <div class="top-nav">
      <div class="logo">
        <img src="~@/assets/images/custom-logo.svg" @click="goToHome" />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="goToLogin" v-if="!isLoggedIn">登录</el-button>
        <el-button type="primary" @click="goToHome" v-if="isLoggedIn">我的项目</el-button>
      </div>
    </div>
    -->

    <!-- 分享弹窗 -->
    <el-dialog
      title="分享问卷"
      :visible.sync="shareDialogVisible"
      width="600px"
      :before-close="closeShareDialog"
      center
    >
      <div class="share-dialog-content">
        <el-row :gutter="10" align="middle" type="flex">
          <el-col :span="12">
            <div>
              <vue-qr v-if="shareLink" :callback="qrCodeGenSuccess" :size="194" :text="shareLink" />
            </div>
            <div style="text-align: center; margin-top: 10px">
              <el-link type="primary" @click="downloadQrCode"> 下载分享二维码 </el-link>
            </div>
          </el-col>
          <el-col :span="12">
            <div style="display: flex; justify-content: center">
              <div class="icon-view">
                <i class="el-icon-check success-icon" />
              </div>
            </div>
            <div>
              <p class="success-title">欢迎分享</p>
            </div>
            <div>
              <p class="link-text">{{ shareLink }}</p>
            </div>
            <el-row>
              <el-col :span="12" :offset="6">
                <el-button
                  v-clipboard:copy="shareLink"
                  v-clipboard:error="
                    () => {
                      this.msgError('复制失败')
                    }
                  "
                  v-clipboard:success="
                    () => {
                      this.msgSuccess('复制成功')
                    }
                  "
                  type="primary"
                >
                  复制链接
                </el-button>
              </el-col>
            </el-row>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <div class="header">
      <h2>问卷广场</h2>
      <p>这里展示了所有正在收集中的问卷，选择一个开始填写吧！</p>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="formList.length === 0" class="empty-container">
      <el-empty description="暂无可填写的问卷"></el-empty>
    </div>

    <el-row :gutter="30" v-else>
      <el-col v-for="form in formList" :key="form.id" :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="form-card" shadow="hover">
          <div class="form-title">{{ form.textName || form.name }}</div>
          <div class="form-desc">{{ form.description || '暂无描述' }}</div>
          <div class="form-meta">
            <div class="meta-item">
              <i class="el-icon-user"></i>
              <span
                >发布人:
                <template v-if="form.userId === 1">
                  <el-tag size="mini" type="warning">管理员</el-tag> {{ form.userName || '未知用户' }}
                </template>
                <template v-else>
                  <el-tag size="mini" type="primary">用户</el-tag> {{ form.userName || '未知用户' }}
                </template>
              </span>
            </div>
            <div class="meta-item">
              <i class="el-icon-time"></i>
              <span>创建时间: {{ formatDate(form.createTime) }}</span>
            </div>
          </div>
          <div class="form-footer">
            <div class="btn-group">
              <el-button
                :type="form.isAnswered ? 'info' : 'primary'"
                size="small"
                :icon="form.isAnswered ? 'el-icon-check' : 'el-icon-edit-outline'"
                :disabled="form.isAnswered"
                @click="goToWriteForm(form.formKey)"
              >
                {{ form.isAnswered ? '已填写' : '开始填写' }}
              </el-button>
              <el-button type="success" size="small" icon="el-icon-share" @click="showShareDialog(form)"
                >分享</el-button
              >
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page.sync="currentPage"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import { getRequest } from '@/api/baseRequest'
import Cookies from 'js-cookie'
import { mapGetters } from 'vuex'
import VueQr from 'vue-qr'
import { getSystemInfoConfig } from '@/api/mange/config'
import { checkUserAnsweredRequest } from '@/api/project/data'

export default {
  name: 'ProjectSquare',
  components: {
    VueQr
  },
  data() {
    return {
      formList: [],
      total: 0,
      pageSize: 12,
      currentPage: 1,
      loading: true,
      isLoggedIn: false,
      shareDialogVisible: false,
      shareLink: '',
      currentForm: null,
      qrCodeUrl: '',
      baseUrl: '',
      answeredForms: {} // 存储用户已回答的问卷
    }
  },
  computed: {
    ...mapGetters({
      getUserInfo: 'user/userInfo'
    })
  },
  created() {
    this.checkLoginStatus()
    this.fetchFormList()
    this.getSystemConfig()
    // 添加调试信息
    console.log('用户登录状态:', this.isLoggedIn)
  },

  mounted() {
    // 检查已回答的问卷（无论用户是否登录）
    this.checkAnsweredForms()
  },
  methods: {
    checkLoginStatus() {
      // 从Cookie和localStorage中获取token
      const cookieToken = Cookies.get('token')
      const localStorageToken = localStorage.getItem('token')
      // 从Vuex store中获取token
      const storeToken = this.$store.getters['user/isLogin']

      // 只要有一个token存在，就认为用户已登录
      this.isLoggedIn = !!cookieToken || !!localStorageToken || !!storeToken

      console.log(
        '检测登录状态 - Cookie token:',
        !!cookieToken,
        'LocalStorage token:',
        !!localStorageToken,
        'Store token:',
        !!storeToken,
        '最终状态:',
        this.isLoggedIn
      )
    },
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
    fetchFormList() {
      this.loading = true
      getRequest('/user/form/public/list', {
        page: this.currentPage,
        size: this.pageSize
      })
        .then((res) => {
          if (res.code === 200) {
            this.formList = res.data.records || []
            // 确保每个表单项都有userId字段
            this.formList.forEach((form) => {
              if (!form.userId && form.userId !== 0) {
                // 如果没有userId字段，根据userAdmin字段设置默认值
                form.userId = form.userAdmin ? 1 : 2
              }
              // 初始化已答题状态为false
              form.isAnswered = this.answeredForms[form.formKey] || false
            })
            this.total = res.data.total || 0

            // 检查已回答的问卷（无论用户是否登录）
            this.checkAnsweredForms()
          } else {
            this.$message.error('获取问卷列表失败')
            this.formList = []
            this.total = 0
          }
        })
        .catch((err) => {
          console.error('获取问卷列表出错:', err)
          this.$message.error('获取问卷列表出错')
          this.formList = []
          this.total = 0
        })
        .finally(() => {
          this.loading = false
        })
    },

    // 检查用户已回答的问卷
    checkAnsweredForms() {
      if (this.formList.length === 0) return

      // 使用本地存储检查用户是否已回答问卷
      // 这是一个临时解决方案，直到后端API可用
      try {
        // 从本地存储获取已回答的问卷
        const answeredFormsStr = localStorage.getItem('answeredForms')
        if (answeredFormsStr) {
          const answeredForms = JSON.parse(answeredFormsStr)

          // 更新状态
          this.formList.forEach((form) => {
            if (answeredForms[form.formKey]) {
              this.$set(this.answeredForms, form.formKey, true)
              const index = this.formList.findIndex((item) => item.formKey === form.formKey)
              if (index !== -1) {
                this.$set(this.formList[index], 'isAnswered', true)
              }
            }
          })
        }
      } catch (error) {
        console.error('获取本地存储的已回答问卷数据出错:', error)
      }

      /* 暂时注释掉API调用，直到后端实现该接口
      // 对每个问卷检查用户是否已回答
      this.formList.forEach(form => {
        checkUserAnsweredRequest(form.formKey)
          .then(res => {
            if (res.code === 200 && res.data === true) {
              // 更新已回答状态
              this.$set(this.answeredForms, form.formKey, true)
              // 更新表单列表中的状态
              const index = this.formList.findIndex(item => item.formKey === form.formKey)
              if (index !== -1) {
                this.$set(this.formList[index], 'isAnswered', true)
              }
            }
          })
          .catch(err => {
            console.error('检查问卷回答状态出错:', err)
          })
      })
      */
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchFormList()
    },
    goToWriteForm(formKey) {
      // 查找当前表单
      const form = this.formList.find((item) => item.formKey === formKey)

      // 如果表单已经填写过，则显示提示信息而不跳转
      if (form && form.isAnswered) {
        this.$message({
          message: '您已经填写过此问卷，无需重复填写',
          type: 'info'
        })
        return
      }

      // 重新检查登录状态，确保使用最新的状态
      this.checkLoginStatus()
      console.log('点击开始填写 - 用户登录状态:', this.isLoggedIn, '表单Key:', formKey)

      // 根据用户登录状态决定使用哪个路由
      if (this.isLoggedIn) {
        console.log('使用登录用户路由: /form/' + formKey)
        this.$router.push(`/form/${formKey}`)
      } else {
        console.log('使用未登录用户路由: /s/' + formKey)
        this.$router.push(`/s/${formKey}`)
      }
    },
    goToHome() {
      if (this.isLoggedIn) {
        this.$router.push('/home')
      } else {
        this.$router.push('/')
      }
    },
    goToLogin() {
      this.$router.push('/login')
    },

    // 分享相关方法
    getSystemConfig() {
      getSystemInfoConfig().then((res) => {
        if (res.data) {
          this.baseUrl = res.data.baseUrl || window.location.origin
        }
      })
    },

    showShareDialog(form) {
      this.currentForm = form
      this.shareLink = `${this.baseUrl}/s/${form.formKey}`
      this.shareDialogVisible = true
    },

    closeShareDialog() {
      this.shareDialogVisible = false
      this.currentForm = null
      this.shareLink = ''
      this.qrCodeUrl = ''
    },

    qrCodeGenSuccess(dataUrl) {
      this.qrCodeUrl = dataUrl
    },

    downloadQrCode() {
      if (!this.qrCodeUrl) {
        this.$message.error('二维码生成失败，请稍后再试')
        return
      }
      this.downloadFile('form_qrcode.png', this.qrCodeUrl)
    },

    downloadFile(fileName, content) {
      let aLink = document.createElement('a')
      let blob = this.base64ToBlob(content)
      aLink.download = fileName
      aLink.href = URL.createObjectURL(blob)
      aLink.click()
    },

    base64ToBlob(code) {
      let parts = code.split(';base64,')
      let contentType = parts[0].split(':')[1]
      let raw = window.atob(parts[1])
      let rawLength = raw.length
      let uInt8Array = new Uint8Array(rawLength)
      for (let i = 0; i < rawLength; ++i) {
        uInt8Array[i] = raw.charCodeAt(i)
      }
      return new Blob([uInt8Array], { type: contentType })
    },

    msgSuccess(msg) {
      this.$message.success(msg)
    },

    msgError(msg) {
      this.$message.error(msg)
    }
  }
}
</script>

<style lang="scss" scoped>
.project-square-container {
  padding: 20px;

  /* 顶部导航栏样式 - 已注释掉
  .top-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    margin-bottom: 20px;
    border-bottom: 1px solid #eaeaea;

    .logo {
      img {
        height: 40px;
        cursor: pointer;
      }
    }

    .nav-actions {
      display: flex;
      gap: 10px;
    }
  }
  */

  .header {
    text-align: center;
    margin-bottom: 30px;

    h2 {
      font-size: 28px;
      color: #303133;
      margin-bottom: 10px;
    }

    p {
      font-size: 16px;
      color: #606266;
    }
  }

  .loading-container,
  .empty-container {
    padding: 40px 0;
    text-align: center;
  }

  .form-card {
    height: 280px;
    margin-bottom: 20px;
    display: flex;
    flex-direction: column;
    border-radius: 8px;
    transition: all 0.3s ease;
    padding: 15px;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    }

    .form-title {
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 10px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .form-desc {
      font-size: 14px;
      color: #606266;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .form-meta {
      margin-top: 15px;
      font-size: 13px;
      color: #606266;

      .meta-item {
        display: flex;
        align-items: center;
        margin-bottom: 5px;

        i {
          margin-right: 5px;
        }

        span {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          display: flex;
          align-items: center;

          .el-tag {
            margin-right: 5px;
            font-weight: bold;
          }
        }
      }
    }

    .form-footer {
      margin-top: 20px;
      text-align: center;

      .btn-group {
        display: flex;
        justify-content: center;
        gap: 10px;
      }

      .el-button {
        padding: 8px 20px;
        font-weight: 500;

        &:hover:not([disabled]) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
        }

        &[disabled] {
          cursor: not-allowed;
          opacity: 0.7;
        }
      }
    }
  }

  .pagination-container {
    text-align: center;
    margin-top: 30px;
  }

  // 分享弹窗样式
  .share-dialog-content {
    padding: 20px 0;

    .icon-view {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background-color: #67c23a;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 15px;

      .success-icon {
        color: #fff;
        font-size: 30px;
      }
    }

    .success-title {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
      text-align: center;
      margin-bottom: 15px;
    }

    .link-text {
      font-size: 14px;
      color: #606266;
      text-align: center;
      word-break: break-all;
      margin-bottom: 20px;
      padding: 0 20px;
    }
  }
}
</style>
