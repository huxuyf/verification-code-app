 @echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo   GitHub 部署脚本 (Windows)
echo ==========================================
echo.

REM 检查是否已初始化 git
if not exist ".git" (
    echo 初始化 Git 仓库...
    git init
    echo ✅ Git 仓库初始化完成
) else (
    echo ✅ Git 仓库已存在
)

REM 检查是否有远程仓库
git remote get-url origin >nul 2>&1
if %errorlevel% equ 0 (
    for /f "delims=" %%i in ('git remote get-url origin') do set remote_url=%%i
    echo ✅ 远程仓库已配置: !remote_url!
    echo.
    set /p update_remote="是否要更新远程仓库 URL? (y/n): "
    if /i "!update_remote!"=="y" (
        set /p repo_url="请输入 GitHub 仓库 URL: "
        git remote set-url origin "!repo_url!"
        echo ✅ 远程仓库已更新
    )
) else (
    echo 📝 配置远程仓库...
    set /p repo_url="请输入 GitHub 仓库 URL: "
    git remote add origin "!repo_url!"
    echo ✅ 远程仓库已配置
)

echo.
echo 📦 准备提交代码...

REM 添加所有文件
git add .

REM 检查是否有更改
git diff --cached --quiet >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️  没有需要提交的更改
    pause
    exit /b 0
)

REM 提交
echo 请输入提交信息（留空使用默认信息）:
set /p commit_message=

if "!commit_message!"=="" (
    set commit_message=feat: initial commit - Android verification code app
)

git commit -m "!commit_message!"
echo ✅ 代码已提交

echo.
echo 🚀 推送到 GitHub...

REM 检查主分支名称
git show-ref --verify --quiet refs/heads/main >nul 2>&1
if %errorlevel% equ 0 (
    set main_branch=main
) else (
    git show-ref --verify --quiet refs/heads/master >nul 2>&1
    if %errorlevel% equ 0 (
        set main_branch=master
    ) else (
        set main_branch=main
        git branch -M main
    )
)

REM 推送代码
set /p push_code="是否要推送到远程仓库? (y/n): "
if /i "!push_code!"=="y" (
    git push -u origin !main_branch!
    echo ✅ 代码已推送到 GitHub
    echo.
    echo 📊 GitHub Actions 将自动开始构建
    for /f "delims=" %%i in ('git remote get-url origin') do set remote_url=%%i
    set actions_url=!remote_url:.git=/actions!
    echo 🔍 查看构建状态: !actions_url!
) else (
    echo ⚠️  代码未推送。您可以稍后手动推送:
    echo    git push -u origin !main_branch!
)

echo.
echo 📝 后续步骤:
echo 1. 访问 GitHub 仓库页面
echo 2. 点击 'Actions' 标签查看构建状态
echo 3. 构建完成后，从 Actions 页面下载 APK
echo.
echo 📚 更多信息请查看: GITHUB Actions_GUIDE.md
echo.
echo ==========================================
echo   部署完成！
echo ==========================================

pause
