<template>
  <div class="project-card-container">
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
              <p class="success-title">恭喜您，发布成功！</p>
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
    <el-row v-if="projectList.length" :gutter="30">
      <el-col v-for="p in projectList" :key="p.id" :span="6">
        <el-card :key="p.id" :body-style="{ padding: '0px', position: 'static' }" class="move">
          <div class="body">
            <el-row align="middle" justify="center" type="flex">
              <el-col :span="20">
                <span class="title">
                  {{ p.textName }}
                </span>
              </el-col>

              <el-col :span="4">
                <span class="form-type" :style="{ backgroundColor: getType(p.type).color }">{{
                  getType(p.type).name
                }}</span>
              </el-col>
            </el-row>
            <div class="status">
              <span :style="getStatusColorStyle(p.status)" class="status-tag" />
              <span class="status-name" :style="{ color: getFormStatusName(p.status).color }">{{
                getFormStatusName(p.status).statusName
              }}</span>
            </div>
            <div class="card-time-div">
              <span class="card-time">创建时间：{{ p.createTime | formatDate }}</span>
            </div>
          </div>

          <div class="btns">
            <div class="split-line" />
            <!-- 第一行按钮 - 功能按钮 -->
            <div class="btn-row action-btn-row">
              <el-button type="primary" size="mini" @click="toProjectHandle(p, 'editor')"> 编辑 </el-button>
              <el-button type="primary" size="mini" @click="toProjectHandle(p, 'data')"> 数据 </el-button>
              <el-button type="primary" size="mini" @click.native="toProjectHandle(p, 'statistics')"> 统计 </el-button>
            </div>
            <!-- 第二行按钮 - 操作按钮 -->
            <div class="btn-row action-btn-row">
              <el-button v-if="p.status != 2" type="primary" size="mini" @click="publishProject(p.formKey)">
                发布
              </el-button>
              <el-button v-if="p.status == 2" type="warning" size="mini" @click.native="stopProject(p.formKey)">
                停止
              </el-button>
              <el-button v-if="p.status == 2" type="success" size="mini" @click="showShareDialog(p)"> 分享 </el-button>
              <el-button type="danger" size="mini" @click="deleteFrom(p.formKey)"> 删除 </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <div v-if="projectList.length == 0" class="empty-container">
      <data-empty icon="el-icon-document" desc="暂无项目数据">
        <el-button type="primary" icon="el-icon-plus" @click="$emit('create')">创建新表单</el-button>
      </data-empty>
    </div>
  </div>
</template>

<script>
import mixin from './mixin'
import { publishFormRequest } from '@/api/project/publish'
import VueQr from 'vue-qr'
import { getSystemInfoConfig } from '@/api/mange/config'

export default {
  name: 'FormCardList',
  components: {
    VueQr
  },
  mixins: [mixin],
  props: {
    projectList: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 我的表单页还是协作页区分 默认为表单页 协作页某些内容不显示
    type: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      visible: false,
      shareDialogVisible: false,
      shareLink: '',
      currentForm: null,
      qrCodeUrl: '',
      baseUrl: ''
    }
  },
  computed: {
    getStatusColorStyle() {
      return function (code) {
        let color = this.projectStatusList.find((item) => item.code == code).color
        return {
          backgroundColor: color,
          borderColor: color
        }
      }
    },
    getFormStatusName() {
      return function (code) {
        let name = this.projectStatusList.find((item) => item.code == code).name
        let statusColor = this.projectStatusList.find((item) => item.code == code).color
        return {
          statusName: name,
          color: statusColor
        }
      }
    }
  },
  created() {
    this.getSystemConfig()
  },
  methods: {
    deleteFrom(key) {
      this.$confirm('此操作将删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.logicDeleteForm(key)
        })
        .catch(() => {})
    },
    publishProject(formKey) {
      publishFormRequest({ formKey: formKey }).then(() => {
        this.msgSuccess('发布成功')
        this.$emit('refresh')
      })
    },
    endEvent(event) {
      console.log(event)
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
.project-card-container {
  //width: 950px;
  margin: 20px auto;

  .el-card {
    border-radius: 12px;
    position: relative;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    border: 1px solid #f0f0f0;
    min-height: 280px;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      border-color: #e6f7ff;
    }

    .body {
      padding: 15px;
      height: 180px;
      width: 100%;
      text-align: center;
    }
  }

  .el-col {
    margin-bottom: 20px;
    text-align: left;

    .form-type {
      color: white;
      font-size: 11px;
      padding: 3px 5px;
      border-radius: 5px;
    }

    .status {
      text-align: left;
    }

    .status-tag {
      display: inline-block;
      width: 7px;
      height: 7px;
      line-height: 20px;
      background-color: rgba(0, 110, 255, 100);
      text-align: center;
      border: 1px solid rgba(0, 110, 255, 100);
      border-radius: 20px;
      margin-bottom: 0px;
      margin-left: 10px;
    }

    .status-name {
      margin-left: 6px;
      font-size: 11px;
    }

    .title {
      color: rgb(68, 66, 66);
      font-size: 16px;
      text-align: left;
      font-weight: 500;
      margin-left: 10px;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      display: block;
      max-width: 100%;
      line-height: 27px;
      vertical-align: bottom;
    }

    .card-time-div {
      text-align: left;
      margin-top: 5px;
    }

    .card-time {
      color: rgba(144, 147, 153, 100);
      font-size: 12px;
      line-height: 20px;
      text-align: center;
      margin-left: 10px;
    }

    .time {
      color: rgba(144, 147, 153, 100);
      font-size: 12px;
      line-height: 20px;
      text-align: center;
      margin: 0;
    }

    img {
      width: 143px;
      height: 121px;
    }

    .folder-img {
      width: 60px;
      height: 59px;
      margin: 0 auto;
      display: inline-block;
    }

    .folder-img + .time {
      margin-top: 20px;
    }

    .btns {
      color: rgba(144, 147, 153, 100);
      position: absolute;
      width: 100%;
      margin: 0;
      padding: 0;
      bottom: 0;
      display: block;
      border: none;
      text-align: center;
    }

    .btn-row {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 0 15px;
      margin: 5px 0;

      &.action-btn-row {
        justify-content: center;
        gap: 10px;
        margin-top: 8px;
        margin-bottom: 8px;
      }
    }

    .el-button--mini {
      padding: 7px 15px;
      font-size: 12px;
      border-radius: 4px;

      &.el-button--primary {
        background-color: #409eff;
        border-color: #409eff;

        &:hover,
        &:focus {
          background-color: #66b1ff;
          border-color: #66b1ff;
        }
      }

      &.el-button--success {
        background-color: #67c23a;
        border-color: #67c23a;

        &:hover,
        &:focus {
          background-color: #85ce61;
          border-color: #85ce61;
        }
      }

      &.el-button--warning {
        background-color: #e6a23c;
        border-color: #e6a23c;

        &:hover,
        &:focus {
          background-color: #ebb563;
          border-color: #ebb563;
        }
      }

      &.el-button--danger {
        background-color: #f56c6c;
        border-color: #f56c6c;

        &:hover,
        &:focus {
          background-color: #f78989;
          border-color: #f78989;
        }
      }
    }

    // 不再需要文本按钮样式

    // 按钮样式已在上面定义

    .split-line {
      text-align: center;
      margin-top: 130px;
      width: 100%;
      border-bottom: 1px solid #e7e7e7;
    }

    &:hover .btns {
      display: block;
    }
  }
}

// 已在上面设置了卡片样式，这里不需要再覆盖

.empty-container {
  padding: 40px 0;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
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
</style>
