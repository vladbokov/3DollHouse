# 3DollHouse

## Materials

Filament materials are written as sources in `app/src/main/materials/*.mat` and
compiled into packages in `app/src/main/assets/materials/*.filamat`, which the
app loads at runtime.

The compiled `.filamat` files are committed, so building the app does not need
any extra tools. Recompile materials only when you:

- change or add a `.mat` source;
- update the Filament version in `gradle/libs.versions.toml` — packages must be
  built with `matc` from the same Filament release as the Android library.

To recompile:

```sh
sh tools/build_materials.sh
```

The script uses `tools/filament/bin/matc` from Filament 1.80.1. The binaries in
`tools/filament/bin` (`matc`, `matinfo`) are **macOS arm64 only**. On other
platforms, download the matching Filament release from
https://github.com/google/filament/releases and point the script at its `matc`:

```sh
MATC=/path/to/filament/bin/matc sh tools/build_materials.sh
```

`matinfo` prints what a compiled package contains:

```sh
tools/filament/bin/matinfo app/src/main/assets/materials/color_roughness_lit.filamat
```
