#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

targets=()
for f in props/*.properties; do
    name="$(basename "$f" .properties)"
    targets+=("$name")
done

if [ $# -gt 0 ]; then
    targets=("$@")
fi

for mc in "${targets[@]}"; do
    echo "==> Building Minecraft ${mc}"
    ./gradlew build -Pmc="${mc}" ${GRADLE_ARGS:-}
done

echo
echo "==> Artifacts:"
find ./*/build/libs -name '*.jar' 2>/dev/null | sort || echo "  (none)"
