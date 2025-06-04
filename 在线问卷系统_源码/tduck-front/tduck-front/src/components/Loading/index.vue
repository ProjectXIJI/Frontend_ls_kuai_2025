<template>
  <div class="custom-loading" v-if="visible">
    <div class="loading-mask"></div>
    <div class="loading-spinner">
      <i :class="icon"></i>
      <p class="loading-text">{{ text }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomLoading',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    text: {
      type: String,
      default: '加载中...'
    },
    icon: {
      type: String,
      default: 'el-icon-loading'
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-loading {
  position: absolute;
  z-index: 1000;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  
  .loading-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.9);
    z-index: 1001;
  }
  
  .loading-spinner {
    position: relative;
    z-index: 1002;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;
    border-radius: 8px;
    background-color: rgba(0, 0, 0, 0.7);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    
    i {
      font-size: 30px;
      color: #fff;
      animation: rotating 2s linear infinite;
    }
    
    .loading-text {
      margin-top: 10px;
      font-size: 14px;
      color: #fff;
    }
  }
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
