<template>
  <div class="app-container theme-list-container">
    <!-- 搜索和过滤区域 -->
    <div class="filter-container">
      <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px">
        <el-form-item label="主题名称" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入主题名称"
            size="small"
            prefix-icon="el-icon-search"
            class="custom-input"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-search" size="small" type="primary" class="search-btn" @click="handleQuery"
            >搜索</el-button
          >
          <el-button icon="el-icon-refresh" size="small" class="reset-btn" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="action-container">
      <div class="left-actions">
        <el-button icon="el-icon-plus" size="small" type="primary" class="action-btn" @click="handleAdd"
          >新增主题</el-button
        >
        <el-button
          :disabled="single"
          icon="el-icon-edit"
          size="small"
          type="success"
          class="action-btn"
          @click="handleUpdate"
          >修改</el-button
        >
        <el-button
          :disabled="multiple"
          icon="el-icon-delete"
          size="small"
          type="danger"
          class="action-btn"
          @click="handleDelete"
          >删除</el-button
        >
      </div>
      <div class="right-actions">
        <el-radio-group v-model="viewMode" size="small" class="view-mode-switch">
          <el-radio-button label="table"><i class="el-icon-menu"></i> 表格</el-radio-button>
          <el-radio-button label="card"><i class="el-icon-s-grid"></i> 卡片</el-radio-button>
        </el-radio-group>
        <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
      </div>
    </div>

    <!-- 表格视图 -->
    <transition name="fade-transform" mode="out-in">
      <div v-show="viewMode === 'table'" class="table-container">
        <el-table
          v-loading="loading"
          :data="themeList"
          border
          stripe
          highlight-current-row
          class="theme-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column align="center" type="selection" width="50" />
          <el-table-column align="center" label="ID" prop="id" width="80" />
          <el-table-column align="center" label="主题名称" prop="name" min-width="120" show-overflow-tooltip />
          <el-table-column align="center" label="主题风格" prop="style" width="120">
            <template slot-scope="scope">
              <el-tag size="medium" effect="plain">{{ getStyleName(scope.row.style) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="头部图片" prop="headImgUrl" min-width="150">
            <template slot-scope="scope">
              <div class="image-preview-container">
                <el-image
                  :preview-src-list="[scope.row.headImgUrl]"
                  :src="scope.row.headImgUrl"
                  fit="cover"
                  class="preview-image"
                >
                  <div slot="error" class="image-error">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
              </div>
            </template>
          </el-table-column>
          <el-table-column align="center" label="背景图片" prop="backgroundImg" min-width="150">
            <template slot-scope="scope">
              <div v-if="scope.row.backgroundImg" class="image-preview-container">
                <el-image
                  :preview-src-list="[scope.row.backgroundImg]"
                  :src="scope.row.backgroundImg"
                  fit="cover"
                  class="preview-image"
                >
                  <div slot="error" class="image-error">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
              </div>
              <el-tag v-else size="small" type="info">未配置背景图片</el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="主题颜色" prop="color" width="100">
            <template slot-scope="scope">
              <div class="color-preview">
                <span
                  :style="{ backgroundColor: scope.row.themeColor }"
                  class="color-block"
                  :title="scope.row.themeColor"
                />
                <span class="color-value">{{ scope.row.themeColor || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="150">
            <template slot-scope="scope">
              <el-button
                icon="el-icon-edit"
                size="mini"
                type="primary"
                circle
                title="修改"
                @click="handleUpdate(scope.row)"
              ></el-button>
              <el-button
                icon="el-icon-delete"
                size="mini"
                type="danger"
                circle
                title="删除"
                @click="handleDelete(scope.row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 卡片视图 -->
      <div v-show="viewMode === 'card'" class="card-container">
        <el-row :gutter="20">
          <el-col v-for="item in themeList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="4">
            <el-card class="theme-card" :body-style="{ padding: '0px' }" shadow="hover">
              <div class="card-header">
                <div class="card-title">{{ item.name }}</div>
                <el-tag size="small" effect="plain">{{ getStyleName(item.style) }}</el-tag>
              </div>
              <div class="card-image-container">
                <el-image :src="item.headImgUrl" fit="cover" class="card-image" @click="previewImage(item.headImgUrl)">
                  <div slot="error" class="image-error">
                    <i class="el-icon-picture-outline"></i>
                  </div>
                </el-image>
                <div class="image-overlay">
                  <span class="overlay-text">点击预览</span>
                </div>
              </div>
              <div class="card-content">
                <div class="card-info">
                  <div class="info-item">
                    <span class="info-label">背景图：</span>
                    <span v-if="item.backgroundImg" class="info-value">
                      <el-button type="text" @click="previewImage(item.backgroundImg)">查看</el-button>
                    </span>
                    <span v-else class="info-value">未配置</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">主题色：</span>
                    <span class="info-value">
                      <span :style="{ backgroundColor: item.themeColor }" class="color-block"></span>
                    </span>
                  </div>
                </div>
                <div class="card-actions">
                  <el-button type="primary" size="mini" icon="el-icon-edit" @click="handleUpdate(item)">编辑</el-button>
                  <el-button type="danger" size="mini" icon="el-icon-delete" @click="handleDelete(item)"
                    >删除</el-button
                  >
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </transition>

    <!-- 分页 -->
    <div class="pagination-container">
      <pagination
        v-show="total > 0"
        :limit.sync="queryParams.size"
        :page.sync="queryParams.current"
        :total="total"
        @pagination="getList"
      />
    </div>

    <!-- 添加或修改项目主题外观模板对话框 -->
    <el-dialog
      :title="title"
      :visible.sync="open"
      append-to-body
      width="600px"
      center
      :close-on-click-modal="false"
      custom-class="theme-dialog"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="theme-form">
        <el-form-item label="主题名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入主题名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="主题风格" prop="style">
          <el-select v-model="form.style" placeholder="请选择主题风格" class="custom-select" style="width: 100%">
            <el-option v-for="c in themeCategoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="头部图片" prop="headImgUrl" class="upload-form-item">
          <div class="upload-container">
            <image-upload v-model="form.headImgUrl" />
            <div class="upload-tip"><i class="el-icon-info"></i> 建议尺寸: 1200 x 300 像素，格式为 JPG 或 PNG</div>
          </div>
        </el-form-item>
        <el-form-item label="背景图片" prop="backgroundImg" class="upload-form-item">
          <div class="upload-container">
            <image-upload v-model="form.backgroundImg" />
            <div class="upload-tip"><i class="el-icon-info"></i> 建议尺寸: 1920 x 1080 像素，格式为 JPG 或 PNG</div>
          </div>
        </el-form-item>
        <el-form-item label="主题颜色">
          <div class="color-picker-container">
            <el-color-picker v-model="form.themeColor" show-alpha />
            <el-input v-model="form.themeColor" placeholder="颜色代码" class="color-input" />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel" class="cancel-btn">取 消</el-button>
        <el-button type="primary" @click="submitForm" class="submit-btn">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog
      title="图片预览"
      :visible.sync="previewVisible"
      width="800px"
      center
      append-to-body
      custom-class="preview-dialog"
    >
      <div class="preview-container">
        <el-image :src="previewUrl" fit="contain" class="preview-image-large">
          <div slot="error" class="image-error large">
            <i class="el-icon-picture-outline"></i>
            <p>图片加载失败</p>
          </div>
        </el-image>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addTheme, delTheme, getTheme, listCategory, listTheme, updateTheme } from '@/api/project/theme'

export default {
  name: 'FormTheme',
  components: {},
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 项目主题外观模板表格数据
      themeList: [],
      // 分类
      themeCategoryList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        current: 1,
        size: 10
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: '主题名称不能为空', trigger: 'blur' }],
        style: [{ required: true, message: '主题风格不能为空', trigger: 'change' }],
        headImgUrl: [{ required: true, message: '头部图片不能为空', trigger: 'blur' }]
      },
      // 视图模式：表格或卡片
      viewMode: 'table',
      // 图片预览
      previewVisible: false,
      previewUrl: ''
    }
  },
  created() {
    this.getList()
    this.getThemeCategoryList()
  },
  methods: {
    /** 查询项目主题外观模板列表 */
    getList() {
      this.loading = true
      listTheme(this.queryParams).then((response) => {
        this.themeList = response.data.records
        this.total = response.data.total
        this.loading = false
      })
    },

    /**
     * 获取主题分类列表
     */
    getThemeCategoryList() {
      listCategory().then((response) => {
        this.themeCategoryList = response.data
      })
    },

    /**
     * 获取主题风格名称
     * @param {number} id 主题风格 ID
     * @returns {string} 主题风格名称
     */
    getStyleName(id) {
      let item = this.themeCategoryList.find((item) => item.id == id)
      return item ? item.name : ''
    },

    /**
     * 图片预览
     * @param {string} url 图片URL
     */
    previewImage(url) {
      if (!url) return
      this.previewUrl = url
      this.previewVisible = true
    },

    /**
     * 取消按钮
     */
    cancel() {
      this.open = false
      this.reset()
    },

    /**
     * 表单重置
     */
    reset() {
      this.form = {
        id: null,
        name: null,
        style: null,
        headImgUrl: null,
        backgroundImg: null,
        themeColor: null,
        updateTime: null,
        createTime: null
      }
      this.resetForm('form')
    },

    /**
     * 搜索按钮操作
     */
    handleQuery() {
      this.queryParams.current = 1
      this.getList()
    },

    /**
     * 重置按钮操作
     */
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },

    /**
     * 多选框选中数据
     */
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },

    /**
     * 新增按钮操作
     */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加主题'
    },

    /**
     * 修改按钮操作
     */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getTheme(id).then((response) => {
        this.form = response.data
        this.open = true
        this.title = '修改主题'
      })
    },

    /**
     * 提交按钮
     */
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if (this.form.id != null) {
            updateTheme(this.form).then(() => {
              this.$notify({
                title: '成功',
                message: '主题修改成功',
                type: 'success',
                duration: 2000
              })
              this.open = false
              this.getList()
            })
          } else {
            addTheme(this.form).then(() => {
              this.$notify({
                title: '成功',
                message: '主题添加成功',
                type: 'success',
                duration: 2000
              })
              this.open = false
              this.getList()
            })
          }
        }
      })
    },

    /**
     * 删除按钮操作
     */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$confirm('是否确认删除选中的主题?', '删除确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(function () {
          return delTheme(ids)
        })
        .then(() => {
          this.getList()
          this.$notify({
            title: '成功',
            message: '删除成功',
            type: 'success',
            duration: 2000
          })
        })
        .catch(() => {})
    }
  }
}
</script>
<style lang="scss" scoped>
/* 容器样式 */
.theme-list-container {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

/* 搜索区域 */
.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.custom-input {
  width: 240px;
}

.search-btn {
  margin-right: 10px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  }
}

