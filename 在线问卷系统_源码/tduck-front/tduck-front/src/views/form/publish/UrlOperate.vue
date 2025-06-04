<template>
  <el-row class="url-operate-container">
    <el-col :span="13">
      <el-input :disabled="true" :value="linkUrl" size="medium" />
    </el-col>
    <el-col :span="11">
      <el-button-group class="button-group">
        <el-button
          v-clipboard:copy="linkUrl"
          v-clipboard:error="onCopyError"
          v-clipboard:success="onCopySuccess"
          icon="el-icon-copy-document"
          size="medium"
          type="primary"
        >
          复 制
        </el-button>

        <el-button icon="el-icon-s-promotion" size="medium" type="primary" @click="handleOpen"> 打 开 </el-button>

        <el-popover placement="right" trigger="click" width="200">
          <vue-qr v-if="linkUrl" :size="194" :callback="qrCodeGenSuccess" :text="linkUrl" />
          <el-button class="download-btn" size="mini" type="primary" @click="handleDownloadQrCode">
            下载二维码
          </el-button>
          <el-button slot="reference" size="medium" type="primary">
            <i class="el-icon-picture-outline"></i>
          </el-button>
        </el-popover>
      </el-button-group>
    </el-col>
  </el-row>
</template>

<script>
import VueQr from 'vue-qr'

export default {
  name: 'UrlOperate',
  components: {
    VueQr
  },
  props: {
    linkUrl: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      qrCodeFileUrl: null
    }
  },
  methods: {
    onCopySuccess() {
      this.$message({
        message: '复制成功',
        type: 'success'
      })
    },
    onCopyError() {
      this.$message({
        message: '复制失败',
        type: 'error'
      })
    },
    handleOpen() {
      if (!this.linkUrl) {
        this.$message({
          message: '链接地址为空',
          type: 'warning'
        })
        return
      }
      window.open(this.linkUrl, '_blank')
    },
    qrCodeGenSuccess(dataUrl) {
      this.qrCodeFileUrl = dataUrl
    },
    handleDownloadQrCode() {
      if (!this.qrCodeFileUrl) {
        this.$message({
          message: '二维码生成失败',
          type: 'warning'
        })
        return
      }
      this.downloadFile('qrcode.png', this.qrCodeFileUrl)
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
.url-operate-container {
  display: flex;
  align-items: center;
  margin: 15px 0;

  .button-group {
    margin-left: 10px;
    display: flex;

    .el-button {
      margin-right: 5px;
    }
  }
}

.download-btn {
  margin-top: 10px;
  width: 100%;
}
</style>
