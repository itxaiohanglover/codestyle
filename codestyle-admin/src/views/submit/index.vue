<template>
  <div style="padding: 40px; max-width: 900px; margin: 0 auto">
    <a-typography-title :heading="3">上传模板</a-typography-title>
    <a-form layout="vertical">
      <a-form-item field="title" label="标题">
        <a-input placeholder="请输入标题" />
      </a-form-item>
      <a-form-item field="url" label="链接">
        <a-input placeholder="请输入链接" />
      </a-form-item>
      <a-form-item field="desc" label="描述">
        <a-textarea
          placeholder="简单描述你的灵感"
          :auto-size="{ minRows: 4 }"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary">提交</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts"></script>
