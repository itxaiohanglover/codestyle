<template>
  <a-drawer v-model:visible="visible" title="${businessName}详情" :width="width >= 600 ? 600 : '100%'" :footer="false">
    <a-descriptions :column="2" size="large" class="general-description">
      <#list fieldConfigs as fieldConfig>
      <a-descriptions-item label="${fieldConfig.comment}">{{ dataDetail?.${fieldConfig.fieldName} }}</a-descriptions-item>
      <#if fieldConfig.fieldName = 'createUser'>
      <a-descriptions-item label="创建人">{{ dataDetail?.createUserString }}</a-descriptions-item>
      <#elseif fieldConfig.fieldName = 'updateUser'>
      <a-descriptions-item label="修改人">{{ dataDetail?.updateUserString }}</a-descriptions-item>
      </#if>
      </#list>
    </a-descriptions>
  </a-drawer>
</template>

<script setup lang="ts">
  import {useWindowSize} from '@vueuse/core'
  import {
        type
        ${classNamePrefix}DetailResp,
        get${classNamePrefix} as getDetail
    } from '@/apis/${apiModuleName}/${apiName}'

const { width } = useWindowSize()

const dataId = ref('')
const dataDetail = ref<${classNamePrefix}DetailResp>()
const visible = ref(false)

// 查询详情
const getDataDetail = async () => {
  const { data } = await getDetail(dataId.value)
  dataDetail.value = data
}

// 打开
const onOpen = async (id: string) => {
  dataId.value = id
  await getDataDetail()
  visible.value = true
}

defineExpose({ onOpen })
</script>

<style scoped lang="scss"></style>
