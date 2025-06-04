<template>
  <!-- 添加或修改单模板对话框 -->
  <el-dialog class="t-dialog t-dialog--top" title="保存模板" :visible.sync="open" width="500px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="封面图" prop="coverImg">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleCoverSuccess"
          :before-upload="beforeCoverUpload"
          :data="{ type: 'cover' }"
        >
          <img v-if="form.coverImg" :src="form.coverImg" class="avatar" />
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
      </el-form-item>
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入模板名称" />
      </el-form-item>
      <el-form-item label="模板描述" prop="description">
        <el-input v-model="form.description" type="textarea" placeholder="请输入模板描述" />
      </el-form-item>
      <el-form-item label="模板类型" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择项目类型">
          <el-option v-for="temp in templateTypeList" :key="temp.id" :label="temp.name" :value="temp.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getFormTemplateTypeListRequest, createTemplateRequest } from '@/api/project/template'
import { getToken } from '@/utils/auth'

export default {
  name: 'CreateTemplate',
  props: {
    formKey: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {
        formKey: '',
        coverImg: '',
        name: '',
        description: '',
        categoryId: null,
        scheme: null,
        status: 0
      },
      templateTypeList: [],
      // 表单校验
      rules: {
        coverImg: [{ required: true, message: '请上传封面图', trigger: 'blur' }],
        name: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
        categoryId: [{ required: true, message: '模板类型不能为空', trigger: 'change' }]
      },
      uploadUrl: process.env.VUE_APP_API_ROOT + '/user/file/upload',
      uploadHeaders: {
        token: getToken()
      }
    }
  },
  created() {
    this.getTemplateTypes()
  },
  methods: {
    getTemplateTypes() {
      getFormTemplateTypeListRequest()
        .then((res) => {
          this.templateTypeList = res.data
        })
        .catch((err) => {
          console.error('获取模板类型失败', err)
          this.msgError('获取模板类型失败')
        })
    },
    handleOpen() {
      this.form.formKey = this.formKey
      this.open = true
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        formKey: this.formKey,
        coverImg: null,
        name: null,
        description: null,
        categoryId: null,
        scheme: null,
        status: 0
      }
      this.resetForm('form')
    },
    // 图片上传前的验证
    beforeCoverUpload(file) {
      const isValidType = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isValidType) {
        this.$message.error('上传图片只能是 JPG/PNG/GIF 格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!')
      }
      return isValidType && isLt2M
    },
    // 封面图片上传成功处理
    handleCoverSuccess(res, file) {
      if (res.code === 200) {
        this.form.coverImg = res.data
        this.$message.success('封面图上传成功')
      } else {
        this.$message.error(res.msg || '封面图上传失败')
      }
    },
    // 提交表单
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.form.formKey = this.formKey

          const loading = this.$loading({
            lock: true,
            text: '正在保存模板...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          })

          createTemplateRequest(this.form)
            .then((res) => {
              loading.close()
              this.msgSuccess('保存成功')
              this.open = false
              this.$router.push('/project/template')
            })
            .catch((err) => {
              loading.close()
              console.error('保存模板失败', err)
              this.msgError('保存模板失败')
            })
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;

  &:hover {
    border-color: #409eff;
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }

  .avatar {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
  }
}

.dialog-footer {
  text-align: right;
}
</style>
