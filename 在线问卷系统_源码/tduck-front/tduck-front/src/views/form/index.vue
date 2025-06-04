<template>
  <div class="form-index-container">
    <el-card class="header-container">
      <el-row align="middle" type="flex" :gutter="5" style="height: 50px">
        <i class="el-icon-back" @click="$router.back(-1)" />
        <div class="logo-container">
          <img class="header-logo" src="~@/assets/images/custom-logo.svg" @click="$router.push({ path: '/home' })" />
        </div>
        <el-col />
        <el-button type="primary" icon="el-icon-view" @click="previewDialogVisible = true"> 预览 </el-button>
        <el-button type="success" icon="el-icon-folder-add" @click="saveProjectAsTemplateHandle">
          保存至模板中心
        </el-button>
      </el-row>
    </el-card>
    <div class="main-container">
      <div class="left-menu-container">
        <el-menu
          :collapse="isCollapse"
          :default-active="defaultActiveMenu"
          class="el-menu-vertical"
          @select="menuSelectHandle"
        >
          <el-menu-item v-for="menuItem in menuItemList" :key="menuItem.route" :index="menuItem.route">
            <i :class="menuItem.icon" />
            <span slot="title">{{ menuItem.title }}</span>
          </el-menu-item>
        </el-menu>
        <i v-if="!isCollapse" class="el-icon-d-arrow-left" @click="collapseHandle" />
        <i v-else class="el-icon-d-arrow-right" @click="collapseHandle" />
      </div>
      <div class="right-content-container">
        <router-view />
      </div>
    </div>
    <el-dialog :visible.sync="previewDialogVisible" destroy-on-close fullscreen>
      <pre-view :key="previewKey" :preview-qrcode="true" />
    </el-dialog>
    <template-create ref="templateCreate" :form-key="projectKey" />
  </div>
</template>

<script>
import PreView from '@/views/form/preview'
import TemplateCreate from '@/views/project/template/create'

export default {
  name: 'NewIndex',
  components: { TemplateCreate, PreView },
  data() {
    return {
      previewKey: +new Date(),
      previewDialogVisible: false,
      defaultActiveMenu: 'editor',
      projectKey: null,
      isCollapse: false,
      menuItemList: [
        {
          title: '编辑',
          icon: 'el-icon-edit',
          route: '/project/form/editor'
        },
        {
          title: '逻辑',
          icon: 'el-icon-menu',
          route: '/project/form/logic'
        },
        {
          title: '外观',
          icon: 'el-icon-view',
          route: '/project/form/theme'
        },
        {
          title: '设置',
          icon: 'el-icon-setting',
          route: '/project/form/setting'
        },
        {
          title: '发布',
          icon: 'el-icon-video-play',
          route: '/project/form/publish'
        },
        {
          title: '数据',
          icon: 'el-icon-s-data',
          route: '/project/form/data'
        },
        {
          title: '统计',
          icon: 'el-icon-data-line',
          route: '/project/form/statistics'
        }
      ]
    }
  },
  created() {
    this.projectKey = this.$route.query.key
    this.defaultActiveMenu = this.$route.path
    this.isCollapse = this.$store.state.form.isCollapse
  },
  methods: {
    menuSelectHandle(index) {
      this.$router.replace({ path: index, query: { key: this.projectKey } })
    },
    openPreviewHandle() {
      this.previewKey = +new Date()
      this.previewDialogVisible = true
    },
    saveProjectAsTemplateHandle() {
      this.$refs['templateCreate'].handleOpen()
    },
    collapseHandle() {
      let isCollapse = !this.isCollapse
      this.$store.dispatch('form/setIsCollapse', isCollapse).then(() => {
        this.isCollapse = isCollapse
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form-index-container {
  height: 100%;
  width: 100%;
}

::v-deep .el-card__body {
  padding: 0;
}

::v-deep .el-menu {
  border: none;
  background-color: transparent;
}

.header-container {
  width: 100%;
  height: 50px;
  padding: 0 20px;
  line-height: 50px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);

  .el-icon-back {
    font-size: 22px;
    font-weight: 550;
    cursor: pointer;
    color: #000;
    margin-right: 75px;
    transition: all 0.3s ease;

    &:hover {
      color: rgb(32, 160, 255);
      transform: translateX(-3px);
    }
  }

  .logo-container {
    position: relative;
    z-index: 1000;
  }

  .header-logo {
    height: 35px;
    width: 160px;
    vertical-align: middle;
    transition: all 0.3s ease;
    position: fixed;
    z-index: 1000;
    filter: drop-shadow(0 2px 5px rgba(0, 0, 0, 0.1));

    &:hover {
      transform: scale(1.05);
      filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.15));
    }
  }
}

.main-container {
  width: 100vw;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: row;

  .right-content-container {
    width: calc(100vw - 100px);
    height: calc(100vh - 60px);
    overflow-x: hidden;
  }
}

.left-menu-container {
  max-width: 100px;
  text-align: center;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-right: solid 1px #e6e6e6;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;

  .el-menu-vertical:not(.el-menu--collapse) {
    width: 100px;
    min-height: 400px;
  }

  .el-menu-item {
    transition: all 0.3s ease;

    &:hover {
      transform: translateX(3px);
    }

    &.is-active {
      position: relative;
      overflow: hidden;

      &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 3px;
        height: 100%;
        background-color: #409eff;
      }
    }
  }

  .el-icon-d-arrow-left,
  .el-icon-d-arrow-right {
    font-size: 19px;
    cursor: pointer;
    font-weight: 550;
    color: #000;
    margin-bottom: 100px;
    transition: all 0.3s ease;
    border-radius: 50%;
    padding: 5px;

    &:hover {
      color: rgb(32, 160, 255);
      background-color: rgba(64, 158, 255, 0.1);
      transform: scale(1.1);
    }
  }
}

::v-deep.preview-container {
  background-color: #ffffff;
}
</style>
