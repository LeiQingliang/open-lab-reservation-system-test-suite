# syntax=docker/dockerfile:1
# ============================================================================
# 前端镜像：node 构建生产包 → nginx 托管
#   build   阶段用 pnpm 安装依赖并 build:prod 产出 dist
#   runtime 阶段用 nginx 提供静态资源 + 反代 /prod-api 到后端
# 构建上下文(context) = system-under-test/front/RuoYi-Vue3
# ============================================================================

FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
# 先装依赖（利用层缓存）。pnpm 版本由 package.json 的 packageManager 字段固定
COPY package.json pnpm-lock.yaml ./
RUN --mount=type=cache,target=/root/.local/share/pnpm/store \
    pnpm install --frozen-lockfile
# 拷贝源码并打包（生产模式：VITE_APP_BASE_API=/prod-api）
COPY . .
RUN pnpm build:prod

# ----------------------------------------------------------------------------
FROM nginx:1.27-alpine AS runtime
COPY --from=build /app/dist /usr/share/nginx/html
# nginx 站点配置：SPA history 回退 + /prod-api 反代后端（变量+resolver，支持后端重启换 IP）
COPY <<'NGINXCONF' /etc/nginx/conf.d/default.conf
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;
    client_max_body_size 20m;

    # SPA history 模式回退
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 反向代理后端：剥掉 /prod-api 前缀；用变量 + docker DNS 解析，后端重启不失效
    location /prod-api/ {
        resolver 127.0.0.11 valid=10s ipv6=off;
        set $upstream_backend backend;
        rewrite ^/prod-api/(.*)$ /$1 break;
        proxy_pass http://$upstream_backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
    }
}
NGINXCONF
EXPOSE 80
# 用 127.0.0.1 强制走 IPv4：nginx 只 listen 80(IPv4)，而容器内 localhost 会先解析到 ::1，
# busybox wget 连 IPv6 失败会误报 unhealthy（服务其实正常）。
HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=5 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null 2>&1 || exit 1
