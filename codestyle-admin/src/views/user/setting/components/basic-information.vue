<template>
  <div class="form-scroll-container">
    <div class="form-scroll-wrapper">
      <div class="form-item-container">
        <a-form
          ref="formRef"
          :model="formData"
          class="form"
          layout="vertical"
        >
          <a-form-item
            field="email"
            :label="$t('userSetting.basicInfo.form.label.email')"
            :rules="[
              {
                required: true,
                message: $t('userSetting.form.error.email.required'),
              },
            ]"
          >
            <a-input
              v-model="formData.email"
              :placeholder="$t('userSetting.basicInfo.placeholder.email')"
              class="form-input"
            />
          </a-form-item>
        </a-form>
      </div>
      
      <div class="form-item-container">
        <a-form
          :model="formData"
          class="form"
          layout="vertical"
        >
          <a-form-item
            field="nickname"
            :label="$t('userSetting.basicInfo.form.label.nickname')"
            :rules="[
              {
                required: true,
                message: $t('userSetting.form.error.nickname.required'),
              },
            ]"
          >
            <a-input
              v-model="formData.nickname"
              :placeholder="$t('userSetting.basicInfo.placeholder.nickname')"
              class="form-input"
            />
          </a-form-item>
        </a-form>
      </div>
      
      <div class="form-item-container">
        <a-form
          :model="formData"
          class="form"
          layout="vertical"
        >
          <a-form-item
            field="countryRegion"
            :label="$t('userSetting.basicInfo.form.label.countryRegion')"
            :rules="[
              {
                required: true,
                message: $t('userSetting.form.error.countryRegion.required'),
              },
            ]"
          >
            <a-select
              v-model="formData.countryRegion"
              :placeholder="$t('userSetting.basicInfo.placeholder.area')"
              class="form-input"
            >
              <a-option value="China">中国</a-option>
            </a-select>
          </a-form-item>
        </a-form>
      </div>
      
      <div class="form-item-container">
        <a-form
          :model="formData"
          class="form"
          layout="vertical"
        >
          <a-form-item
            field="area"
            :label="$t('userSetting.basicInfo.form.label.area')"
            :rules="[
              {
                required: true,
                message: $t('userSetting.form.error.area.required'),
              },
            ]"
          >
            <a-cascader
              v-model="formData.area"
              :placeholder="$t('userSetting.basicInfo.placeholder.area')"
              :options="[
                {
                  label: '北京',
                  value: 'beijing',
                  children: [
                    {
                      label: '北京',
                      value: 'beijing',
                      children: [
                        {
                          label: '朝阳',
                          value: 'chaoyang',
                        },
                      ],
                    },
                  ],
                },
              ]"
              allow-clear
              class="form-input"
            />
          </a-form-item>
        </a-form>
      </div>
      
      <div class="form-item-container">
        <a-form
          :model="formData"
          class="form"
          layout="vertical"
        >
          <a-form-item
            field="address"
            :label="$t('userSetting.basicInfo.form.label.address')"
          >
            <a-input
              v-model="formData.address"
              :placeholder="$t('userSetting.basicInfo.placeholder.address')"
              class="form-input"
            />
          </a-form-item>
        </a-form>
      </div>
      
      <div class="form-item-container textarea-container">
        <a-form
          :model="formData"
          class="form"
          layout="vertical"
        >
          <a-form-item
            field="profile"
            :label="$t('userSetting.basicInfo.form.label.profile')"
            :rules="[
              {
                maxLength: 200,
                message: $t('userSetting.form.error.profile.maxLength'),
              },
            ]"
          >
            <a-textarea
              v-model="formData.profile"
              :placeholder="$t('userSetting.basicInfo.placeholder.profile')"
              class="form-textarea"
              :auto-size="{ minRows: 4, maxRows: 6 }"
            />
          </a-form-item>
        </a-form>
      </div>
    </div>
    
    <div class="form-actions">
      <a-space>
        <a-button type="primary" @click="validate" class="submit-button">
          {{ $t('userSetting.save') }}
        </a-button>
        <a-button type="secondary" @click="reset" class="reset-button">
          {{ $t('userSetting.reset') }}
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { BasicInfoModel } from '@/api/user-center';

  const formRef = ref<FormInstance>();
  const formData = ref<BasicInfoModel>({
    email: '',
    nickname: '',
    countryRegion: '',
    area: '',
    address: '',
    profile: '',
  });
  const validate = async () => {
    const res = await formRef.value?.validate();
    if (!res) {
      // do some thing
      // you also can use html-type to submit
    }
  };
  const reset = async () => {
    await formRef.value?.resetFields();
  };
</script>

<style scoped lang="less">
  // 水平滚动容器
  .form-scroll-container {
    width: 100%;
    overflow: hidden;
    background: linear-gradient(135deg, #FFFBEB 0%, #FFFFFF 100%);
    border-radius: 8px;
    padding: 20px;
  }
  
  // 滚动包装器
  .form-scroll-wrapper {
    display: flex;
    gap: 20px;
    overflow-x: auto;
    padding-bottom: 10px;
    // 隐藏滚动条但保持功能
    scrollbar-width: thin;
    scrollbar-color: #F59E0B #FFFBEB;
    
    // WebKit滚动条样式
    &::-webkit-scrollbar {
      height: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: #FFFBEB;
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-thumb {
      background-color: #F59E0B;
      border-radius: 3px;
      border: 2px solid #FFFBEB;
    }
    
    // 触摸设备优化
    -webkit-overflow-scrolling: touch;
    scroll-behavior: smooth;
  }
  
  // 表单项目容器
  .form-item-container {
    flex: 0 0 auto;
    width: 300px;
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.1);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(245, 158, 11, 0.2);
    }
    
    // 文本域特殊处理
    &.textarea-container {
      width: 400px;
    }
  }
  
  // 表单样式
  .form {
    width: 100%;
  }
  
  // 输入框样式
  .form-input {
    width: 100%;
    border-color: #FDE68A;
    border-radius: 8px;
    
    &:focus {
      border-color: #F59E0B;
      box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.2);
    }
  }
  
  // 文本域样式
  .form-textarea {
    width: 100%;
    border-color: #FDE68A;
    border-radius: 8px;
    
    &:focus {
      border-color: #F59E0B;
      box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.2);
    }
  }
  
  // 表单标签样式
  :deep(.arco-form-item-label) {
    color: #D97706;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  // 操作按钮区域
  .form-actions {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
  }
  
  // 按钮样式
  .submit-button {
    background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
    border: none;
    padding: 10px 24px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(245, 158, 11, 0.4);
      background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
    }
  }
  
  .reset-button {
    background: white;
    border: 1px solid #FDE68A;
    padding: 10px 24px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 8px;
    color: #D97706;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      border-color: #F59E0B;
      box-shadow: 0 4px 12px rgba(245, 158, 11, 0.2);
    }
  }
  
  // 响应式设计
  @media (max-width: 768px) {
    .form-item-container {
      width: 280px;
      
      &.textarea-container {
        width: 280px;
      }
    }
    
    .form-actions {
      justify-content: center;
      flex-direction: column;
      gap: 12px;
    }
    
    .form-scroll-wrapper {
      gap: 16px;
    }
  }
</style>
