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
    :width="960"
    :mask-closable="true"
    unmount-on-close
    centered
    :modal-style="{ borderRadius: '16px', overflow: 'hidden', boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)' }"
    class="custom-auth-modal"
    @cancel="authVisible = false"
  >
    <div class="auth-modal">
      <div class="auth-left">
        <div class="logo-section">
          <div class="logo-icon">V.</div>
          <div class="logo-text">CodeStyle Admin</div>
        </div>
        <div class="welcome-message">
          <div class="welcome-title">欢迎回来!</div>
          <div class="welcome-desc">使用您的账户登录以访问完整功能，管理您的项目和团队。</div>
        </div>
        <a-button 
          @click="authMode = authMode === 'login' ? 'register' : 'login'"
          type="text"
          class="transform-button left-toggle"
          style="color: white; border: 1px solid rgba(255, 255, 255, 0.3);"
        >
          {{ authMode === 'login' ? '还不是用户? 立即注册' : '已有账户? 立即登录' }}
        </a-button>
      </div>
      
      <div class="auth-right">
        <div class="auth-header">
          <h2 class="auth-title">{{ authMode === 'login' ? '登录' : '注册' }}</h2>
          <div class="switch-tip">
            {{ authMode === 'login' ? '还没有账户?' : '已有账户?' }}
            <a-link
              @click="authMode = authMode === 'login' ? 'register' : 'login'"
              class="switch-link"
            >
              {{ authMode === 'login' ? '立即注册' : '立即登录' }}
            </a-link>
          </div>
        </div>
        
        <div class="auth-form">
          <a-form layout="vertical">
            <a-form-item
              field="email"
              :label="authMode === 'login' ? '电子邮件或用户名' : '电子邮件'"
              :rules="[{ required: true, message: '请输入' + (authMode === 'login' ? '用户名' : '您的邮箱') }]"
            >
              <a-input
                 v-model="auth.email"
                 :placeholder="authMode === 'login' ? '请输入您的邮箱或用户名' : '请输入您的邮箱'"
                 size="large"
                 class="form-input"
              >
                <template #prefix>
                  <a-icon name="user" :style="{ color: 'var(--color-text-4)' }" />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item 
              field="password" 
              label="密码"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
                  <a-input-password
                      v-model="auth.password"
                      :placeholder="authMode === 'login' ? '请输入您的密码' : '请设置密码'"
                      size="large"
                      class="form-input"
                  >
                    <template #prefix>
                      <a-icon name="lock" :style="{ color: 'var(--color-text-4)' }" />
                    </template>
                  </a-input-password>
                </a-form-item>

                <a-form-item v-if="authMode === 'register'"
                  field="confirm"
                  label="确认密码"
                  :rules="[{ required: true, message: '请再次输入密码' }]"
                >
                  <a-input-password
                    v-model="auth.confirm"
                    placeholder="请再次输入密码"
                    size="large"
                    class="form-input"
                  >
                    <template #prefix>
                      <a-icon name="lock" :style="{ color: 'var(--color-text-4)' }" />
                    </template>
                  </a-input-password>
                </a-form-item>

                <div class="form-options">
                  <a-checkbox v-if="authMode === 'login'" v-model="auth.remember"
                    class="remember-checkbox"
                  >
                    记住我状态
                  </a-checkbox>
                  <a-link v-if="authMode === 'login'" class="forgot-link"
                  >
                    忘记密码?
                  </a-link>
                </div>
                
                <a-button
                  type="primary"
                  long
                  size="large"
                  class="transform-button"
                  :loading="authLoading"
                  @click="authMode === 'login' ? handleLogin() : handleRegister()"
                >
                  {{ authMode === 'login' ? '立即登录' : '立即注册' }}
                </a-button>
              </a-form>
            </div>
            
            <div class="social-section">
              <div class="social-divider">
                <span>或使用以下方式登录</span>
              </div>
              <a-space :size="16" class="social-buttons">
                <a-button 
                  type="default"
                  shape="circle" 
                  :style="{ borderColor: '#000', color: '#000', width: '40px', height: '40px' }"
                >
                  <template #icon>
                    <a-icon name="icon-Apple" />
                  </template>
                </a-button>
                <a-button 
                  type="default"
                  shape="circle" 
                  :style="{ borderColor: '#24292e', color: '#24292e', width: '40px', height: '40px' }"
                >
                  <template #icon>
                    <a-icon name="icon-LogoGithub" />
                  </template>
                </a-button>
                <a-button 
                  type="default"
                  shape="circle" 
                  :style="{ borderColor: '#1DA1F2', color: '#1DA1F2', width: '40px', height: '40px' }"
                >
                  <template #icon>
                    <a-icon name="icon-LogoTwitter" />
                  </template>
                </a-button>
              </a-space>
            </div>
            
            <div class="auth-footer">
              <div class="terms-text">
                登录即表示您同意我们的
                <a-link size="small">服务条款</a-link>
                和
                <a-link size="small">隐私政策</a-link>
              </div>
            </div>
          </div>
        </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
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
  const authLoading = ref(false);
  const auth = ref({ email: 'admin', password: 'admin', confirm: '', remember: true });
  const openAuth = (mode: AuthMode) => {
    authMode.value = mode;
    authVisible.value = true;
  };
  const handleLogin = async () => {
    try {
      authLoading.value = true;
      // 模拟登录请求
      await new Promise(resolve => setTimeout(resolve, 1000));
      // 存储用户信息
      sessionStorage.setItem('user', JSON.stringify({
        email: auth.value.email,
        name: auth.value.email.split('@')[0],
      }));
      // 记住密码逻辑
      if (auth.value.remember) {
        localStorage.setItem('remembered_email', auth.value.email);
      } else {
        localStorage.removeItem('remembered_email');
      }
      authVisible.value = false;
      // 液体扩散动画
      inkActive.value = true;
      setTimeout(() => {
        inkActive.value = false;
        router.push('/');
      }, 500);
    } catch (error) {
      console.error('Login failed:', error);
      // 这里可以添加错误提示
    } finally {
      authLoading.value = false;
    }
  };
  const handleRegister = async () => {
    try {
      authLoading.value = true;
      // 简单的密码验证
      if (authMode.value === 'register' && auth.value.password !== auth.value.confirm) {
        throw new Error('密码不匹配');
      }
      // 模拟注册请求
      await new Promise(resolve => setTimeout(resolve, 1000));
      // 存储用户信息
      sessionStorage.setItem('user', JSON.stringify({
        email: auth.value.email,
        name: auth.value.email.split('@')[0],
      }));
      authVisible.value = false;
      // 液体扩散动画
      inkActive.value = true;
      setTimeout(() => {
        inkActive.value = false;
        router.push('/');
      }, 500);
    } catch (error) {
      console.error('Register failed:', error);
      // 这里可以添加错误提示
    } finally {
      authLoading.value = false;
    }
  };
  
  // 初始化时检查记住的邮箱
  onMounted(() => {
    const rememberedEmail = localStorage.getItem('remembered_email');
    if (rememberedEmail) {
      auth.value.email = rememberedEmail;
      auth.value.remember = true;
    }
  });

  // 顶栏液体扩散动画 + 路由跳转
  const topbarRef = ref<HTMLElement | null>(null);
  const inkActive = ref(false);
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
        font-size: 24px;
    font-weight: 600;
    color: white;
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
  /* 登录模态框主容器 */
  .auth-modal {
    display: grid;
    grid-template-columns: 1.2fr 1fr;
    min-height: 600px;
  }
  
  /* 左侧欢迎区域 */
  .auth-left {
    background: linear-gradient(135deg, #ff6b00 0%, #ff9900 100%);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 40px;
    color: white;
  }
  
  .logo-section {
    display: flex;
    align-items: center;
    margin-bottom: 40px;
  }
  
  .logo-icon {
    font-size: 36px;
    margin-right: 12px;
  }
  
  .welcome-message {
    text-align: center;
    margin-bottom: 40px;
  }
  
  .welcome-title {
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 12px;
  }
  
  .welcome-desc {
    font-size: 16px;
    opacity: 0.9;
    line-height: 1.6;
  }
  
  /* 右侧表单区域 */
  .auth-right {
    padding: 40px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    background: white;
  }
  
  .auth-header {
    margin-bottom: 32px;
  }
  
  .auth-title {
    font-size: 32px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 8px;
  }
  
  .switch-tip {
    color: #666;
    font-size: 14px;
  }
  
  .switch-link {
    color: #ff6b00;
    font-weight: 500;
  }
  
  .auth-form {
    flex: 1;
  }
  
  .form-input {
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    font-size: 15px;
  }
  
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }
  
  .remember-checkbox {
    font-size: 14px;
    color: #666;
  }
  
  .forgot-link {
    font-size: 14px;
    color: #ff6b00;
  }
  
    .transform-button {
    transition: all 0.3s ease;
  }
  .transform-button:hover {
    background: linear-gradient(90deg, #e55e00, #ff8c00) !important;
    color: #fff !important;
    background-color: transparent;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 107, 0, 0.3);
  }
  .auth-button {
    background: linear-gradient(90deg, #ff6b00, #ff9900);
    border: none;
    border-radius: 12px;
    padding: 12px 24px;
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 24px;
  }
  
  .social-section {
    margin-top: 32px;
  }
  
  .social-divider {
    text-align: center;
    margin-bottom: 20px;
  }
  
  .social-divider span {
    font-size: 14px;
    color: #999;
  }
  
  .social-buttons {
    justify-content: center;
  }
  
  .auth-footer {
    margin-top: 24px;
    text-align: center;
  }
  
  .terms-text {
    font-size: 12px;
    color: #999;
    line-height: 1.5;
  }
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .auth-modal {
      grid-template-columns: 1fr;
    }
    .auth-left {
      display: none;
    }
    .auth-right {
      padding: 24px;
    }
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
