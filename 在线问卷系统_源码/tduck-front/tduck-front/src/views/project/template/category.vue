<template>
  <div class="category-container fade-in">
    <!-- Search and Filter Header -->
    <div class="category-header">
      <div class="header-left">
        <h2 class="page-title">模板分类管理</h2>
        <p class="page-description">管理表单模板的分类，便于模板的组织和查找</p>
      </div>

      <div class="search-filter" v-show="showSearch">
        <el-input
          v-model="queryParams.name"
          placeholder="搜索分类名称"
          prefix-icon="el-icon-search"
          clearable
          @keyup.enter.native="handleQuery"
        />
        <div class="filter-buttons">
          <el-button icon="el-icon-search" type="primary" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </div>
      </div>
    </div>

    <!-- Action Toolbar -->
    <div class="action-toolbar">
      <div class="left-actions">
        <el-button
          v-hasPermi="['form:template:category:save']"
          type="primary"
          icon="el-icon-plus"
          @click="handleAdd"
          class="btn-hover-effect"
        >
          新增分类
        </el-button>
        <el-button
          v-hasPermi="['form:template:category:update']"
          type="success"
          icon="el-icon-edit"
          :disabled="single"
          @click="handleUpdate"
          class="btn-hover-effect"
        >
          修改
        </el-button>
        <el-button
          v-hasPermi="['form:template:category:delete']"
          type="danger"
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          class="btn-hover-effect"
        >
          删除
        </el-button>
      </div>

      <div class="right-actions">
        <el-button type="text" icon="el-icon-view" @click="showSearch = !showSearch">
          {{ showSearch ? '隐藏搜索' : '显示搜索' }}
        </el-button>
        <el-button type="text" icon="el-icon-refresh" @click="getList"> 刷新 </el-button>
      </div>
    </div>

    <!-- Category Table -->
    <el-card class="category-table-card" shadow="hover">
      <el-table
        v-loading="loading"
        :data="categoryList"
        border
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange"
        class="category-table"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="分类名称" prop="name" align="center" min-width="150">
          <template slot-scope="scope">
            <el-tag type="info" effect="plain">{{ scope.row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
        <el-table-column label="操作" align="center" width="200">
          <template slot-scope="scope">
            <el-button
              v-hasPermi="['form:template:category:update']"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              class="table-action-btn"
            >
              编辑
            </el-button>
            <el-divider direction="vertical"></el-divider>
            <el-button
              v-hasPermi="['form:template:category:delete']"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              class="table-action-btn danger-action"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Empty state -->
      <div v-if="categoryList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-folder-opened"></i>
        <p>暂无分类数据</p>
        <el-button type="primary" @click="handleAdd" v-hasPermi="['form:template:category:save']">
          创建第一个分类
        </el-button>
      </div>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          :current-page.sync="queryParams.current"
          :page-sizes="[10, 20, 30, 50]"
          :page-size.sync="queryParams.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="getList"
          @current-change="getList"
          background
        />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body center class="category-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" :step="1" controls-position="right" />
          <div class="form-item-help">数字越小排序越靠前，默认为0</div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addCategory, delCategory, getCategory, listCategory, updateCategory } from '@/api/project/template'

export default {
  name: 'TemplateCategory',
  data() {
    return {
      // Loading states
      loading: false,
      submitLoading: false,

      // Selection
      ids: [],
      single: true,
      multiple: true,

      // Display
      showSearch: true,

      // Data
      total: 0,
      categoryList: [],

      // Dialog
      title: '',
      open: false,

      // Query
      queryParams: {
        current: 1,
        size: 10,
        name: null,
        sort: null
      },

      // Form
      form: {
        id: null,
        name: null,
        sort: 0
      },

      // Validation
      rules: {
        name: [
          { required: true, message: '分类名称不能为空', trigger: 'blur' },
          { min: 2, max: 50, message: '分类名称长度必须在2到50个字符之间', trigger: 'blur' }
        ],
        sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询分类列表 */
    getList() {
      this.loading = true
      listCategory(this.queryParams)
        .then((response) => {
          this.categoryList = response.data.records || []
          this.total = response.data.total || 0
        })
        .catch(() => {
          this.categoryList = []
          this.total = 0
          this.$message.error('获取分类列表失败')
        })
        .finally(() => {
          this.loading = false
        })
    },

    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },

    /** 表单重置 */
    reset() {
      this.form = {
        id: null,
        name: null,
        sort: 0
      }
      this.resetForm('form')
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.current = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },

    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加模板分类'
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids[0]
      this.loading = true

      getCategory(id)
        .then((response) => {
          this.form = response.data
          this.open = true
          this.title = '修改模板分类'
        })
        .catch(() => {
          this.$message.error('获取分类详情失败')
        })
        .finally(() => {
          this.loading = false
        })
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.submitLoading = true

          if (this.form.id != null) {
            // 更新
            updateCategory(this.form)
              .then(() => {
                this.$message.success('修改成功')
                this.open = false
                this.getList()
              })
              .catch(() => {
                this.$message.error('修改失败')
              })
              .finally(() => {
                this.submitLoading = false
              })
          } else {
            // 新增
            addCategory(this.form)
              .then(() => {
                this.$message.success('新增成功')
                this.open = false
                this.getList()
              })
              .catch(() => {
                this.$message.error('新增失败')
              })
              .finally(() => {
                this.submitLoading = false
              })
          }
        }
      })
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      const names =
        row.name ||
        this.categoryList
          .filter((item) => this.ids.includes(item.id))
          .map((item) => item.name)
          .join('、')

      this.$confirm(`确定要删除分类"${names}"吗？删除后将无法恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.loading = true
          return delCategory(ids)
        })
        .then(() => {
          this.$message.success('删除成功')
          this.getList()
        })
        .catch(() => {})
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.category-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  .page-title {
    margin: 0 0 4px 0;
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }

  .page-description {
    margin: 0;
    font-size: 14px;
    color: #909399;
  }
}

.search-filter {
  display: flex;
  gap: 10px;

  .el-input {
    width: 300px;
  }

  .filter-buttons {
    display: flex;
    gap: 8px;
  }
}

.action-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;

  .left-actions {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }

  .right-actions {
    display: flex;
    gap: 10px;
    align-items: center;
  }
}

.category-table-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  }

  .category-table {
    margin: 10px 0;

    .table-action-btn {
      padding: 4px 8px;
      font-size: 13px;

      &:hover {
        background-color: #f0f9ff;
        border-radius: 4px;
      }

      &.danger-action:hover {
        color: #f56c6c;
        background-color: #fef0f0;
      }
    }
  }
}

.empty-state {
  padding: 40px 0;
  text-align: center;

  i {
    font-size: 48px;
    color: #c0c4cc;
    margin-bottom: 15px;
  }

  p {
    font-size: 16px;
    color: #909399;
    margin: 0 0 20px;
  }
}

.pagination-container {
  padding: 15px 0 5px;
  text-align: right;
}

.category-dialog {
  .el-dialog {
    border-radius: 8px;
    overflow: hidden;
  }

  .el-input-number {
    width: 100%;
  }

  .form-item-help {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

// Animation
.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

// Responsive
@media (max-width: 768px) {
  .category-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-filter {
    width: 100%;
    flex-direction: column;

    .el-input {
      width: 100%;
    }

    .filter-buttons {
      width: 100%;
      justify-content: space-between;

      .el-button {
        flex: 1;
      }
    }
  }

  .action-toolbar {
    flex-direction: column;

    .left-actions,
    .right-actions {
      width: 100%;
      justify-content: space-between;
    }
  }
}
</style>
