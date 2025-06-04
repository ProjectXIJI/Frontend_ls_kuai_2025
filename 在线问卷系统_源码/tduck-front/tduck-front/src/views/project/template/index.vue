<template>
  <div class="template-container fade-in">
    <!-- Header section with search and filters -->
    <div class="template-header">
      <div class="search-section">
        <el-input
          v-model="queryParams.name"
          placeholder="搜索模板..."
          prefix-icon="el-icon-search"
          clearable
          @keyup.enter.native="queryTemplatePage"
        />
        <el-button type="primary" @click="queryTemplatePage" class="search-btn"> 查询 </el-button>
      </div>

      <div class="category-tabs">
        <el-tabs v-model="queryParams.type" @tab-click="queryTemplatePage" stretch>
          <el-tab-pane label="全部" name=""></el-tab-pane>
          <el-tab-pane
            v-for="(item, index) in templateTypeList"
            :key="index"
            :label="item.name"
            :name="item.id.toString()"
          ></el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- Template grid section -->
    <div class="template-content">
      <transition-group name="template-list" tag="div" class="template-grid">
        <div v-for="template in templateList" :key="template.id" class="template-card card-hover-effect">
          <div class="template-card-inner">
            <div class="template-image">
              <img
                :src="template.coverImg ? template.coverImg : require('@/assets/images/99d0.png')"
                :alt="template.name"
              />
              <div class="template-badge">
                <el-tag size="small" type="success">模板</el-tag>
              </div>
            </div>
            <div class="template-info">
              <h3 class="template-title">{{ template.name }}</h3>
              <p v-if="template.description" class="template-description">{{ template.description }}</p>
              <div class="template-meta">
                <span class="meta-item"> <i class="el-icon-time"></i> {{ formatDate(template.createTime) }} </span>
                <span class="meta-item">
                  <i class="el-icon-user"></i>
                  <template v-if="template.userId === 1">
                    <el-tag size="mini" type="warning">管理员</el-tag> {{ template.userName || '系统模板' }}
                  </template>
                  <template v-else>
                    <el-tag size="mini" type="primary">用户</el-tag> {{ template.userName || '系统模板' }}
                  </template>
                </span>
              </div>
            </div>
            <div class="template-actions">
              <el-button
                type="primary"
                icon="el-icon-view"
                size="small"
                @click="toProjectTemplate(template.formKey)"
                class="btn-hover-effect"
              >
                查看
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="small"
                @click="handleDelete(template)"
                class="btn-hover-effect"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- Empty state when no templates are found -->
        <div v-if="templateList.length === 0" key="empty" class="empty-state">
          <i class="el-icon-document"></i>
          <p>暂无模板</p>
        </div>
      </transition-group>
    </div>

    <!-- Pagination -->
    <div class="template-pagination">
      <el-pagination
        v-if="total > 0"
        :current-page.sync="queryParams.current"
        :page-size.sync="queryParams.size"
        :total="total"
        background
        layout="total, prev, pager, next, jumper"
        @current-change="queryTemplatePage"
      />
    </div>
  </div>
</template>

<script>
import {
  deleteFormTemplateRequest,
  getFormTemplatePageRequest,
  getFormTemplateTypeListRequest
} from '@/api/project/template'
import { createFormRequest } from '@/api/project/form'

