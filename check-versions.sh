#!/bin/bash
echo "=========================================="
echo "  GitHub Actions 版本检查"
echo "=========================================="
echo ""
echo "build-android.yml:"
grep "uses:" .github/workflows/build-android.yml | grep -v "^#" | sed 's/^[[:space:]]*/  /'
echo ""
echo "build-release.yml:"
grep "uses:" .github/workflows/build-release.yml | grep -v "^#" | sed 's/^[[:space:]]*/  /'
echo ""
echo "=========================================="
echo "  检查完成"
echo "=========================================="
