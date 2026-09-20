#!/usr/bin/env bash
set -uo pipefail
cd "$(dirname "$0")"

targets=("$@")
if [ ${#targets[@]} -eq 0 ]; then
    for f in props/*.properties; do targets+=("$(basename "$f" .properties)"); done
fi

rc=0
for mc in "${targets[@]}"; do
    module=$(grep -oP '(?<=^loader_module=).*' "props/${mc}.properties")
    log="build/verify-${mc}.log"
    mkdir -p build
    echo "==> ${mc} (${module})"

    mkdir -p "${module}/run/${mc}"
    echo "eula=true" > "${module}/run/${mc}/eula.txt"

    timeout 600 ./gradlew ":${module}:runServer" -Pmc="${mc}" > "${log}" 2>&1 &
    pid=$!

    ok=1
    for _ in $(seq 120); do
        grep -qE 'Done \(|For help, type' "${log}" 2>/dev/null && { ok=0; break; }
        grep -qE 'Failed to start|Caused by:|FAILURE: Build' "${log}" 2>/dev/null && break
        kill -0 $pid 2>/dev/null || break
        sleep 5
    done

    kill $pid 2>/dev/null; wait $pid 2>/dev/null

    if [ $ok -eq 0 ]; then
        echo "    server reached Done  (${log})"
    else
        echo "    FAILED - see ${log}"
        rc=1
    fi
done
exit $rc
