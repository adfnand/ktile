# ktile
A desktop tiling window manager for Linux and macOS. Press the global hotkey to show the layout and pick a tile combination to arrange the currently focused window accordingly.

<img width="2400" height="1350" alt="Screenshot From 2026-08-16 22-57-52" src="https://github.com/user-attachments/assets/178af25c-2913-4c7e-bd84-6a58cfc517bc" />

## Requirements

- Linux (X11 or Wayland) or macOS
- Java 17+
- Rust toolchain (only needed for Linux builds; the hotkey library is written in Rust)
- On Linux, your user must be in the `input` and `uinput` groups for the global hotkey to work reliably on Wayland

## Setup

### Linux

1. Add your user to the `input` and `uinput` groups:

   ```bash
   sudo usermod -aG input,uinput "$USER"
   ```

   Then log out and back in (or run `newgrp input` and `newgrp uinput` to apply the change in the current shell).

2. Verify permissions:

   ```bash
   ls -l /dev/input/event* | head -1
   ls -l /dev/uinput
   ```

   `/dev/input/event*` should be readable by the `input` group and `/dev/uinput` should be writable by the `uinput` group.

3. Build and run:

   ```bash
   ./gradlew run
   ```

   KTile starts hidden in the system tray. Press **Super+K** to toggle the preview window.

### macOS

1. Build and run:

   ```bash
   ./gradlew run
   ```

   KTile uses JNativeHook for global hotkeys. The first run may prompt for accessibility permissions.

## Global hotkey

- Default: **Super+K**
- On Linux, the hotkey is registered via the `kbd-global` Rust crate reading from `/dev/input/event*`; this works on both X11 and Wayland.
- On macOS, the hotkey is registered with JNativeHook.

### Wayland note

On Wayland, `Super+K` can conflict with the compositor's own Super binding (e.g., opening the Activities overview). For the most reliable experience, bind `Super+K` directly in your compositor's keyboard shortcuts to run:

```bash
ktile --toggle
```

KTile uses a single-instance Unix socket, so this command will toggle the running instance instead of starting a second one.

If you prefer the built-in `evdev` listener, the app must be able to open `/dev/input/event*` and create a virtual device via `/dev/uinput` for key forwarding. Both are usually granted by the `input` and `uinput` groups.

## Tests

```bash
./gradlew check
```

This runs Kotlin tests, ktlint, detekt, and the Rust test suite for the hotkey library.

## Building a distribution

```bash
./gradlew package
```

Produces `.deb`, `.rpm`, or `.dmg` depending on the host OS.
