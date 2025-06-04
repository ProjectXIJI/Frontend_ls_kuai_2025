/**
 * 悬浮提交按钮指令
 */
export default {
  inserted(el, binding, vnode) {
    // 查找表单中的提交按钮
    const formBtnWrapper = el.querySelector('.form-btn-wrapper')
    if (!formBtnWrapper) return

    // 创建悬浮提交按钮
    const submitBtn = document.createElement('button')
    submitBtn.className = 'fixed-submit-btn'
    submitBtn.textContent = '提交'
    submitBtn.style.position = 'fixed'
    submitBtn.style.bottom = '20px'
    submitBtn.style.left = '50%'
    submitBtn.style.transform = 'translateX(-50%)'
    submitBtn.style.padding = '12px 60px'
    submitBtn.style.fontSize = '16px'
    submitBtn.style.fontWeight = '500'
    submitBtn.style.borderRadius = '6px'
    submitBtn.style.backgroundColor = '#409EFF'
    submitBtn.style.color = 'white'
    submitBtn.style.border = 'none'
    submitBtn.style.boxShadow = '0 6px 16px rgba(24, 144, 255, 0.35)'
    submitBtn.style.zIndex = '1001'
    submitBtn.style.cursor = 'pointer'
    submitBtn.style.minWidth = '180px'
    submitBtn.style.textAlign = 'center'
    submitBtn.style.transition = 'all 0.3s ease'
    // 添加悬停效果
    submitBtn.onmouseover = function () {
      submitBtn.style.transform = 'translateX(-50%) translateY(-3px)'
      submitBtn.style.boxShadow = '0 8px 20px rgba(24, 144, 255, 0.45)'
      submitBtn.style.backgroundColor = '#66b1ff'
    }

    submitBtn.onmouseout = function () {
      submitBtn.style.transform = 'translateX(-50%)'
      submitBtn.style.boxShadow = '0 6px 16px rgba(24, 144, 255, 0.35)'
      submitBtn.style.backgroundColor = '#409EFF'
    }

    submitBtn.onclick = function () {
      // 触发原始提交按钮的点击事件
      const originalBtn = formBtnWrapper.querySelector('.el-button')
      if (originalBtn) {
        originalBtn.click()
      }
    }

    // 确保按钮始终固定在底部
    window.addEventListener('scroll', function () {
      // 保持按钮在视口底部
      submitBtn.style.position = 'fixed'
      submitBtn.style.bottom = '20px'
      submitBtn.style.zIndex = '1001'
      submitBtn.style.left = '50%'

      // 检查是否处于悬停状态，保持正确的transform
      if (submitBtn.matches(':hover')) {
        submitBtn.style.transform = 'translateX(-50%) translateY(-3px)'
      } else {
        submitBtn.style.transform = 'translateX(-50%)'
      }
    })

    // 添加到DOM中
    document.body.appendChild(submitBtn)

    // 处理表单项中的说明文本
    setTimeout(() => {
      // 移除多余的标题和描述（红框中的内容）
      const formItems = el.querySelectorAll('.form-item-wrapper')
      formItems.forEach((item) => {
        // 查找表单项中的标题元素
        const titleElements = item.querySelectorAll('.el-form-item__label, .item-label')

        // 检查是否包含"测试AI"或与页面标题相同的文本
        titleElements.forEach((title) => {
          const titleText = title.textContent.trim()
          if (titleText === '测试AI' || titleText === '123') {
            // 找到父元素并隐藏整个表单项
            const parentItem = findParentWithClass(title, 'form-item-wrapper')
            if (parentItem) {
              parentItem.style.display = 'none'
            }
          }
        })

        // 处理说明文本
        const descElements = item.querySelectorAll('.description-text, .el-form-item__description')
        descElements.forEach((desc) => {
          if (desc && desc.textContent && !desc.textContent.trim().startsWith('说明：')) {
            desc.textContent = '说明：' + desc.textContent.trim()
          }
        })
      })
    }, 500) // 延迟执行，确保DOM已完全渲染
  },
  // 当指令所在组件被销毁时调用
  unbind(el, binding) {
    // 移除提交按钮
    const submitBtn = document.querySelector('.fixed-submit-btn')
    if (submitBtn) {
      submitBtn.parentNode.removeChild(submitBtn)
    }

    // 移除事件监听器
    window.removeEventListener('scroll', null)
  }
}

// 查找具有特定类名的父元素
function findParentWithClass(element, className) {
  let parent = element.parentElement
  while (parent) {
    if (parent.classList.contains(className)) {
      return parent
    }
    parent = parent.parentElement
  }
  return null
}
