<template>
  <FormDesign :question-mode="true" :show-item-number="true" />
</template>
<script>
import TduckForm, { FormDesign } from 'tduck-form-generator'
import 'tduck-form-generator/dist/TduckForm.css'
import mixin from '../TduckFormMixin.js'

export default {
  name: 'FormEditor',
  components: {
    FormDesign
  },
  mixins: [mixin],
  data() {
    return {
      showItemNumber: true
    }
  },
  computed: {},
  methods: {}
}
</script>

<style lang="scss">
/* 题目序号样式 */
::v-deep .form-item-wrapper {
  position: relative;

  .item-number {
    position: absolute;
    left: -15px;
    top: 20px;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #1890ff;
    color: white;
    border-radius: 50%;
    font-weight: bold;
    font-size: 14px;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
    z-index: 10;
  }
}
</style>
