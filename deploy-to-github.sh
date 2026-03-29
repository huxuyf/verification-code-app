#!/bin/bash

# GitHub 部署脚本
# 用于将项目推送到 GitHub 并触发 GitHub Actions 构建

set -e

echo "=========================================="
echo "  GitHub 部署脚本"
echo "=========================================="
echo ""

# 检查是否已初始化 git
if [ ! -d ".git" ]; then
    echo "初始化 Git 仓库..."
    git init
    echo "✅ Git 仓库初始化完成"
else
    echo "✅ Git 仓库已存在"
fi

# 检查是否有远程仓库
if git remote get-url origin > /dev/null 2>&1; then
    echo "✅ 远程仓库已配置: $(git remote get-url origin)"
    echo ""
    read -p "是否要更新远程仓库 URL? (y/n): " update_remote
    if [ "$update_remote" = "y" ]; then
        read -p "请输入 GitHub 仓库 URL: " repo_url
        git remote set-url origin "$repo_url"
        echo "✅ 远程仓库已更新"
    fi
else
    echo "📝 配置远程仓库..."
    read -p "请输入 GitHub 仓库 URL: " repo_url
    git remote add origin "$repo_url"
    echo "✅ 远程仓库已配置"
fi

echo ""
echo "📦 准备提交代码..."

# 添加所有文件
git add .

# 检查是否有更改
if git diff --cached --quiet; then
    echo "⚠️  没有需要提交的更改"
    exit 0
fi

# 提交
echo "请输入提交信息（留空使用默认信息）:"
read commit_message
if [ -z "$commit_message" ]; then
    commit_message="feat: initial commit - Android verification code app"
fi

git commit -m "$commit_message"
echo "✅ 代码已提交"

echo ""
echo "🚀 推送到 GitHub..."

# 检查主分支名称
if git show-ref --verify --quiet refs/heads/main; then
    main_branch="main"
elif git show-ref --verify --quiet refs/heads/master; then
    main_branch="master"
else
    main_branch="main"
    git branch -M main
fi

# 推送代码
read -p "是否要推送到远程仓库? (y/n): " push_code
if [ "$push_code" = "y" ]; then
    git push -u origin "$main_branch"
    echo "✅ 代码已推送到 GitHub"
    echo ""
    echo "📊 GitHub Actions 将自动开始构建"
    echo "🔍 查看构建状态: $(git remote get-url origin | sed 's/\.git$/\/actions/')"
else
    echo "⚠️  代码未推送。您可以稍后手动推送:"
    echo "   git push -u origin $main_branch"
fi

echo ""
echo "📝 后续步骤:"
echo "1. 访问 GitHub 仓库页面"
echo "2. 点击 'Actions' 标签查看构建状态"
echo "3. 构建完成后，从 Actions 页面下载 APK"
echo ""
echo "📚 更多信息请查看: GITHUB_ACTIONS_GUIDE.md"
echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
