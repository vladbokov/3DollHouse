#!/bin/sh
# Compiles Filament materials: app/src/main/materials/*.mat -> assets/materials/*.filamat
#
# The app can't load .mat sources, only compiled .filamat packages (committed).
# Rerun after editing a .mat or updating Filament: matc must be the same Filament
# version as the Android dependency (1.80.1), or materials fail to load.
#
# tools/filament/bin/matc is macOS arm64 only. Elsewhere, take matc from the
# Filament release archive and run: MATC=/path/to/matc sh tools/build_materials.sh
set -eu

cd "$(dirname "$0")/.."

MATC="${MATC:-tools/filament/bin/matc}"
SRC=app/src/main/materials
OUT=app/src/main/assets/materials

mkdir -p "$OUT"

for material in "$SRC"/*.mat; do
    name=$(basename "$material" .mat)

    # -p: target mobile GPUs. -a: graphics APIs to include shaders for.
    "$MATC" \
        -p mobile \
        -a opengl \
        -a vulkan \
        -o "$OUT/$name.filamat" \
        "$material"
done
