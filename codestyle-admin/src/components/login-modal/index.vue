<template>
  <a-modal
    v-model:visible="visible"
    :title="'登录NoCode平台'"
    :width="360"
    :footer="null"
    centered
    :mask-closeable="true"
    @close="handleClose"
  >
    <div class="login-modal-content">
      <div class="login-header">
        <img src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image" alt="NoCode" class="login-logo" />
        <h2 class="login-title">扫码登录开启NoCode之旅</h2>
        <p class="login-subtitle">未注册的微信将自动创建账号</p>
      </div>
      
      <div class="qrcode-container">
        <div class="qrcode-placeholder">
          <!-- 这里放二维码图片 -->
          <div style="width: 200px; height: 200px; background: #f0f0f0; display: flex; align-items: center; justify-content: center;">
            <span>二维码占位</span>
          </div>
        </div>
        <div class="qrcode-tips">
          <a-icon name="wechat" style="color: #07C160; margin-right: 4px;" />
          微信扫码登录
        </div>
      </div>
      
      <div class="login-links">
        <a href="#" class="login-link">注册提示</a>
        <a href="#" class="login-link">隐私政策</a>
        <a href="#" class="login-link">用户协议</a>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'close']);

const handleClose = () => {
  emit('update:visible', false);
  emit('close');
};
</script>

<style lang="less" scoped>
.login-modal-content {
  padding: 20px 0;
  
  .login-header {
    text-align: center;
    margin-bottom: 30px;
    
    .login-logo {
      width: 60px;
      height: 60px;
      margin-bottom: 16px;
    }
    
    .login-title {
      font-size: 20px;
      color: #333;
      margin-bottom: 8px;
    }
    
    .login-subtitle {
      font-size: 14px;
      color: #666;
    }
  }
  
  .qrcode-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 30px;
    
    .qrcode-placeholder {
      padding: 10px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
    
    .qrcode-tips {
      margin-top: 16px;
      font-size: 14px;
      color: #FF9500;
      display: flex;
      align-items: center;
    }
  }
  
  .login-links {
    display: flex;
    justify-content: center;
    gap: 20px;
    
    .login-link {
      font-size: 12px;
      color: #999;
      
      &:hover {
        color: #FF9500;
      }
    }
  }
}
</style>