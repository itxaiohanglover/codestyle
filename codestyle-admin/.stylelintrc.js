module.exports = {
  extends: [
    // 'stylelint-config-standard', // 注释掉严格的标准配置
    // 'stylelint-config-rational-order', // 注释掉严格的顺序配置
    'stylelint-config-prettier',
    // 'stylelint-config-recommended-vue', // 注释掉Vue推荐配置
  ],
  defaultSeverity: 'ignore', // 忽略所有警告
  plugins: [/* 'stylelint-order' */], // 注释掉order插件
  rules: {
    'at-rule-no-unknown': 0, // 禁用未知at规则检查
    'rule-empty-line-before': 0, // 禁用规则前空行检查
    'selector-pseudo-class-no-unknown': 0, // 禁用未知伪类检查
    'declaration-block-no-duplicate-properties': 0, // 允许重复属性
    'property-no-unknown': 0, // 允许未知属性
    'selector-type-no-unknown': 0, // 允许未知选择器类型
    'no-descending-specificity': 0, // 禁用特异性检查
    'color-no-invalid-hex': 0, // 禁用无效十六进制颜色检查
    'unit-no-unknown': 0, // 允许未知单位
    'selector-class-pattern': 0, // 禁用类名模式检查
    'block-no-empty': 0, // 允许空块
    'comment-no-empty': 0, // 允许空注释
  },
};
