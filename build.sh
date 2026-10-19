#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

all=()
for f in props/*.properties; do
    all+=("$(basename "$f" .properties)")
done

targets=("${all[@]}")
if [ $# -gt 0 ]; then
    targets=("$@")
fi

module_of() {
    grep -oP '(?<=^loader_module=).*' "props/$1.properties"
}

for mc in "${all[@]}"; do
    echo "==> Generating data for Minecraft ${mc}"
    ./gradlew ":$(module_of "${mc}"):runData" -Pmc="${mc}" ${GRADLE_ARGS:-}
done

for mc in "${targets[@]}"; do
    echo "==> Building Minecraft ${mc}"
    ./gradlew build -Pmc="${mc}" ${GRADLE_ARGS:-}
done

echo
echo "==> Artifacts:"
find ./*/build/libs -name '*.jar' 2>/dev/null | sort || echo "  (none)"
