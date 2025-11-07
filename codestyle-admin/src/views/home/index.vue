<template>
  <div class="home page">
    <!-- Banner区域 - 黄白主题 -->
    <section class="hero">
      <!-- 主标题区域 -->
      <div class="hero-inner">
        <div class="hero-badge">全新体验</div>
        <h1 class="hero-title">
          <span class="hero-title-gradient">Vibe Coding</span>
          <br />MCP 驱动的代码风格服务器
        </h1>
        <p class="hero-subtitle">
          完全掌控、无限扩展，帮助你的开发团队快速响应变化，显著降低成本。
        </p>
        <div class="hero-actions">
          <a-button
            class="hero-button-primary"
            type="primary"
            size="large"
            @click="goToSpec"
            >下载使用</a-button
          >
          <a-button size="large" class="hero-button-secondary"
            >在线体验</a-button
          >
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">10k+</div>
            <div class="stat-label">活跃开发者</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">500+</div>
            <div class="stat-label">企业用户</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">99.9%</div>
            <div class="stat-label">稳定性保障</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据展示区 -->
    <section class="stats-section">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-number">200K+</div>
          <div class="stat-label">用户数</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-number">100K+</div>
          <div class="stat-label">模板数</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-number">500+</div>
          <div class="stat-label">运行天数</div>
        </div>
      </div>
    </section>

    <!-- 工作流程说明区 -->
    <section class="process-section">
      <div class="process-container">
        <h2 class="section-title">Codestyle工作流程</h2>
        <div class="process-steps">
          <div class="process-step">
            <div class="step-number">Step01</div>
            <div class="step-title">项目模板提取</div>
            <div class="step-description"
              >从现有项目中智能提取可复用的模板组件</div
            >
          </div>
          <div class="process-arrow">→</div>
          <div class="process-step">
            <div class="step-number">Step02</div>
            <div class="step-title">项目模板检索</div>
            <div class="step-description">根据需求快速检索匹配的项目模板</div>
          </div>
          <div class="process-arrow">→</div>
          <div class="process-step">
            <div class="step-number">Step03</div>
            <div class="step-title">基于模板生成</div>
            <div class="step-description">利用模板快速生成符合规范的代码</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 用户案例展示区 -->
    <section class="users-section">
      <div class="users-container">
        <h2 class="section-title">他们正在使用 Codestyle</h2>
        <!-- 滚动容器 -->
        <div
          class="users-scroll-wrapper"
        >
          <div class="users-scroll-container">
            <!-- 第一行 - 慢速度滚动 -->
        <div class="users-row row-1">
          <div class="users-row-inner">
            <div
              v-for="user in users.slice(0, 6)"
              :key="`row1-${user.id}`"
              class="user-card card-row-1"
            >
              <div class="user-info">
                <div class="user-avatar">
                  <img
                    :src="user.avatar"
                    :alt="user.name"
                    @error="handleImageError"
                  />
                </div>
                <div class="user-text">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-company">{{ user.company }}</div>
                  <div class="user-comment">{{
                    truncateComment(user.comment)
                  }}</div>
                </div>
              </div>
            </div>
            <!-- 复制内容用于无缝滚动 -->
            <div
              v-for="user in users.slice(0, 6)"
              :key="`row1-dup-${user.id}`"
              class="user-card card-row-1"
            >
              <div class="user-info">
                <div class="user-avatar">
                  <img
                    :src="user.avatar"
                    :alt="user.name"
                    @error="handleImageError"
                  />
                </div>
                <div class="user-text">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-company">{{ user.company }}</div>
                  <div class="user-comment">{{
                    truncateComment(user.comment)
                  }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 第二行 - 中速度滚动 -->
        <div class="users-row row-2">
          <div class="users-row-inner">
            <div
              v-for="user in users.slice(6, 12)"
              :key="`row2-${user.id}`"
              class="user-card card-row-2"
            >
              <div class="user-info">
                <div class="user-avatar">
                  <img
                    :src="user.avatar"
                    :alt="user.name"
                    @error="handleImageError"
                  />
                </div>
                <div class="user-text">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-company">{{ user.company }}</div>
                  <div class="user-comment">{{
                    truncateComment(user.comment)
                  }}</div>
                </div>
              </div>
            </div>
            <!-- 复制内容用于无缝滚动 -->
            <div
              v-for="user in users.slice(6, 12)"
              :key="`row2-dup-${user.id}`"
              class="user-card card-row-2"
            >
              <div class="user-info">
                <div class="user-avatar">
                  <img
                    :src="user.avatar"
                    :alt="user.name"
                    @error="handleImageError"
                  />
                </div>
                <div class="user-text">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-company">{{ user.company }}</div>
                  <div class="user-comment">{{
                    truncateComment(user.comment)
                  }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 第三行 - 快速度滚动 -->
        <div class="users-row row-3">
          <div class="users-row-inner">
            <div
              v-for="user in users.slice(0, 6)"
              :key="`row3-${user.id}`"
              class="user-card card-row-3"
            >
              <div class="user-info">
                <div class="user-avatar">
                  <img
                    :src="user.avatar"
                    :alt="user.name"
                    @error="handleImageError"
                  />
                </div>
                <div class="user-text">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-company">{{ user.company }}</div>
                  <div class="user-comment">{{
                    truncateComment(user.comment)
                  }}</div>
                </div>
              </div>
            </div>
            <!-- 复制内容用于无缝滚动 -->
            <div
              v-for="user in users.slice(0, 6)"
              :key="`row3-dup-${user.id}`"
              class="user-card card-row-3"
            >
              <div class="user-info">
                <div class="user-avatar">
                  <img
                    :src="user.avatar"
                    :alt="user.name"
                    @error="handleImageError"
                  />
                </div>
                <div class="user-text">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-company">{{ user.company }}</div>
                  <div class="user-comment">{{
                    truncateComment(user.comment)
                  }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 团队介绍区 -->
    <section class="team-section">
      <div class="team-container">
        <h2 class="section-title">我们的团队</h2>
        <div class="team-grid">
          <a
            v-for="member in teamMembers"
            :key="member.id"
            :href="member.githubUrl"
            target="_blank"
            class="team-member"
          >
            <div class="member-avatar">
              <img :src="member.avatar" :alt="member.name" />
            </div>
            <div class="member-name">{{ member.name }}</div>
          </a>
        </div>
      </div>
    </section>

    <!-- 底部网站信息区 -->
    <footer class="footer-section">
      <div class="footer-container">
        <div class="footer-column">
          <h3 class="footer-title">模板库</h3>
          <a-input-search
            placeholder="搜索模板"
            allow-clear
            style="width: 200px"
          />
          <ul class="footer-links">
            <li><a href="#">热门模板</a></li>
            <li><a href="#">最新模板</a></li>
            <li><a href="#">模板分类</a></li>
          </ul>
        </div>
        <div class="footer-column">
          <h3 class="footer-title">MCP</h3>
          <ul class="footer-links">
            <li><a href="#">MCP文档介绍</a></li>
            <li><a href="#">使用指南</a></li>
            <li><a href="#">API参考</a></li>
          </ul>
        </div>
        <div class="footer-column">
          <h3 class="footer-title">规约编程</h3>
          <ul class="footer-links">
            <li><a href="#">规约编程电子书</a></li>
            <li><a href="#">最佳实践</a></li>
            <li><a href="#">案例分享</a></li>
          </ul>
        </div>
        <div class="footer-column">
          <h3 class="footer-title">关注我们</h3>
          <div class="qr-code">
            <img
              src="https://picsum.photos/seed/qrcode/100/100"
              alt="公众号二维码"
              style="width: 100px; height: 100px"
            />
          </div>
          <a-button type="primary" @click="showLoginModal">登录注册</a-button>
        </div>
      </div>
      <div class="footer-copyright">
        <div class="copyright-text"
          >© 2025 上海NoCode网络科技有限公司 ICP沪12345678号-5
          增值电信业务经营许可证</div
        >
        <div class="copyright-links">
          <a href="#">关于我们</a>
          <a href="#">隐私政策</a>
          <a href="#">用户协议</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Input as AInputSearch, Button as AButton } from '@arco-design/web-vue';

const router = useRouter();

// 用户数据
const users = ref([
  {
    id: 1,
    name: '小明同学',
    company: '腾讯科技',
    avatar: 'https://picsum.photos/seed/user1/100/100',
    comment: `使用Codestyle后，我们团队的代码质量提升了50%`,
  },
  {
    id: 2,
    name: 'Bulubulu',
    company: '阿里巴巴',
    avatar: 'https://picsum.photos/seed/user2/100/100',
    comment: `模板检索功能非常强大，大大提高了开发效率`,
  },
  {
    id: 3,
    name: '王大锤',
    company: '字节跳动',
    avatar: 'https://picsum.photos/seed/user3/100/100',
    comment: `企业级解决方案，值得信赖`,
  },
  {
    id: 4,
    name: '小皮球',
    company: '美团',
    avatar: 'https://picsum.photos/seed/user4/100/100',
    comment: `学习成本低，上手很快`,
  },
  {
    id: 5,
    name: '程序员老王',
    company: '滴滴出行',
    avatar: 'https://picsum.photos/seed/user5/100/100',
    comment: `推荐给所有需要标准化代码的团队`,
  },
  {
    id: 6,
    name: 'gzhexuan',
    company: '百度',
    avatar: 'https://picsum.photos/seed/user6/100/100',
    comment: `功能强大，文档齐全`,
  },
  {
    id: 7,
    name: 'pheo',
    company: '网易',
    avatar: 'https://picsum.photos/seed/user7/100/100',
    comment: `帮助我们解决了代码风格不统一的问题`,
  },
  {
    id: 8,
    name: '新用户A',
    company: '华为',
    avatar: 'https://picsum.photos/seed/user8/100/100',
    comment: `华为团队对Codestyle的评价非常高`,
  },
  {
    id: 9,
    name: '新用户B',
    company: '京东',
    avatar: 'https://picsum.photos/seed/user9/100/100',
    comment: `京东团队认为Codestyle是不可或缺的工具`,
  },
  {
    id: 10,
    name: '新用户C',
    company: '拼多多',
    avatar: 'https://picsum.photos/seed/user10/100/100',
    comment: `拼多多团队对Codestyle的功能赞不绝口`,
  },
  {
    id: 11,
    name: '新用户D',
    company: '小米',
    avatar: 'https://picsum.photos/seed/user11/100/100',
    comment: `小米团队认为Codestyle是提升效率的利器`,
  },
  {
    id: 12,
    name: '新用户E',
    company: 'OPPO',
    avatar: 'https://picsum.photos/seed/user12/100/100',
    comment: `OPPO团队对Codestyle的评价非常高`,
  },
]);

// 团队成员数据
const teamMembers = ref([
  {
    id: 1,
    name: '张三',
    avatar: 'https://picsum.photos/seed/team1/100/100',
    githubUrl: 'https://github.com',
  },
  {
    id: 2,
    name: '李四',
    avatar: 'https://picsum.photos/seed/team2/100/100',
    githubUrl: 'https://github.com',
  },
  {
    id: 3,
    name: '王五',
    avatar: 'https://picsum.photos/seed/team3/100/100',
    githubUrl: 'https://github.com',
  },
  {
    id: 4,
    name: '赵六',
    avatar: 'https://picsum.photos/seed/team4/100/100',
    githubUrl: 'https://github.com',
  },
  {
    id: 5,
    name: '钱七',
    avatar: 'https://picsum.photos/seed/team5/100/100',
    githubUrl: 'https://github.com',
  },
  {
    id: 6,
    name: '孙八',
    avatar: 'https://picsum.photos/seed/team6/100/100',
    githubUrl: 'https://github.com',
  },
]);

// 跳转到规约编程页面
const goToSpec = () => {
  router.push('/spec');
};

// 显示登录模态框
const showLoginModal = () => {
  // 触发导航栏中的登录模态框
  window.dispatchEvent(new CustomEvent('showLoginModal'));
};

// 使用CSS动画实现滚动，不再需要JavaScript控制滚动

const handleImageError = (event: Event) => {
  (event.target as HTMLImageElement).src =
    'https://via.placeholder.com/100?text=No+Image';
};

const truncateComment = (comment: string) => {
  const maxLength = 50;
  return comment.length > maxLength
    ? `${comment.slice(0, maxLength)}...`
    : comment;
};

// 移除钩子，因为使用CSS动画实现滚动
</script>

<style lang="less" scoped>
  .home {
    --primary-orange: #ff9500;
    --primary-orange-light: #fff4e5;
    --text-primary: #333333;
    --text-secondary: #666666;
    --border-color: #eeeeee;
    --white: #ffffff;

    padding-bottom: 60px;

    .section-title {
      font-size: 28px;
      font-weight: 600;
      color: var(--text-primary);
      text-align: center;
      margin-bottom: 40px;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -10px;
        left: 50%;
        transform: translateX(-50%);
        width: 60px;
        height: 4px;
        background: linear-gradient(
          90deg,
          var(--primary-orange) 0%,
          #ffb74d 100%
        );
        border-radius: 2px;
      }
    }

    // Hero 区域 - 黄白主题
    .hero {
      position: relative;
      z-index: 1;
      padding: 60px 0 100px;
      background: linear-gradient(135deg, #fffbeb 0%, #ffffff 100%);
    }

    .hero-inner {
      max-width: 900px;
      margin: 0 auto;
      padding: 60px 24px 60px;
      text-align: center;
    }

    .hero-badge {
      display: inline-block;
      padding: 6px 16px;
      background: rgba(251, 191, 36, 0.15);
      color: #f59e0b;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 24px;
      backdrop-filter: blur(10px);
    }

    .hero-title {
      margin: 0 0 20px;
      font-size: 56px;
      line-height: 1.2;
      color: var(--text-primary);
      font-weight: 700;
      letter-spacing: -0.5px;
    }

    .hero-title-gradient {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }

    .hero-subtitle {
      margin: 0 0 32px;
      font-size: 20px;
      color: var(--text-secondary);
      line-height: 1.6;
      max-width: 700px;
      margin-left: auto;
      margin-right: auto;
    }

    .hero-actions {
      display: inline-flex;
      gap: 16px;
      margin-bottom: 60px;
    }

    .hero-button-primary {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      border: none;
      padding: 14px 32px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
      box-shadow: 0 10px 30px rgba(245, 158, 11, 0.4);
      transition: all 0.3s ease;
    }

    .hero-button-primary:hover {
      transform: translateY(-3px);
      box-shadow: 0 12px 36px rgba(245, 158, 11, 0.5);
    }

    .hero-button-secondary {
      background: rgba(255, 255, 255, 0.95);
      border: 1px solid rgba(224, 226, 236, 0.5);
      padding: 14px 32px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
      color: var(--text-primary);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;
    }

    .hero-button-secondary:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    .hero-stats {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 32px;
      padding: 24px 0;
      border-top: 1px solid rgba(224, 226, 236, 0.3);
      border-bottom: 1px solid rgba(224, 226, 236, 0.3);
    }

    .hero-stats .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0 20px;
    }

    .hero-stats .stat-number {
      font-size: 36px;
      font-weight: 700;
      color: #f59e0b;
      margin-bottom: 8px;
    }

    .hero-stats .stat-label {
      font-size: 14px;
      color: var(--text-secondary);
    }

    .hero-stats .stat-divider {
      width: 1px;
      height: 40px;
      background-color: var(--border-color);
    }

    // 适配移动端
    @media (max-width: 768px) {
      .hero-title {
        font-size: 42px;
      }

      .hero-actions {
        flex-direction: column;
        align-items: center;
        width: 100%;
      }

      .hero-stats {
        flex-direction: column;
        gap: 16px;
      }

      .hero-stats .stat-divider {
        width: 40px;
        height: 1px;
      }
    }

    // 统计数据展示区
    .stats-section {
      background-color: var(--white);
      padding: 40px 0;
      margin-bottom: 60px;

      .stats-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
        display: flex;
        justify-content: center;
        align-items: center;

        @media (max-width: 768px) {
          flex-direction: column;
        }
      }

      .stat-item {
        text-align: center;
        padding: 0 40px;

        @media (max-width: 768px) {
          padding: 20px 0;
        }
      }

      .stat-number {
        font-size: 36px;
        font-weight: 700;
        color: var(--primary-orange);
        margin-bottom: 10px;
      }

      .stat-label {
        font-size: 16px;
        color: var(--text-secondary);
      }

      .stat-divider {
        width: 1px;
        height: 60px;
        background-color: var(--border-color);

        @media (max-width: 768px) {
          width: 60px;
          height: 1px;
        }
      }
    }

    // 工作流程说明区
    .process-section {
      padding: 60px 0;
      margin-bottom: 60px;

      .process-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
      }

      .process-steps {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 30px;
        flex-wrap: wrap;
      }

      .process-step {
        background-color: var(--white);
        padding: 30px;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        text-align: center;
        width: 280px;
        transition: transform 0.3s ease, box-shadow 0.3s ease;

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
        }
      }

      .step-number {
        font-size: 18px;
        font-weight: 600;
        color: var(--primary-orange);
        margin-bottom: 15px;
      }

      .step-title {
        font-size: 20px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 10px;
      }

      .step-description {
        font-size: 14px;
        color: var(--text-secondary);
        line-height: 1.6;
      }

      .process-arrow {
        font-size: 24px;
        color: var(--primary-orange);
        @media (max-width: 768px) {
          display: none;
        }
      }
    }

    // 用户案例展示区
    .users-section {
      padding: 60px 0;
      margin-bottom: 60px;
      background-color: var(--primary-orange-light);

      .users-container {
        max-width: 100%;
        margin: 0 auto;
        padding: 0 20px;
      }

      /* 滚动容器样式 */
      .users-scroll-wrapper {
        overflow: hidden;
        position: relative;
        width: 100%;
        height: 520px; /* 增加高度，确保完整显示三行 */
        padding: 20px 0; /* 添加上下内边距 */
      }

      .users-scroll-container {
        width: 100%;
        height: 100%;
        position: relative;
      }

      /* 每行样式 - 设置不同的对齐方式 */
      .users-row {
        position: absolute;
        width: 100%;
        overflow: hidden;
        height: 160px; /* 增加行高，给卡片留出足够空间 */
        padding: 10px 0; /* 添加行间距 */
      }

      .row-1 {
        top: 0;
        left: 0;
      }

      .row-2 {
        top: 180px; /* 调整间距 */
        left: 40px;
      }

      .row-3 {
        top: 360px; /* 调整间距 */
        left: 20px;
      }

      /* 行内滚动容器 */
      .users-row-inner {
        display: flex;
        width: 200%; /* 宽度是内容的两倍以实现无缝滚动 */
      }

      /* 滚动动画逻辑修改 - 第一行和第三行向左，第二行向右滚动 */
      .row-1 .users-row-inner,
      .row-3 .users-row-inner {
        animation: scroll 40s linear infinite;
      }

      .row-2 .users-row-inner {
        animation: scrollReverse 40s linear infinite;
      }

      /* 统一呼吸效果 - 所有行使用相同的呼吸动画 */
      .user-card {
        animation: breathe 8s ease-in-out infinite alternate;
      }

      @keyframes breathe {
        0% {
          transform: scale(1) translateY(0);
        }
        100% {
          transform: scale(1.02) translateY(-2px);
        }
      }

      /* 滚动动画关键帧定义 */
      @keyframes scrollReverse {
        0% {
          transform: translateX(-50%);
        }
        100% {
          transform: translateX(0);
        }
      }

      /* 用户卡片样式 - 统一宽高比例 */
      .user-card {
        background-color: var(--white);
        padding: 20px;
        border-radius: 16px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
        text-align: left;
        transition: all 0.3s ease;
        height: 140px;
        width: 280px;
        margin-right: 24px;
        position: relative;
        overflow: hidden;

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
        }

        .user-info {
          display: flex;
          height: 100%;
        }
      }

      /* 用户头像样式 */
      .user-avatar {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 16px;
        flex-shrink: 0;
        border: 3px solid rgba(255, 149, 0, 0.1);

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .user-text {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
      }

      /* 用户信息样式 */
      .user-name {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .user-company {
        font-size: 14px;
        color: var(--primary-orange);
        margin: 0 0 8px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        opacity: 0.9;
      }

      .user-comment {
        font-size: 14px;
        color: var(--text-secondary);
        line-height: 1.5;
        margin: 0;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;
        text-overflow: ellipsis;
        opacity: 0.85;
        flex: 1;
      }

      /* 响应式调整 */
      @media (max-width: 768px) {
        .users-scroll-wrapper {
          height: 400px;
        }

        .users-row {
          height: 120px;
        }

        .row-2 {
          top: 140px;
          left: 20px;
        }

        .row-3 {
          top: 280px;
          left: 10px;
        }

        .user-card {
          width: 200px;
          height: 100px; /* 保持2:1的比例 */
          margin-right: 20px;
          padding: 12px;
        }

        .user-avatar {
          width: 50px;
          height: 50px;
        }
      }
    }

    // 团队介绍区
    .team-section {
      padding: 60px 0;
      margin-bottom: 60px;

      .team-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
      }

      .team-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 30px;
        justify-items: center;
      }

      .team-member {
        text-align: center;
        transition: transform 0.3s ease;
        color: var(--text-primary);
        text-decoration: none;

        &:hover {
          transform: translateY(-5px);
        }
      }

      .member-avatar {
        width: 120px;
        height: 120px;
        border-radius: 50%;
        overflow: hidden;
        margin-bottom: 16px;
        border: 3px solid var(--primary-orange);

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .member-name {
        font-size: 16px;
        font-weight: 500;
      }
    }

    // 底部网站信息区
    .footer-section {
      background-color: var(--white);
      border-top: 1px solid var(--border-color);
      padding-top: 60px;

      .footer-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 40px;
        margin-bottom: 40px;
      }

      .footer-column {
        .footer-title {
          font-size: 20px;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 20px;
        }

        .footer-links {
          list-style: none;
          padding: 0;
          margin: 0;
          margin-top: 20px;

          li {
            margin-bottom: 12px;

            a {
              color: var(--text-secondary);
              text-decoration: none;
              transition: color 0.3s ease;

              &:hover {
                color: var(--primary-orange);
              }
            }
          }
        }
      }

      .footer-copyright {
        background-color: var(--primary-orange-light);
        padding: 20px 0;
        text-align: center;

        .copyright-text {
          font-size: 14px;
          color: var(--text-secondary);
          margin-bottom: 10px;
        }

        .copyright-links {
          display: flex;
          justify-content: center;
          gap: 20px;

          a {
            font-size: 14px;
            color: var(--text-secondary);
            text-decoration: none;

            &:hover {
              color: var(--primary-orange);
            }
          }
        }
      }

      .ant-btn-primary {
        background-color: var(--primary-orange);
        border-color: var(--primary-orange);
        margin-top: 20px;

        &:hover {
          background-color: #ff8c00;
          border-color: #ff8c00;
        }
      }
    }

    @keyframes scroll {
      0% {
        transform: translateX(0);
      }
      100% {
        transform: translateX(-50%);
      }
    }
  }
</style>