.reset-btn {
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
  }
}

/* 操作按钮区域 */
.action-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.left-actions {
  display: flex;
  gap: 10px;
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.action-btn {
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.view-mode-switch {
  margin-right: 10px;
}

/* 表格容器 */
.table-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 0;
  margin-bottom: 20px;
  overflow: hidden;
}

.theme-table {
  width: 100%;
}

/* 图片预览容器 */
.image-preview-container {
  width: 120px;
  height: 60px;
  margin: 0 auto;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;

  &.large {
    height: 300px;
    flex-direction: column;

    i {
      font-size: 48px;
      margin-bottom: 10px;
    }
  }
}

/* 颜色块 */
.color-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.color-block {
  width: 24px;
  height: 24px;
  display: inline-block;
  border-radius: 4px;
  margin: 3px auto;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.color-value {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 卡片视图 */
.card-container {
  margin-bottom: 20px;
}

.theme-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  }
}

.card-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}

.card-image-container {
  height: 150px;
  position: relative;
  overflow: hidden;

  &:hover .image-overlay {
    opacity: 1;
  }
}

.card-image {
  width: 100%;
  height: 100%;
  transition: all 0.5s;

  &:hover {
    transform: scale(1.05);
  }
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: all 0.3s;
  cursor: pointer;
}

.overlay-text {
  color: #fff;
  font-size: 14px;
  padding: 6px 12px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
}

.card-content {
  padding: 15px;
}

.card-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
  align-items: center;
}

.info-label {
  color: #606266;
  margin-right: 5px;
}

.info-value {
  color: #303133;
  display: flex;
  align-items: center;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 对话框样式 */
.theme-dialog {
  border-radius: 8px;
  overflow: hidden;
}

.theme-form {
  padding: 0 20px;
}

.upload-form-item {
  margin-bottom: 30px;
}

.upload-container {
  display: flex;
  flex-direction: column;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;

  i {
    margin-right: 4px;
    color: #e6a23c;
  }
}

.color-picker-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.color-input {
  width: 180px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.cancel-btn,
.submit-btn {
  width: 120px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
  }
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}

.preview-image-large {
  max-width: 100%;
  max-height: 500px;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 动画效果 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.5s;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateY(20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
