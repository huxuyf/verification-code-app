#!/bin/bash

# APK 自动签名脚本
# 用途: 为未签名的 Release APK 添加数字签名

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
KEYSTORE_FILE="release.keystore"
KEY_ALIAS="verification-coder-app"
UNSIGNED_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
SIGNED_APK="app/build/outputs/apk/release/app-release-signed.apk"

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 未安装，请先安装"
        exit 1
    fi
}

# 检查文件是否存在
check_file() {
    if [ ! -f "$1" ]; then
        print_error "文件不存在: $1"
        exit 1
    fi
}

# 主函数
main() {
    print_info "========================================="
    print_info "  APK 自动签名工具"
    print_info "========================================="
    echo ""

    # 检查必要工具
    print_info "检查必要工具..."
    check_command "keytool"
    check_command "apksigner"

    # 检查未签名的 APK
    print_info "检查未签名的 APK..."
    check_file "$UNSIGNED_APK"

    # 检查密钥库是否存在
    if [ ! -f "$KEYSTORE_FILE" ]; then
        print_warning "密钥库不存在，将创建新的密钥库..."
        create_keystore
    else
        print_info "使用现有密钥库: $KEYSTORE_FILE"
    fi

    # 读取密码
    echo ""
    read -sp "请输入密钥库密码: " STORE_PASSWORD
    echo ""
    read -sp "请输入密钥密码: " KEY_PASSWORD
    echo ""

    # 签名 APK
    print_info "正在签名 APK..."
    apksigner sign \
        --ks "$KEYSTORE_FILE" \
        --ks-key-alias "$KEY_ALIAS" \
        --ks-pass pass:"$STORE_PASSWORD" \
        --key-pass pass:"$KEY_PASSWORD" \
        --out "$SIGNED_APK" \
        "$UNSIGNED_APK"

    if [ $? -eq 0 ]; then
        print_info "✅ APK 签名成功！"
        print_info "签名后的 APK: $SIGNED_APK"

        # 显示文件信息
        echo ""
        print_info "文件信息:"
        ls -lh "$SIGNED_APK"

        # 验证签名
        echo ""
        print_info "验证签名..."
        apksigner verify --print-certs "$SIGNED_APK"

        echo ""
        print_info "========================================="
        print_info "  签名完成！"
        print_info "========================================="
        print_info "现在可以安装: $SIGNED_APK"
    else
        print_error "❌ APK 签名失败"
        exit 1
    fi
}

# 创建密钥库
create_keystore() {
    print_info "创建新的密钥库..."

    read -p "请输入密钥库别名 [默认: $KEY_ALIAS]: " INPUT_ALIAS
    KEY_ALIAS=${INPUT_ALIAS:-$KEY_ALIAS}

    read -p "请输入组织单位 (O) [默认: VerificationCoder]: " INPUT_O
    ORG=${INPUT_O:-VerificationCoder}

    read -p "请输入组织名称 (OU) [默认: Dev]: " INPUT_OU
    ORG_UNIT=${INPUT_OU:-Dev}

    read -p "请输入城市/地区 (L) [默认: Beijing]: " INPUT_L
    LOCATION=${INPUT_L:-Beijing}

    read -p "请输入州/省份 (ST) [默认: Beijing]: " INPUT_ST
    STATE=${INPUT_ST:-Beijing}

    read -p "请输入国家代码 (C) [默认: CN]: " INPUT_C
    COUNTRY=${INPUT_C:-CN}

    read -sp "请输入密钥库密码: " STORE_PASSWORD
    echo ""

    read -sp "请输入密钥密码: " KEY_PASSWORD
    echo ""

    keytool -genkey -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$STORE_PASSWORD" \
        -keypass "$KEY_PASSWORD" \
        -dname "CN=$KEY_ALIAS, OU=$ORG_UNIT, O=$ORG, L=$LOCATION, ST=$STATE, C=$COUNTRY"

    if [ $? -eq 0 ]; then
        print_info "✅ 密钥库创建成功！"
        print_warning "请妥善保管密钥库文件和密码！"
        print_warning "建议将 $KEYSTORE_FILE 添加到 .gitignore"
    else
        print_error "❌ 密钥库创建失败"
        exit 1
    fi
}

# 运行主函数
main
