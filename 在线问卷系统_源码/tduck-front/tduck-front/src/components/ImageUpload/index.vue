<template>
  <div class="component-upload-image">
    <el-upload
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :on-error="handleUploadError"
      name="file"
      :show-file-list="false"
      :headers="headers"
      style="display: inline-block; vertical-align: top"
    >
      <template v-if="!value">
        <i class="el-icon-plus"></i>
      </template>
      <div v-else class="image">
        <el-image :src="value" :style="`width:150px;height:150px;`" fit="fill">
          <div slot="error" class="image-slot">
            <i class="el-icon-picture-outline"></i>
          </div>
        </el-image>
        <div class="mask">
          <div class="actions">
            <span title="预览" @click.stop="dialogVisible = true">
              <i class="el-icon-zoom-in" />
            </span>
            <span title="移除" @click.stop="removeImage">
              <i class="el-icon-delete" />
            </span>
          </div>
        </div>
      </div>
    </el-upload>
    <el-dialog :visible.sync="dialogVisible" title="预览" width="800" append-to-body>
      <img :src="value" style="display: block; max-width: 100%; margin: 0 auto" />
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'

export default {
  name: 'ImageUpload',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      dialogVisible: false,
      uploadImgUrl: process.env.VUE_APP_API_ROOT + '/common/upload', // 上传的图片服务器地址
      headers: {
        Authorization: 'Bearer ' + getToken()
      }
    }
  },
  watch: {},
  methods: {
    removeImage() {
      this.$emit('input', '')
    },
    handleUploadSuccess(res) {
      this.$emit('input', res.data)
      this.loading.close()
    },
    handleBeforeUpload() {
      this.loading = this.$loading({
        lock: true,
        text: '上传中',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    },
    handleUploadError() {
      this.$message({
        type: 'error',
        message: '上传失败'
      })
      this.loading.close()
    }
  }
}
</script>

<style scoped lang="scss">
.image {
  position: relative;

  .mask {
    opacity: 0;
    position: absolute;
    top: 0;
    bottom: 0;
    width: 100%;
    height: 150px;
    background-color: rgba(0, 0, 0, 0.5);
    transition: all 0.3s;
  }

  &:hover .mask {
    opacity: 1;
  }
}
</style>
