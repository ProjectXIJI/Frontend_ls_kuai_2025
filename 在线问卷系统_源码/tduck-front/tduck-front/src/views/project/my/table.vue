<template>
  <div class="project-table-view">
    <el-table
      :data="projectList"
      border
      :empty-text="null"
      highlight-current-row
      style="width: 100%"
      @row-click="handleRowClick"
    >
      <el-table-column align="center" width="300" label="标题" prop="name">
        <template slot-scope="scope">
          <span class="pl10 one-text-overflow">
            {{ scope.row.textName }}
          </span>
        </template>
      </el-table-column>
      <el-table-column align="center" width="100" label="状态">
        <template slot-scope="scope">
          <span v-if="scope.row.type == '3'" />
          <span v-for="status in projectStatusList" v-else :key="status.code">
            <span v-if="status.code == scope.row.status">
              {{ status.name }}
            </span>
          </span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="类型" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.type == '3'">文件夹</span>
          <span v-else>
            <span>
              {{ getType(scope.row.type).name }}
            </span>
          </span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" />
      <el-table-column align="center" label="更新时间" prop="updateTime" />
      <el-table-column width="350" align="center" label="操作">
        <template slot-scope="scope">
          <div class="action-buttons">
            <div class="main-actions">
              <el-button type="text" @click="toProjectHandle(scope.row, 'editor')">
                <i class="el-icon-edit" />
                编辑
              </el-button>
              <el-button type="text" @click="toProjectHandle(scope.row, 'data')">
                <i class="el-icon-document" />
                数据
              </el-button>
              <el-button type="text" @click="toProjectHandle(scope.row, 'statistics')">
                <i class="el-icon-data-analysis" />
                统计
              </el-button>
            </div>
            <div class="publish-actions">
              <el-button
                v-if="scope.row.status != 2"
                type="primary"
                size="mini"
                @click="publishProject(scope.row.formKey)"
              >
                发布
              </el-button>
              <el-button
                v-if="scope.row.status == 2"
                type="warning"
                size="mini"
                @click="stopProject(scope.row.formKey)"
              >
                停止
              </el-button>
              <el-popconfirm title="确定删除该项目吗？" @confirm="logicDeleteForm(scope.row.formKey)">
                <el-button slot="reference" type="danger" size="mini" icon="el-icon-delete"></el-button>
              </el-popconfirm>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="projectList.length === 0" class="empty-container">
      <data-empty icon="el-icon-document" desc="暂无项目数据">
        <el-button type="primary" icon="el-icon-plus" @click="$emit('create')">创建新表单</el-button>
      </data-empty>
    </div>
  </div>
</template>

<script>
import mixin from './mixin'
import { publishFormRequest } from '@/api/project/publish'

export default {
  name: 'MyFormTable',
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
      dialogVisible: false
    }
  },
  methods: {
    handleRowClick(row, column, event) {},
    publishProject(formKey) {
      publishFormRequest({ formKey: formKey }).then(() => {
        this.msgSuccess('发布成功')
        this.$emit('refresh')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.project-table-view {
  margin-top: 20px;
  width: 100%;
}

.icon-wenjianjia {
  color: var(--color-primary);
}

.el-table tr {
  cursor: pointer;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .main-actions {
    display: flex;

    .el-button {
      margin-right: 5px;
      color: #606266;

      &:hover {
        color: #409eff;
      }
    }
  }

  .publish-actions {
    display: flex;
    gap: 8px;

    .el-button--primary {
      background-color: #409eff;
      border-color: #409eff;
      padding: 7px 15px;
    }

    .el-button--warning {
      background-color: #e6a23c;
      border-color: #e6a23c;
      padding: 7px 15px;
    }

    .el-button--danger {
      background-color: #f56c6c;
      border-color: #f56c6c;
      padding: 7px 10px;
    }

    .el-button {
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      }
    }
  }
}

.empty-container {
  padding: 40px 0;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}
</style>
