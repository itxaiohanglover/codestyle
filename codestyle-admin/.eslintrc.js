// eslint-disable-next-line @typescript-eslint/no-var-requires
const path = require('path');

module.exports = {
  root: true,
  parser: 'vue-eslint-parser',
  parserOptions: {
    // Parser that checks the content of the <script> tag
    parser: '@typescript-eslint/parser',
    sourceType: 'module',
    ecmaVersion: 2020,
    ecmaFeatures: {
      jsx: true,
    },
  },
  env: {
    'browser': true,
    'node': true,
    'vue/setup-compiler-macros': true,
  },
  plugins: ['@typescript-eslint'],
  extends: [
    // Airbnb JavaScript Style Guide https://github.com/airbnb/javascript
    // 'airbnb-base', // 注释掉严格的Airbnb规则
    'plugin:@typescript-eslint/recommended',
    // 'plugin:import/recommended', // 注释掉import相关严格规则
    // 'plugin:import/typescript',
    'plugin:vue/vue3-recommended',
    'plugin:prettier/recommended',
  ],
  settings: {
    'import/resolver': {
      typescript: {
        project: path.resolve(__dirname, './tsconfig.json'),
      },
    },
  },
  rules: {
    'prettier/prettier': 0, // 禁用Prettier错误
    // Vue: Recommended rules to be closed or modify
    'vue/require-default-prop': 0,
    'vue/singleline-html-element-content-newline': 0,
    'vue/max-attributes-per-line': 0,
    'vue/html-indent': 0, // 禁用HTML缩进检查
    'vue/attributes-order': 0, // 禁用属性顺序检查
    'vue/no-mutating-props': 0, // 允许修改props
    'vue/attribute-hyphenation': 0, // 禁用属性连字符检查
    'vue/no-parsing-error': 0, // 禁用解析错误检查
    // Vue: Add extra rules
    'vue/custom-event-name-casing': 0, // 降低为警告
    'vue/no-v-text': 0, // 禁用
    'vue/padding-line-between-blocks': 0, // 禁用
    'vue/require-direct-export': 0, // 禁用
    'vue/multi-word-component-names': 0,
    // TypeScript规则
    '@typescript-eslint/ban-ts-comment': 0,
    '@typescript-eslint/no-unused-vars': 0, // 禁用未使用变量检查
    '@typescript-eslint/no-empty-function': 0, // 禁用空函数检查
    '@typescript-eslint/no-explicit-any': 0,
    '@typescript-eslint/no-var-requires': 0, // 允许使用require
    '@typescript-eslint/ban-types': 0, // 禁用类型检查
    '@typescript-eslint/explicit-module-boundary-types': 0, // 禁用导出类型检查
    // 其他JavaScript规则
    'no-debugger': 0, // 总是允许debugger
    'no-param-reassign': 0,
    'prefer-regex-literals': 0,
    'import/no-extraneous-dependencies': 0,
    'no-console': 0, // 允许console
    'import/order': 0, // 禁用导入顺序检查
    'no-shadow': 0, // 禁用变量阴影检查
    'consistent-return': 0, // 禁用一致返回检查
    'no-plusplus': 0, // 允许++运算符
    'func-names': 0, // 禁用函数命名要求
    'no-restricted-syntax': 0, // 禁用语法限制
  },
};
