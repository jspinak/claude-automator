# Tauri-Based Highlighting for Brobot

## Overview
Replace JavaFX/Swing highlighting with a lightweight Tauri service that provides native, cross-platform highlighting without Java UI dependencies.

## Architecture

### 1. Tauri Highlight Service (Standalone App)
```
tauri-highlight/
├── src-tauri/          # Rust backend
│   ├── main.rs         # WebSocket server, window management
│   └── Cargo.toml
├── src/                # Frontend
│   ├── index.html      # Transparent overlay window
│   ├── highlight.js    # Highlighting logic
│   └── styles.css      # Visual effects
└── tauri.conf.json     # Tauri configuration
```

### 2. Java Integration (Brobot Side)

```java
public class TauriHighlightService implements HighlightService {
    private WebSocketClient wsClient;
    private Process tauriProcess;
    
    public void highlight(Region region, Color color, int duration) {
        // Send highlight command via WebSocket
        wsClient.send(Json.toJson(new HighlightCommand(
            region.x, region.y, region.width, region.height,
            color, duration
        )));
    }
    
    @PostConstruct
    public void init() {
        // Start Tauri app if not running
        if (!isTauriRunning()) {
            tauriProcess = Runtime.getRuntime().exec("tauri-highlight.exe");
        }
        // Connect via WebSocket
        wsClient = new WebSocketClient("ws://localhost:7890");
    }
}
```

## Benefits for Users

### Current JavaFX Approach:
```gradle
// Users must add these dependencies
implementation 'org.openjfx:javafx-base:21'
implementation 'org.openjfx:javafx-graphics:21'
implementation 'org.openjfx:javafx-controls:21'
// Plus platform-specific versions...
```

### With Tauri:
```gradle
// Just use Brobot - highlighting works automatically!
implementation 'io.github.jspinak:brobot'
```

## Implementation Plan

### Phase 1: Tauri Highlight Service
1. Create Tauri app with transparent, borderless windows
2. WebSocket server for IPC with Java
3. Support multiple simultaneous highlights
4. Smooth animations and effects

### Phase 2: Java Integration
1. Add WebSocket client to Brobot
2. Implement TauriHighlightService
3. Auto-download Tauri binary on first use
4. Fallback to Swing if Tauri unavailable

### Phase 3: Enhanced Features
1. Rich animations (pulse, fade, slide)
2. Multiple highlight styles
3. Text overlays
4. Screenshot integration

## Technical Implementation

### Tauri Window Configuration (tauri.conf.json):
```json
{
  "tauri": {
    "windows": [
      {
        "transparent": true,
        "decorations": false,
        "alwaysOnTop": true,
        "skipTaskbar": true,
        "resizable": false,
        "acceptFirstMouse": false
      }
    ]
  }
}
```

### Frontend (highlight.js):
```javascript
// WebSocket connection to Java
const ws = new WebSocket('ws://localhost:7890');

ws.onmessage = (event) => {
  const cmd = JSON.parse(event.data);
  
  if (cmd.type === 'highlight') {
    createHighlight(cmd.x, cmd.y, cmd.width, cmd.height, cmd.color);
  }
};

function createHighlight(x, y, width, height, color) {
  const highlight = document.createElement('div');
  highlight.className = 'highlight';
  highlight.style.cssText = `
    position: fixed;
    left: ${x}px;
    top: ${y}px;
    width: ${width}px;
    height: ${height}px;
    border: 3px solid ${color};
    pointer-events: none;
    animation: pulse 1s infinite;
  `;
  document.body.appendChild(highlight);
}
```

### Rust Backend (main.rs):
```rust
use tauri::{Manager, Window};
use tungstenite::server;

#[tauri::command]
fn position_window(window: Window, x: i32, y: i32, width: i32, height: i32) {
    window.set_position(tauri::Position::Physical(x, y)).ok();
    window.set_size(tauri::Size::Physical(width, height)).ok();
}

fn main() {
    // Start WebSocket server for Java communication
    std::thread::spawn(|| {
        server::start("127.0.0.1:7890");
    });
    
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![position_window])
        .run(tauri::generate_context!())
        .expect("error running tauri app");
}
```

## Distribution Options

### Option 1: Embedded Binary
- Include platform-specific Tauri binaries in Brobot JAR
- Extract and run on first use
- ~5MB per platform

### Option 2: Separate Download
- Download on first use
- Cache in user directory
- Smaller JAR size

### Option 3: Optional Module
- Separate `brobot-highlight-tauri` package
- Users opt-in with additional dependency

## Advantages Over JavaFX

1. **No Java UI Dependencies** - Works with any JDK version
2. **Better Performance** - Native rendering, no JVM overhead
3. **Richer Effects** - Full CSS3 animations, gradients, shadows
4. **Easier Deployment** - Single binary, no classpath issues
5. **Future-Proof** - Web technologies, active development

## Migration Path

1. Keep existing JavaFX/Swing as fallback
2. Add Tauri as preferred option when available
3. Gradually deprecate JavaFX support
4. Eventually make Tauri the default

## Example Usage (Unchanged for Users)

```java
// Users' code doesn't change!
Action action = new Action();
action.highlight(region, Color.RED, 2.0);
```

The highlighting "just works" without any additional setup!