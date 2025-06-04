<template>
  <div>
    <el-card class="publish-card">
      <div slot="header">
        <span>发布</span>
        <span class="desc-text ml-10">公开发布后，所有访问的用户都能够填写数据</span>
      </div>
      <p class="desc-text m20">开启后，会生成表单填写链接，用户即可填写表单</p>
      <el-switch
        v-model="publishStatus"
        active-color="#13ce66"
        inactive-color="#ff4949"
        @change="handlePublishChange"
      />

      <div v-if="publishStatus" class="url-container">
        <url-operate :link-url="createDataLink" class="url-op" />
      </div>

      <div v-if="publishStatus" class="qr-container">
        <vue-qr v-if="createDataLink" :size="150" :text="createDataLink" :callback="qrCodeGenSuccess" />
        <el-button v-if="qrCodeUrl" size="mini" type="primary" icon="el-icon-download" @click="downloadQrCode">
          下载二维码
        </el-button>
      </div>

      <el-divider />
    </el-card>
  </div>
</template>

<script>
import UrlOperate from './UrlOperate'
import VueQr from 'vue-qr'
import { getFormStatusRequest, publishFormRequest, stopPublishFormRequest } from '@/api/project/publish'
import { getSystemInfoConfig } from '@/api/mange/config'

export default {
  name: 'FormPublish',
  components: {
    UrlOperate,
    VueQr
  },
  data() {
    return {
      publishStatus: false,
      createDataLink: '',
      formKey: '',
      qrCodeUrl: null,
      loading: false
    }
  },
  created() {
    this.formKey = this.$route.query.key
    if (!this.formKey) {
      this.msgError('表单参数错误')
      return
    }
    this.getSystemConfig()
    this.getProjectStatus()
  },
  methods: {
    getSystemConfig() {
      this.loading = true
      getSystemInfoConfig()
        .then((res) => {
          if (res.data) {
            const config = JSON.parse(res.data)
            if (config.webBaseUrl) {
              // 使用系统配置的域名
              this.createDataLink = `${config.webBaseUrl}/s/${this.formKey}`
            } else {
              // 如果没有配置域名，则使用当前域名
              let url = window.location.protocol + '//' + window.location.host
              this.createDataLink = `${url}/s/${this.formKey}`
            }
          } else {
            // 如果没有获取到配置，则使用当前域名
            let url = window.location.protocol + '//' + window.location.host
            this.createDataLink = `${url}/s/${this.formKey}`
          }
          this.loading = false
        })
        .catch(() => {
          // 如果获取配置失败，则使用当前域名
          let url = window.location.protocol + '//' + window.location.host
          this.createDataLink = `${url}/s/${this.formKey}`
          this.loading = false
        })
    },
    getProjectStatus() {
      getFormStatusRequest(this.formKey)
        .then((res) => {
          if (res.data.status == 2) {
            this.publishStatus = true
          } else {
            this.publishStatus = false
          }
        })
        .catch((err) => {
          console.error('获取表单状态失败', err)
          this.msgError('获取表单状态失败')
        })
    },
    handlePublishChange(val) {
      if (val) {
        this.publishProject()
      } else {
        this.stopPublishProject()
      }
    },
    publishProject() {
      const loading = this.$loading({
        lock: true,
        text: '正在发布...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      publishFormRequest({ key: this.formKey })
        .then(() => {
          this.publishStatus = true
          this.msgSuccess('发布成功')
          loading.close()
        })
        .catch((err) => {
          console.error('发布失败', err)
          this.msgError('发布失败')
          loading.close()
        })
    },
    stopPublishProject() {
      const loading = this.$loading({
        lock: true,
        text: '正在停止发布...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      stopPublishFormRequest({ key: this.formKey })
        .then((res) => {
          if (res.data) {
            this.msgSuccess('停止发布成功')
            this.getProjectStatus()
          }
          loading.close()
        })
        .catch((err) => {
          console.error('停止发布失败', err)
          this.msgError('停止发布失败')
          loading.close()
        })
    },
    qrCodeGenSuccess(dataUrl) {
      this.qrCodeUrl = dataUrl
    },
    downloadQrCode() {
      this.downloadFile('form_qrcode.png', this.qrCodeUrl)
    },
    downloadFile(fileName, content) {
      let aLink = document.createElement('a')
      let blob = this.base64ToBlob(content)
      aLink.download = fileName
      aLink.href = URL.createObjectURL(blob)
      aLink.click()
    },
    // base64转blob
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
    }
  }
}
</script>

<style lang="scss" scoped>
.publish-card {
  width: 900px;
  min-height: 300px;
  margin: 20px auto;
  padding: 20px;

  .desc-text {
    color: #909399;
    font-size: 14px;
  }

  .m20 {
    margin: 20px 0;
  }

  .ml-10 {
    margin-left: 10px;
  }
}

.url-container {
  margin: 30px 0;
}

.url-op {
  margin-top: 20px;
}

.qr-container {
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;

  .el-button {
    margin-top: 10px;
  }
}
</style>
