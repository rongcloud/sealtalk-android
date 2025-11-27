#!/bin/bash

# SealTalk 多发布渠道一键构建脚本
# 输出内容：
#   - publishstore：全 ABI Release APK
#   - publishMEIZU：64 位 Release APK
#   - publishgoogle：全 ABI Release App Bundle
# 构建产物路径：build/publish-outputs

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLEW="${SCRIPT_DIR}/../gradlew"
STAMP="$(date +%Y%m%d%H)"
PUBLISH_OUTPUT_DIR="${SCRIPT_DIR}/build/publish-outputs"
declare -a COPIED_FILES=()

run_gradle() {
  echo "  -> gradlew $*"
  "${GRADLEW}" "$@"
}

print_artifacts() {
  local label="$1"
  local dir="$2"
  local pattern="$3"

  echo "输出目录: ${dir}"
  if [ -d "${dir}" ]; then
    local result
    result=$(find "${dir}" -maxdepth 1 -type f -name "${pattern}" -print)
    if [ -n "${result}" ]; then
      echo "${label}:"
      # shellcheck disable=SC2001
      echo "${result}" | sed 's/^/  - /'
    else
      echo "  ${label} 未找到"
    fi
  else
    echo "  目录不存在"
  fi
  echo
}

copy_release_artifacts() {
  local label="$1"
  local dir="$2"
  local pattern="$3"

  if [ ! -d "${dir}" ]; then
    echo "  ${label} 目录不存在: ${dir}"
    return
  fi

  local files=()
  while IFS= read -r file; do
    files+=("$file")
  done < <(find "${dir}" -maxdepth 1 -type f -name "${pattern}" | sort)

  if [ ${#files[@]} -eq 0 ]; then
    echo "  ${label} 未找到"
    return
  fi

  mkdir -p "${PUBLISH_OUTPUT_DIR}"

  for file in "${files[@]}"; do
    local filename
    filename="$(basename "${file}")"
    local renamed
    renamed="$(python3 - "$filename" "$STAMP" <<'PY'
import re, sys
name = sys.argv[1]
stamp = sys.argv[2]
name = re.sub('sealtalk', 'sealchat', name, flags=re.IGNORECASE)
match = re.search('release', name, flags=re.IGNORECASE)
if match:
    idx = match.end()
    name = name[:idx] + '-' + stamp + name[idx:]
else:
    dot = name.rfind('.')
    if dot != -1:
        name = name[:dot] + '-' + stamp + name[dot:]
    else:
        name = name + '-' + stamp
print(name)
PY
)"
    local target="${PUBLISH_OUTPUT_DIR}/${renamed}"
    cp "${file}" "${target}"
    COPIED_FILES+=("${renamed}")
    echo "  已复制 ${label}: ${renamed}"
  done
}

echo "=============================="
echo " SealTalk 发布渠道一键构建"
echo "=============================="
echo

echo "执行清理..."
run_gradle clean
echo

echo ">>> 构建 publishstore Release APK（全 ABI）"
run_gradle assemblePublishstoreRelease
echo
print_artifacts "Release APK" "${SCRIPT_DIR}/build/outputs/apk/publishstore/release" "*.apk"

echo ">>> 构建 publishMEIZU Release APK（仅 64 位 ABI）"
run_gradle assemblePublishMEIZURelease
echo
print_artifacts "Release APK" "${SCRIPT_DIR}/build/outputs/apk/publishMEIZU/release" "*.apk"

echo ">>> 构建 publishgoogle Release App Bundle（全 ABI）"
run_gradle bundlePublishgoogleRelease
echo
print_artifacts "Release AAB" "${SCRIPT_DIR}/build/outputs/bundle/publishgoogleRelease" "*.aab"

echo ">>> 复制发布产物到 publish-outputs"
copy_release_artifacts "publishstore Release APK" "${SCRIPT_DIR}/build/outputs/apk/publishstore/release" "*.apk"
copy_release_artifacts "publishMEIZU Release APK" "${SCRIPT_DIR}/build/outputs/apk/publishMEIZU/release" "*.apk"
copy_release_artifacts "publishgoogle Release AAB" "${SCRIPT_DIR}/build/outputs/bundle/publishgoogleRelease" "*.aab"

if [ ${#COPIED_FILES[@]} -gt 0 ]; then
  echo
  echo "publish-outputs 目录内容:"
  ls -la "${PUBLISH_OUTPUT_DIR}"
else
  echo "未复制任何发布产物，请检查上述输出。"
fi

echo "全部构建完成！"

