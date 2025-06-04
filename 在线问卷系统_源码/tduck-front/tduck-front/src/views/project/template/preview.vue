<template>
  <div class="preview-container fade-in">
    <!-- Header with navigation and actions -->
    <div class="preview-header">
      <div class="back-navigation">
        <el-button icon="el-icon-arrow-left" type="text" size="medium" @click="$router.back(-1)"> 返回 </el-button>
        <h2 class="preview-title">模板预览</h2>
      </div>

      <div class="preview-actions">
        <el-button
          type="primary"
          icon="el-icon-plus"
          :loading="createProjectLoading"
          class="use-template-btn btn-hover-effect"
          @click="createProjectByTemplate"
        >
          使用此模板
        </el-button>
      </div>
    </div>

    <!-- Main content area with form preview -->
    <el-card class="preview-card" :body-style="{ padding: 0 }" v-loading="createProjectLoading">
      <!-- Template info section -->
      <div class="template-info-section" v-if="templateInfo">
        <div class="template-cover" v-if="templateInfo.coverImg">
          <img :src="templateInfo.coverImg" :alt="templateInfo.name || '模板预览'" />
        </div>
        <div class="template-meta">
          <h3 class="template-name">{{ templateInfo.name || '未命名模板' }}</h3>
          <p class="template-description" v-if="templateInfo.description">{{ templateInfo.description }}</p>
          <div class="template-category" v-if="templateInfo.categoryName">
            <el-tag size="small" type="info">{{ templateInfo.categoryName }}</el-tag>
          </div>
        </div>
      </div>

      <!-- Form content -->
      <div class="preview-content">
        <el-scrollbar class="preview-scrollbar">
          <div class="form-container scale-in">
            <biz-project-form
              v-if="formConfig.formKey"
              :form-config="formConfig"
              @submit="submitForm"
              class="questionnaire-form"
            />
          </div>
        </el-scrollbar>
      </div>

      <!-- Floating action buttons -->
      <div class="floating-actions">
        <el-button
          type="primary"
          icon="el-icon-plus"
          circle
          class="floating-action-btn"
          :loading="createProjectLoading"
          @click="createProjectByTemplate"
        ></el-button>
      </div>
    </el-card>

    <!-- Bottom actions for mobile -->
    <div class="mobile-actions">
      <el-button
        type="primary"
        icon="el-icon-plus"
        :loading="createProjectLoading"
        class="mobile-use-btn"
        @click="createProjectByTemplate"
      >
        使用此模板
      </el-button>
    </div>
  </div>
</template>

<script>
import { BizProjectForm } from 'tduck-form-generator'
import 'tduck-form-generator/dist/TduckForm.css'
import '@/assets/styles/form/questionnaire.scss'
import { useTemplateCreateFormRequest, getFormTemplateDetailRequest } from '@/api/project/template'
import mixin from '../../form/TduckFormMixin'

export default {
  name: 'TemplatePreview',
  components: {
    BizProjectForm
  },
  mixins: [mixin],
  data() {
    return {
      createProjectLoading: false,
      formConfig: {
        formKey: '',
        preview: false,
        formKind: 2,
        showBtns: true,
        showItemNumber: true // 显示题目序号
      },
      templateInfo: null
    }
  },
  created() {
    this.formConfig.formKey = this.$route.query.key
    this.fetchTemplateDetails()
  },
  methods: {
    fetchTemplateDetails() {
      if (this.formConfig.formKey) {
        getFormTemplateDetailRequest({ formKey: this.formConfig.formKey })
          .then((res) => {
            if (res.data) {
              this.templateInfo = res.data
            }
          })
          .catch(() => {
            this.$message.error('获取模板详情失败')
          })
      }
    },
    createProjectByTemplate() {
      this.createProjectLoading = true
      useTemplateCreateFormRequest({ formKey: this.formConfig.formKey })
        .then((res) => {
          if (res.data) {
            this.$message({
              type: 'success',
              message: '创建成功，即将跳转到编辑页面',
              duration: 1500,
              onClose: () => {
                this.$router.push({
                  path: '/project/form/editor',
                  query: { key: res.data, active: 1 }
                })
              }
            })
          }
        })
        .catch(() => {
          this.$message.error('创建失败，请重试')
        })
        .finally(() => {
          this.createProjectLoading = false
        })
    },
    submitForm() {
      // Form submission handled by parent component
      this.$message({
        type: 'info',
        message: '这是预览模式，表单提交功能已禁用'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.preview-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 80px);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.back-navigation {
  display: flex;
  align-items: center;

  .el-button {
    margin-right: 0.5rem;
    padding: 0;
    font-size: 16px;

    &:hover {
      color: #1890ff;
    }
  }
}

.preview-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.preview-actions {
  .use-template-btn {
    padding: 10px 20px;
    font-weight: 500;
  }
}

.preview-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 1rem;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.template-info-section {
  display: flex;
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.template-cover {
  width: 100px;
  height: 100px;
  margin-right: 1.5rem;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.template-meta {
  flex: 1;
}

.template-name {
  margin: 0 0 0.5rem;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.template-description {
  font-size: 14px;
  color: #606266;
  margin: 0 0 1rem;
  line-height: 1.5;
}

.template-category {
  margin-top: 0.5rem;
}

.preview-content {
  flex: 1;
  position: relative;
  overflow: hidden;
  background-color: #f5f7fa;
}

.preview-scrollbar {
  height: 100%;
  overflow: hidden;
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;

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
}

.floating-actions {
  position: fixed;
  right: 30px;
  bottom: 30px;
  z-index: 10;
  display: none;

  @media (min-width: 768px) {
    display: block;
  }
}

.floating-action-btn {
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
  width: 56px;
  height: 56px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);

  &:hover {
    transform: scale(1.1) rotate(90deg);
    box-shadow: 0 6px 16px rgba(24, 144, 255, 0.5);
  }
}

.mobile-actions {
  display: none;
  padding: 1rem;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;

  @media (max-width: 767px) {
    display: flex;
    justify-content: center;
  }

  .mobile-use-btn {
    width: 100%;
  }
}

// Animation classes
.fade-in {
  animation: fadeIn 0.5s ease-out;
}

.scale-in {
  animation: scaleIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

// Responsive adjustments
@media (max-width: 767px) {
  .preview-container {
    padding: 1rem;
    padding-bottom: 70px; // Space for mobile action buttons
  }

  .preview-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .preview-actions {
    display: none;
  }

  .template-info-section {
    flex-direction: column;
    padding: 1rem;
  }

  .template-cover {
    width: 100%;
    height: 120px;
    margin-right: 0;
    margin-bottom: 1rem;
  }

  .form-container {
    padding: 1rem;
  }
}
</style>