export default {
  name: 'TemplateIndex',
  data() {
    return {
      queryParams: {
        current: 1,
        size: 12,
        name: '',
        type: ''
      },
      total: 0,
      templateTypeList: [],
      templateList: [],
      loading: false
    }
  },
  created() {
    this.queryTemplateType()
    this.queryTemplatePage()
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return '未知时间'
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(
        2,
        '0'
      )}`
    },
    queryTemplateType() {
      getFormTemplateTypeListRequest().then((res) => {
        this.templateTypeList = res.data
      })
    },
    toProjectTemplate(key) {
      this.$router.push({
        path: '/project/template/preview',
        query: { key: key }
      })
    },
    handleDelete(item) {
      this.$confirm(`此操作将永久删除模板"${item.name}", 是否继续?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.loading = true
          deleteFormTemplateRequest({ formKey: item.formKey })
            .then((res) => {
              this.loading = false
              this.msgSuccess('删除成功')
              this.queryTemplatePage()
            })
            .catch(() => {
              this.loading = false
            })
        })
        .catch(() => {})
    },
    queryTemplatePage() {
      this.loading = true
      getFormTemplatePageRequest(this.queryParams)
        .then((res) => {
          let { records, total, size } = res.data
          this.templateList = records

          // 确保每个模板项都有userId字段
          this.templateList.forEach((template) => {
            if (!template.userId && template.userId !== 0) {
              // 如果没有userId字段，根据userAdmin字段或其他信息设置默认值
              // 假设系统模板或管理员创建的模板userId为1
              if (template.userAdmin || !template.userName || template.userName === '系统模板') {
                template.userId = 1
              } else {
                template.userId = 2 // 普通用户
              }
            }
          })

          this.total = total
          this.queryParams.size = size
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    createBlankTemplate() {
      this.loading = true
      createFormRequest({ description: '表单模板', name: '表单模板' })
        .then((res) => {
          this.loading = false
          this.$router.push({ path: '/project/form', query: { key: res.data } })
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.template-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.template-header {
  margin-bottom: 2rem;
}

.search-section {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;

  .el-input {
    max-width: 400px;
  }

  .search-btn {
    margin-left: 1rem;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
    }
  }
}

.category-tabs {
  .el-tabs__nav-wrap::after {
    height: 1px;
    background-color: #f0f0f0;
  }

  .el-tabs__item {
    height: 50px;
    line-height: 50px;
    transition: all 0.3s ease;

    &.is-active {
      font-weight: 600;
      color: #1890ff;
    }

    &:hover {
      color: #1890ff;
    }
  }
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.template-card {
  border-radius: 12px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
    border-color: #e6f7ff;

    .template-actions {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.template-card-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.template-image {
  height: 180px;
  overflow: hidden;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: all 0.5s ease;

    &:hover {
      transform: scale(1.05);
    }
  }

  .template-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 2;
  }
}

.template-info {
  padding: 1rem;
  flex-grow: 1;
}

.template-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 0.5rem;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.template-description {
  font-size: 13px;
  color: #909399;
  margin: 0 0 0.8rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.template-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-top: 0.5rem;

  .meta-item {
    display: flex;
    align-items: center;

    i {
      margin-right: 4px;
    }

    .el-tag {
      margin-right: 5px;
      font-weight: bold;
    }
  }
}

.template-actions {
  padding: 0.5rem 1rem 1rem;
  display: flex;
  justify-content: space-between;
  opacity: 0.8;
  transform: translateY(10px);
  transition: all 0.3s ease;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background-color: #fafafa;
  border-radius: 8px;
  color: #909399;

  i {
    font-size: 48px;
    margin-bottom: 1rem;
  }

  p {
    font-size: 16px;
    margin: 0;
  }
}

.template-pagination {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

// Animations
.template-list-enter-active,
.template-list-leave-active {
  transition: all 0.5s;
}

.template-list-enter,
.template-list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.template-list-move {
  transition: transform 0.5s;
}

// Responsive adjustments
@media (max-width: 768px) {
  .template-container {
    padding: 1rem;
  }

  .template-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }

  .template-image {
    height: 140px;
  }
}

@media (max-width: 480px) {
  .search-section {
    flex-direction: column;
    align-items: stretch;

    .el-input {
      max-width: 100%;
    }

    .search-btn {
      margin: 1rem 0 0;
      width: 100%;
    }
  }

  .template-grid {
    grid-template-columns: 1fr;
  }
}
</style>
