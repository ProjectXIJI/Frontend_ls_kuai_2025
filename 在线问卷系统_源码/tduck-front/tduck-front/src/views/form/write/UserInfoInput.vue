<template>
  <div class="user-info-floating-card">
    <!-- 表单元信息：创建时间、发布人和答题人数 -->
    <div class="form-meta-info" v-if="formInfo">
      <div class="meta-item">
        <i class="el-icon-user"></i>
        <span
          >发布人:
          <template v-if="formInfo.formCreator && formInfo.formCreator.userId === 1">
            <el-tag size="mini" type="warning">管理员</el-tag> {{ formInfo.formCreator.userName || '未知用户' }}
          </template>
          <template v-else-if="formInfo.formCreator">
            <el-tag size="mini" type="primary">用户</el-tag> {{ formInfo.formCreator.userName || '未知用户' }}
          </template>
          <template v-else> 未知用户 </template>
        </span>
      </div>
      <div class="meta-item">
        <i class="el-icon-time"></i>
        <span>创建时间: {{ formatDate(formInfo.formCreateTime) }}</span>
      </div>
      <div class="meta-item">
        <i class="el-icon-data-analysis"></i>
        <span>已答题: {{ formInfo.formStats.completeCount || 0 }}人</span>
      </div>
    </div>

    <div class="divider"></div>

    <div class="card-header">
      <h3>填写人信息</h3>
    </div>
    <el-form :model="userInfo" :rules="rules" ref="userInfoForm" label-width="60px" size="small">
      <el-form-item label="姓名" prop="userName">
        <el-input v-model="userInfo.userName" placeholder="请输入您的姓名"></el-input>
      </el-form-item>
      <el-form-item label="邮箱" prop="userEmail">
        <el-input v-model="userInfo.userEmail" placeholder="请输入您的邮箱"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'UserInfoInput',
  props: {
    formInfo: {
      type: Object,
      default: () => ({
        formCreator: null,
        formCreateTime: '',
        formStats: {
          completeCount: 0
        }
      })
    }
  },
  data() {
    return {
      userInfo: {
        userName: '',
        userEmail: ''
      },
      rules: {
        userName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        userEmail: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    validate() {
      return new Promise((resolve, reject) => {
        this.$refs.userInfoForm.validate((valid) => {
          if (valid) {
            resolve(this.userInfo)
          } else {
            reject('请填写正确的用户信息')
          }
        })
      })
    },
    resetForm() {
      this.$refs.userInfoForm.resetFields()
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
    }
  }
}
</script>

<style lang="scss" scoped>
.user-info-floating-card {
  position: fixed;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  width: 280px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  padding: 15px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  }

  .form-meta-info {
    display: flex;
    flex-direction: column;
    margin-bottom: 15px;

    .meta-item {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      font-size: 13px;
      color: #606266;

      i {
        margin-right: 5px;
      }

      span {
        display: flex;
        align-items: center;
        flex-wrap: wrap;

        .el-tag {
          margin-right: 5px;
        }
      }
    }
  }

  .divider {
    height: 1px;
    background-color: #f0f0f0;
    margin: 15px 0;
  }

  .card-header {
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #f0f0f0;

    h3 {
      font-size: 16px;
      color: #333;
      margin: 0;
      font-weight: 500;
    }
  }

  ::v-deep .el-form-item__label {
    font-size: 14px;
    color: #606266;
  }

  ::v-deep .el-form-item {
    margin-bottom: 15px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  // 响应式调整
  @media (max-width: 1200px) {
    position: static;
    transform: none;
    width: 100%;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
}
</style>
