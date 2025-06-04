<template>
  <div class="preview-container">
    <el-tabs v-if="formConfig.formKey" type="card">
      <el-tab-pane>
        <span slot="label"
          ><i class="el-icon-mobile" />
          手机
        </span>
        <div class="preview-layer">
          <div class="preview-bg" />
          <div class="preview-phone">
            <iframe
              id="preview-html"
              :src="mobilePreviewUrl"
              class="preview-html"
              frameborder="0"
              name="preview-html"
              scrolling="auto"
            />
          </div>
        </div>
        <div v-if="mobilePreviewUrl && previewQrcode" class="qrcode-view">
          <p style="font-weight: bold; font-size: 16px">手机扫码预览</p>
          <p class="tips-text">* 预览仅查看效果，无法提交数据</p>
          <p>
            <vue-qr
              v-if="mobilePreviewUrl && previewQrcode"
              :size="134"
              style="border-radius: 10px"
              :text="mobilePreviewUrl"
            />
          </p>
        </div>
      </el-tab-pane>
      <el-tab-pane>
        <span slot="label"
          ><i class="el-icon-monitor" />
          电脑
        </span>
        <el-scrollbar style="height: 77vh; overflow-x: hidden !important">
          <biz-project-form v-if="formConfig.formKey" :form-config="formConfig" class="questionnaire-form" />
        </el-scrollbar>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { BizProjectForm } from 'tduck-form-generator'
import 'tduck-form-generator/dist/TduckForm.css'
import '@/assets/styles/form/questionnaire.scss'
import VueQr from 'vue-qr'
import mixin from '../TduckFormMixin'
import { getSystemInfoConfig } from '@/api/mange/config'

export default {
  name: 'PreView',
  components: {
    BizProjectForm,
    VueQr
  },
  mixins: [mixin],
  props: {
    previewQrcode: null
  },
  data() {
    return {
      formKey: null,
      mobilePreviewUrl: '',
      formConfig: {
        formKey: '',
        showBtns: true,
        showItemNumber: true // 显示题目序号
      }
    }
  },
  mounted() {
    this.formKey = this.$route.query.key
    this.getSystemConfig()
    this.$set(this.formConfig, 'formKey', this.formKey)
  },
  methods: {
    getSystemConfig() {
      getSystemInfoConfig()
        .then((res) => {
          if (res.data) {
            const config = JSON.parse(res.data)
            if (config.webBaseUrl) {
              // 使用系统配置的域名
              this.mobilePreviewUrl = `${config.webBaseUrl}/project/form/view?key=${this.formKey}`
            } else {
              // 如果没有配置域名，则使用当前域名
              let url = window.location.protocol + '//' + window.location.host
              this.mobilePreviewUrl = `${url}/project/form/view?key=${this.formKey}`
            }
          } else {
            // 如果没有获取到配置，则使用当前域名
            let url = window.location.protocol + '//' + window.location.host
            this.mobilePreviewUrl = `${url}/project/form/view?key=${this.formKey}`
          }
        })
        .catch(() => {
          // 如果获取配置失败，则使用当前域名
          let url = window.location.protocol + '//' + window.location.host
          this.mobilePreviewUrl = `${url}/project/form/view?key=${this.formKey}`
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.tips-text {
  margin-top: 10px;
  margin-bottom: 10px;
}
.preview-container {
  margin: 0;
  padding-top: 30px;
  height: 100vh;
  overflow: hidden !important;
  background-color: var(--color-bg);
}

::v-deep .el-tabs--card > .el-tabs__header .el-tabs__nav {
  border: 1px solid #e4e7ed !important;
  border-radius: 10px;
  background-color: white;
}

::v-deep .el-tabs__header {
  width: 300px;
  margin: 0 auto;
  border: none;
}

::v-deep .el-dialog__body {
  max-height: calc(100vh - 200px) !important;
}

::v-deep .el-tabs--card > .el-tabs__header .el-tabs__item {
  background-color: transparent;
  //border: 1px solid white;
}

::v-deep .project-form {
  padding: 20px;
}

::v-deep .questionnaire-form {
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
}

::v-deep .project-form-wrapper {
  background-color: transparent;
}

div.preview-layer {
  width: 500px;
  height: 100%;
  margin: 10px auto;
  right: 0;
  text-align: center;
}

div.preview-layer .preview-bg {
  width: 500px;
  height: 100%;
  margin: 20px auto;
  z-index: 999;
  opacity: 0.7;
}

div.preview-layer .preview-phone {
  width: 372px;
  height: 744px;
  background: url('~@/assets/images/appearset_bgc_big.png');
  background-size: 372px 744px;
  z-index: 1000;
}

.qrcode-view {
  position: absolute;
  top: 20px;
  right: 260px;

  p {
    text-align: center;
    font-size: 12px;
    color: #303133;
  }
}

.preview-html {
  width: 345px !important;
  height: 568px !important;
  margin: 74px 0 0;
  border-radius: 5px;
  outline: none;
  background-color: #fff;
  border-width: 2px;
  border-style: inset;
  border-color: initial;
  border-image: initial;
  border-top-width: 0;
  border-right-width: 0;
  border-bottom-width: 0;
  border-left-width: 0;
}
</style>
