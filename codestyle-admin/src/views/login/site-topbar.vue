<template>
  <div ref="topbarRef" class="topbar">
    <div class="left">
      <div class="logo">
        <span class="logo-icon">🐝</span>
        <span class="logo-text">码风</span>
      </div>
    </div>
    <div class="center">
      <a-link class="nav-item" @click.prevent="navigateWithInk('/template', $event)">模板库</a-link>
      <a-link class="nav-item" @click.prevent="navigateWithInk('/mcp', $event)">MCP</a-link>
      <a-link class="nav-item" @click.prevent="navigateWithInk('/spec', $event)">规约编程</a-link>
      <a-input-search
        v-model="keyword"
        allow-clear
        placeholder="搜索模板、MCP、规约..."
        :style="{ width: '400px', maxWidth: '45vw' }"
        class="rounded-search"
        @search="onSearch"
      />
    </div>
    <div class="right">
      <a-link class="nav-item login-btn" @click.prevent="openAuth('login')">登录</a-link>
    </div>
    <!-- 顶栏内部液体线条 -->
    <div
      v-show="ink.visible"
      class="ink-line"
      :style="ink.style as any"
    ></div>
  </div>
  <!-- 顶栏液体扩散层 -->
  <div v-if="inkActive" class="ink-splash play"></div>
  <a-modal
    :visible="authVisible"
    :footer="false"
    :width="900"
    :mask-closable="true"
    unmount-on-close
    :modal-style="{ position: 'relative' }"
    class="custom-auth-modal"
    @cancel="authVisible = false"
  >
    <div class="auth-modal">
      <div class="auth-left">
        <div class="welcome">欢迎！</div>
        <div class="brand">V.</div>
        <div class="smile">:)</div>
        <div class="switch-tip">
          {{ authMode === 'login' ? '还不是成员？' : '已经注册了？' }}
          <a-link
            @click="authMode = authMode === 'login' ? 'register' : 'login'"
          >
            {{ authMode === 'login' ? '立即注册' : '去登录' }}
          </a-link>
        </div>
      </div>
      <div class="auth-right">
        <a-typography-title
          :heading="1"
          :style="{ marginTop: '0', marginBottom: '24px' }"
        >
          {{ authMode === 'login' ? '登录' : '注册' }}
        </a-typography-title>
        <a-form layout="vertical">
          <a-form-item
            field="email"
            label="电子邮件或用户名"
            style="margin-top: 20px"
          >
            <a-input
              v-model="auth.email"
              placeholder="电子邮件或用户名"
              style="margin-top: 20px"
            />
          </a-form-item>
          <a-form-item field="password" label="密码" style="margin-top: 20px">
            <a-input-password
              v-model="auth.password"
              placeholder="密码"
              style="margin-top: 20px"
            />
          </a-form-item>

          <a-checkbox v-if="authMode === 'login'" v-model="auth.remember"
            >保持登录状态</a-checkbox
          >
          <a-button
            type="primary"
            long
            :style="{ marginTop: '12px' }"
            style="margin-top: 40px"
          >
            {{ authMode === 'login' ? '立即登录' : '立即注册' }}
          </a-button>
        </a-form>
        <div class="social">
          <span>或使用</span>
          <a-space :size="10" style="margin-top: 8px; display: block">
            <a-button shape="round">G 谷歌</a-button>
            <a-button shape="round">F 脸书</a-button>
            <a-button shape="round">X 响</a-button>
          </a-space>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue';
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const keyword = ref('');
  const onSearch = () => {
    const q = (keyword.value || '').trim();
    router.push({ name: 'search', query: { q } });
  };

  type AuthMode = 'login' | 'register';
  const authVisible = ref(false);
  const authMode = ref<AuthMode>('login');
  const auth = ref({ email: '', password: '', confirm: '', remember: true });
  const openAuth = (mode: AuthMode) => {
    authMode.value = mode;
    authVisible.value = true;
  };

  // 顶栏液体扩散动画 + 路由跳转
  const topbarRef = ref<HTMLElement | null>(null);
  const ink = reactive({ visible: false, style: { left: '0px', top: '0px', width: '0px', height: '0px', borderRadius: '12px', transition: 'all 360ms cubic-bezier(0.22, 0.61, 0.36, 1)', background: 'rgba(255,255,255,0.95)' } });

  const triggerInkFrom = (targetEl: HTMLElement) => {
    const bar = topbarRef.value;
    if (!bar) return;
    const barRect = bar.getBoundingClientRect();
    const linkRect = targetEl.getBoundingClientRect();

    // 起始：从被点击的链接位置和尺寸开始（先去掉过渡，确保每次都能重新起跳）
    ink.visible = true;
    ink.style.transition = 'none';
    ink.style.left = `${linkRect.left - barRect.left}px`;
    ink.style.top = `${linkRect.top - barRect.top}px`;
    ink.style.width = `${linkRect.width}px`;
    ink.style.height = `${Math.max(28, linkRect.height)}px`;
    ink.style.borderRadius = '14px';

    // 强制回流，应用起始状态
    (bar as HTMLElement).getBoundingClientRect();

    // 恢复过渡并下滑到顶栏底部，同时横向铺满
    ink.style.transition = 'left 360ms cubic-bezier(0.22,0.61,0.36,1), top 320ms cubic-bezier(0.22,0.61,0.36,1), width 420ms cubic-bezier(0.22,0.61,0.36,1), height 280ms cubic-bezier(0.22,0.61,0.36,1), border-radius 300ms ease';
    requestAnimationFrame(() => {
      const lineH = 14; // 稍厚一点，更有“液体”质感
      ink.style.left = '0px';
      ink.style.top = `${barRect.height - lineH}px`;
      ink.style.width = `${barRect.width}px`;
      ink.style.height = `${lineH}px`;
      ink.style.borderRadius = '18px';
    });
  };

  const navigateWithInk = (to: string, evt: Event) => {
    const current = evt.currentTarget as HTMLElement;
    triggerInkFrom(current);
    // 稍延时后跳转，液体仍然保持不消失
    setTimeout(() => router.push(to), 120);
  };
