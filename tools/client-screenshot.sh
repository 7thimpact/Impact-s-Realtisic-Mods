#!/bin/bash
# Starts a mod's development client on a virtual display, straight into the world saved as
# <mod>/forge/run/saves/ImpactsTest (copied from the GameTest world the first time), performs the given steps, then
# saves a screenshot and stops the client. A step is a chat command, "key:<xdotool key name>" for a key press, or
# "shot:<file.png>" for an extra screenshot along the way.
#
#   tools/client-screenshot.sh <mod folder> <output.png> [step ...]
#   tools/client-screenshot.sh impacts-inventory /tmp/hotbar.png "/gamemode survival" "/give @s iron_sword" key:f
set -u
MOD_FOLDER=$1
OUTPUT_FILE=$2
shift 2
REPOSITORY_ROOT=$(cd "$(dirname "$0")/.." && pwd)
RUN_DIRECTORY="$REPOSITORY_ROOT/$MOD_FOLDER/forge/run"
TEST_WORLD="$RUN_DIRECTORY/saves/ImpactsTest"
CLIENT_LOG=$(mktemp)
DISPLAY_NUMBER=:97
STARTUP_TIMEOUT_SECONDS=600

if [ ! -d "$TEST_WORLD" ]; then
  if [ ! -d "$RUN_DIRECTORY/world" ]; then
    echo "No test world yet: run ./gradlew :$MOD_FOLDER:forge:runGameTestServer first." >&2
    exit 1
  fi
  mkdir -p "$RUN_DIRECTORY/saves"
  cp -r "$RUN_DIRECTORY/world" "$TEST_WORLD"
fi
python3 -I "$REPOSITORY_ROOT/tools/enable_world_cheats.py" "$TEST_WORLD/level.dat" > /dev/null

Xvfb "$DISPLAY_NUMBER" -screen 0 1280x720x24 > /dev/null 2>&1 &
XVFB_PROCESS=$!
export DISPLAY=$DISPLAY_NUMBER
export LIBGL_ALWAYS_SOFTWARE=1
(cd "$REPOSITORY_ROOT" && ./gradlew ":$MOD_FOLDER:forge:runClientQuickPlay" --max-workers=2 > "$CLIENT_LOG" 2>&1) &
GRADLE_PROCESS=$!

stop_everything() {
  pkill -f "forgeclientuserdev" > /dev/null 2>&1
  kill "$GRADLE_PROCESS" > /dev/null 2>&1
  kill "$XVFB_PROCESS" > /dev/null 2>&1
}
trap stop_everything EXIT

elapsed_seconds=0
until grep -q "joined the game" "$CLIENT_LOG"; do
  if [ "$elapsed_seconds" -ge "$STARTUP_TIMEOUT_SECONDS" ] || ! kill -0 "$GRADLE_PROCESS" 2> /dev/null; then
    echo "The client did not reach the world. Log: $CLIENT_LOG" >&2
    exit 1
  fi
  sleep 5
  elapsed_seconds=$((elapsed_seconds + 5))
done
sleep 10

GAME_WINDOW=$(xdotool search --name "Minecraft" | head -1)
xdotool windowactivate --sync "$GAME_WINDOW" > /dev/null 2>&1
for step in "$@"; do
  case "$step" in
    key:*)
      xdotool key --window "$GAME_WINDOW" "${step#key:}"
      sleep 2
      ;;
    shot:*)
      import -window root "${step#shot:}"
      ;;
    *)
      xdotool key --window "$GAME_WINDOW" t
      sleep 1
      xdotool type --window "$GAME_WINDOW" --delay 40 "$step"
      xdotool key --window "$GAME_WINDOW" Return
      sleep 2
      ;;
  esac
done
sleep 3
import -window root "$OUTPUT_FILE"
echo "Saved $OUTPUT_FILE (client log: $CLIENT_LOG)"
