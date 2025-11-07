<template>
  <div style="padding: 40px; max-width: 520px; margin: 0 auto">
    <a-typography-title :heading="3">注册</a-typography-title>
    <a-form layout="vertical">
      <a-form-item field="email" label="邮箱">
        <a-input placeholder="请输入邮箱" />
      </a-form-item>
      <a-form-item field="password" label="密码">
        <a-input-password placeholder="请输入密码" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts"></script>
