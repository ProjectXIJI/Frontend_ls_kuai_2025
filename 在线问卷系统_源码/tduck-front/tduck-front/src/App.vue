<template>
  <div id="app">
    <RouterView v-if="isRouterAlive" />
  </div>
</template>

<script>
import TduckForm from 'tduck-form-generator'
import { getCurrentDomain } from '@/utils'

export default {
  name: 'App',
  provide() {
    return {
      reload: this.reload
    }
  },
  data() {
    return {
      isRouterAlive: true
    }
  },
  watch: {
    $route: 'routeChange'
  },
  beforeCreate() {
    window.tduckBaseUrl = process.env['VUE_APP_API_ROOT']
    if (process.env['VUE_APP_API_ROOT']) {
      localStorage.setItem(TduckForm.constant.BASE_URL, process.env['VUE_APP_API_ROOT'])
    } else {
      localStorage.setItem(TduckForm.constant.BASE_URL, getCurrentDomain())
    }
    console.log('更新日期：2024-8-2 16:37')
  },
  methods: {
    reload() {
      this.isRouterAlive = false
      this.$nextTick(() => (this.isRouterAlive = true))
    },
    routeChange(newVal, oldVal) {
      if (newVal.name == oldVal.name) {
        this.reload()
      }
    }
  },
  metaInfo: {
    titleTemplate: (title) => {
      return title ? `${title} - ${process.env.VUE_APP_TITLE}` : process.env.VUE_APP_TITLE
    }
  }
}
</script>
