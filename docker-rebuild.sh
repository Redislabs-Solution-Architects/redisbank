#!/usr/bin/env bash



free_port() {
  local PORT="${1:-6379}"
  local AUTO="${2:-no}"

  echo "🔎 Checking port ${PORT}..."

  # Check if the port is currently listening
  if ! lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "✅ Port ${PORT} is available."
    return 0
  fi

  echo "⛔ Port ${PORT} is in use."
  echo "Process using port ${PORT}:"
  lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN | sed 's/^/  /'
  echo

  echo "🐳 Checking if any Docker container is publishing port ${PORT}..."
  mapfile -t CONTAINERS < <(docker ps --format '{{.ID}} {{.Names}} {{.Ports}}' \
    | awk -v p=":${PORT}->" '$0 ~ p {print $0}')

  if (( ${#CONTAINERS[@]} == 0 )); then
    echo "❌ No Docker container found publishing port ${PORT}."
    echo "👉 The port is used by a local process (see above)."
    return 1
  fi

  echo "✅ Docker container(s) detected:"
  printf '  %s\n' "${CONTAINERS[@]}"
  echo

  if [[ "${AUTO}" != "-y" ]]; then
    read -r -p "Stop these container(s)? [y/N] " ans
    [[ "${ans:-N}" =~ ^[Yy]$ ]] || { echo "ℹ️  Cancelled. Nothing stopped."; return 0; }
  fi

  for line in "${CONTAINERS[@]}"; do
    local cid
    cid="$(awk '{print $1}' <<<"$line")"
    echo "→ Stopping container ${cid}..."
    docker stop "$cid" >/dev/null
  done

  echo "✅ Attempted to free port ${PORT}."

  # Quick verification
  if lsof -nP -iTCP:"${PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "⚠️  Port ${PORT} is still in use. Another process may be listening."
    return 1
  else
    echo "🎉 Port ${PORT} is now free."
    return 0
  fi
}


free_port 6379 -y || exit 1

cd frontend
npm ci --fund=false
npm run build:prod
cd -



docker compose down -v --rmi local --remove-orphans
docker compose build
docker compose up