</script>

<style scoped>
  .topbar {
    position: sticky;
    top: 0;
    z-index: 10;
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    align-items: center;
    height: 72px;
    padding: 0 32px;
    background: linear-gradient(135deg, #ffffff 0%, #fff9f0 100%);
    -webkit-backdrop-filter: saturate(180%) blur(10px);
    backdrop-filter: saturate(180%) blur(10px);
    border-bottom: 1px solid rgba(255, 153, 0, 0.1);
  }
  .left {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 24px;
  }
  .logo-icon {
    font-size: 32px;
  }
  .logo-text {
    background: linear-gradient(90deg, #ff6b00, #ff9900);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  .center {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
  }
  .right {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    align-items: center;
  }

  /* 新：顶栏内部“液体线条”，只在顶栏内移动，不外溢，也不消失 */
  .ink-line {
    position: absolute;
    left: 0;
    top: 0;
    height: 12px;
    /* 多重径向渐变制造“流动/团块”质感 */
    background:
      radial-gradient(16px 12px at 8% 60%, rgba(255,255,255,.95) 0%, rgba(255,255,255,.0) 65%),
      radial-gradient(22px 14px at 22% 40%, rgba(255,255,255,.95) 0%, rgba(255,255,255,.0) 68%),
      radial-gradient(18px 12px at 38% 65%, rgba(255,255,255,.95) 0%, rgba(255,255,255,.0) 70%),
      radial-gradient(26px 14px at 56% 45%, rgba(255,255,255,.95) 0%, rgba(255,255,255,.0) 70%),
      radial-gradient(18px 12px at 72% 60%, rgba(255,255,255,.95) 0%, rgba(255,255,255,.0) 68%),
      radial-gradient(22px 14px at 88% 50%, rgba(255,255,255,.95) 0%, rgba(255,255,255,.0) 70%),
      linear-gradient(to right, rgba(255,255,255,.75), rgba(255,255,255,.75));
    background-size: 140px 100%, 160px 100%, 180px 100%, 200px 100%, 160px 100%, 180px 100%, 100% 100%;
    background-repeat: repeat-x;
    animation: inkWave 3.2s linear infinite;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08), inset 0 0 14px rgba(255,255,255,0.65);
    filter: saturate(110%) blur(0.2px);
    border-radius: 18px;
    pointer-events: none;
    z-index: 0; /* 保持在内容下方 */
  }
  .left, .center, .right { position: relative; z-index: 1; }

  /* 让径向团块在水平方向缓慢流动，产生“流动感” */
  @keyframes inkWave {
    0% { background-position: 0 0, 0 0, 0 0, 0 0, 0 0, 0 0, 0 0; }
    100% { background-position: 140px 0, 160px 0, 180px 0, 200px 0, 160px 0, 180px 0, 0 0; }
  }

  .nav-item,
  .nav-item:visited {
    color: #333 !important;
    font-size: 16px;
    font-weight: 500;
    padding: 8px 16px;
    border-radius: 8px;
    transition: all 0.3s ease;
  }
  .nav-item:hover {
    color: #ff6b00 !important;
    background-color: rgba(255, 153, 0, 0.1);
    text-decoration: none;
  }
  .login-btn {
    background: linear-gradient(90deg, #ff6b00, #ff9900);
    color: white !important;
    font-weight: 600;
    padding: 8px 20px;
  }
  .login-btn:hover {
    background: linear-gradient(90deg, #e55e00, #ff8c00);
    color: white !important;
    background-color: transparent;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 107, 0, 0.3);
  }

  /* 搜索框外层样式与圆角 */
  :deep(.arco-input-wrapper.arco-input-search) {
    --border-radius-small: 24px;
    border-radius: 24px !important;
    background-color: white;
    border: 1px solid rgba(255, 153, 0, 0.2);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
  }
  :deep(.arco-input-wrapper.arco-input-search:hover) {
    border-color: rgba(255, 153, 0, 0.4);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  /* 去掉输入框/前后缀的竖线与边框以及光标 */
  :deep(.arco-input-wrapper.arco-input-search .arco-input) {
    border: none !important;
    box-shadow: none !important;
    caret-color: transparent;
  }
  :deep(.arco-input-prefix),
  :deep(.arco-input-suffix),
  :deep(.arco-input-group-addafter),
  :deep(.arco-input-group-addbefore) {
    border: none !important;
  }

  /* 移除渐变动画，使用简洁风格 */
</style>

<style scoped>
  .auth-modal {
    display: grid;
    grid-template-columns: 1.2fr 1fr;
    min-height: 560px; /* 增加整体高度 */
  }
  .auth-left {
    background: #f5f5f5;
    border-radius: 8px;
    margin-right: 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%; /* 确保左侧区域占满高度 */
  }
  .welcome {
    font-size: 20px;
    margin-bottom: 20px;
  }
  .brand {
    font-size: 56px;
    font-weight: 800;
  }
  .smile {
    font-size: 48px;
    margin: 10px 0 40px;
  }
  .switch-tip {
    color: var(--color-text-2);
  }
  .auth-right {
    padding-left: 20px; /* 增加内边距 */
    padding-right: 40px;
    padding-top: 80px;
    padding-bottom: 0px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    height: 100%; /* 确保右侧区域占满高度 */
  }
  .social {
    margin-top: 20px; /* 增加社交按钮上边距 */
    color: var(--color-text-2);
  }
</style>

<style>
  /* 放大并固定关闭按钮到右上角 */
  .custom-auth-modal .arco-modal-close-btn {
    position: absolute;
    top: 16px !important;
    right: 16px !important;
    width: 36px !important;
    height: 36px !important;
    z-index: 1001;
  }
  .custom-auth-modal .arco-modal-close-icon {
    font-size: 22px !important;
  }
  /* 调整模态框内容区域高度 */
  .custom-auth-modal .arco-modal-body {
    padding: 0;
  }
</style>
