@echo off
REM APK 自动签名脚本 (Windows 版本)
REM 用途: 为未签名的 Release APK 添加数字签名

setlocal enabledelayedexpansion

REM 配置
set KEYSTORE_FILE=release.keystore
set KEY_ALIAS=verification-coder-app
set UNSIGNED_APK=app\app-release-unsigned.apk
set SIGNED_APK=app\app-release-signed.apk

echo =========================================
echo   APK 自动签名工具 (Windows)
echo =========================================
echo.

REM 检查必要工具
echo [INFO] 检查必要工具...
where keytool >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] keytool 未安装，请先安装 Android SDK
    pause
    exit /b 1
)

where apksigner >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] apksigner 未安装，请先安装 Android SDK
    echo [INFO] apksigner 通常位于: %ANDROID_HOME%\build-tools\{version}\apksigner.bat
    pause
    exit /b 1
)

REM 检查未签名的 APK
echo [INFO] 检查未签名的 APK...
if not exist "%UNSIGNED_APK%" (
    echo [ERROR] 文件不存在: %UNSIGNED_APK%
    pause
    exit /b 1
)

REM 检查密钥库是否存在
if not exist "%KEYSTORE_FILE%" (
    echo [WARNING] 密钥库不存在，将创建新的密钥库...
    call :create_keystore
) else (
    echo [INFO] 使用现有密钥库: %KEYSTORE_FILE%
)

REM 读取密码
echo.
set /p STORE_PASSWORD="请输入密钥库密码: "
set /p KEY_PASSWORD="请输入密钥密码: "

REM 签名 APK
echo [INFO] 正在签名 APK...
apksigner sign ^
    --ks "%KEYSTORE_FILE%" ^
    --ks-key-alias "%KEY_ALIAS%" ^
    --ks-pass pass:%STORE_PASSWORD% ^
    --key-pass pass:%KEY_PASSWORD% ^
    --out "%SIGNED_APK%" ^
    "%UNSIGNED_APK%"

if %ERRORLEVEL% EQU 0 (
    echo [INFO] APK 签名成功！
    echo [INFO] 签名后的 APK: %SIGNED_APK%

    REM 显示文件信息
    echo.
    echo [INFO] 文件信息:
    dir "%SIGNED_APK%"

    REM 验证签名
    echo.
    echo [INFO] 验证签名...
    apksigner verify --print-certs "%SIGNED_APK%"

    echo.
    echo =========================================
    echo   签名完成！
    echo =========================================
    echo [INFO] 现在可以安装: %SIGNED_APK%
) else (
    echo [ERROR] APK 签名失败
    pause
    exit /b 1
)

pause
exit /b 0

REM 创建密钥库函数
:create_keystore
echo [INFO] 创建新的密钥库...

set /p INPUT_ALIAS="请输入密钥库别名 [默认: %KEY_ALIAS%]: "
if not "%INPUT_ALIAS%"=="" set KEY_ALIAS=%INPUT_ALIAS%

set /p INPUT_O="请输入组织单位 (O) [默认: VerificationCoder]: "
if not "%INPUT_O%"=="" set ORG=%INPUT_O%
if "%INPUT_O%"=="" set ORG=VerificationCoder

set /p INPUT_OU="请输入组织名称 (OU) [默认: Dev]: "
if not "%INPUT_OU%"=="" set ORG_UNIT=%INPUT_OU%
if "%INPUT_OU%"=="" set ORG_UNIT=Dev

set /p INPUT_L="请输入城市/地区 (L) [默认: Beijing]: "
if not "%INPUT_L%"=="" set LOCATION=%INPUT_L%
if "%INPUT_L%"=="" set LOCATION=Beijing

set /p INPUT_ST="请输入州/省份 (ST) [默认: Beijing]: "
if not "%INPUT_ST%"=="" set STATE=%INPUT_ST%
if "%INPUT_ST%"=="" set STATE=Beijing

set /p INPUT_C="请输入国家代码 (C) [默认: CN]: "
if not "%INPUT_C%"=="" set COUNTRY=%INPUT_C%
if "%INPUT_C%"=="" set COUNTRY=CN

set /p STORE_PASSWORD="请输入密钥库密码: "
set /p KEY_PASSWORD="请输入密钥密码: "

keytool -genkey -v ^
    -keystore "%KEYSTORE_FILE%" ^
    -alias "%KEY_ALIAS%" ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity 10000 ^
    -storepass "%STORE_PASSWORD%" ^
    -keypass "%KEY_PASSWORD%" ^
    -dname "CN=%KEY_ALIAS%, OU=%ORG_UNIT%, O=%ORG%, L=%LOCATION%, ST=%STATE%, C=%COUNTRY%"

if %ERRORLEVEL% EQU 0 (
    echo [INFO] 密钥库创建成功！
    echo [WARNING] 请妥善保管密钥库文件和密码！
    echo [WARNING] 建议将 %KEYSTORE_FILE% 添加到 .gitignore
) else (
    echo [ERROR] 密钥库创建失败
    pause
    exit /b 1
)
goto :EOF
