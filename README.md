# NvgRenderer

NanoVG wrapper for Fabric 1.21.10.

## Features

1. Render callback that runs after the vanilla GUI finishes
2. A few basic components (`NvgText`, `NvgTooltip`, `NvgTextInputHandler`)
3. Image loading from classpath, file paths, or URLs (PNG/SVG)
4. Keyboard and mouse events

## Requirements

1. Minecraft `1.21.10`
2. Fabric Loader `>= 0.18.4`
3. Fabric Kotlin `>= 1.13.9+kotlin.2.3.10`
4. Java 21

## Install With JitPack

1. Add JitPack to your Gradle repositories.
2. Add the dependency and sync.

### Gradle Kotlin DSL (`build.gradle.kts`)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    modImplementation("com.github.noamm9:NvgRenderer:1.0")
```

### Gradle Groovy (`build.gradle`)

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    modImplementation "com.github.noamm9:NvgRenderer:1.0"
}
```

## Basic Usage

Register a render callback, then call `NVG` draw functions inside it. The library already handles `beginFrame` and
`endFrame` for you.

```kotlin
import com.github.noamm9.nvgrenderer.batchers.NVGBatcher
import com.github.noamm9.nvgrenderer.nvg.NVG
import com.github.noamm9.nvgrenderer.nvg.ui.NvgText
import net.fabricmc.api.ClientModInitializer
import java.awt.Color

class ExampleClient: ClientModInitializer {
    override fun onInitializeClient() {
        NVGBatcher.addCallback {
            NVG.rect(20, 20, 140, 40, Color(0, 0, 0, 140), 6f)
            NvgText.draw("Hello NanoVG", 28f, 30f, color = Color.WHITE, size = 16f)
            NVG.line(20, 70, 200, 70, 2f, Color(255, 255, 255, 180))
        }
    }
}
```

## Input Batching

The mixins forward mouse and keyboard events into the batchers. Return `true` to consume the event.

```kotlin
import com.github.noamm9.nvgrenderer.batchers.KeyboardBatcher
import com.github.noamm9.nvgrenderer.batchers.MouseBatcher

KeyboardBatcher.addCallbackKey { event ->
    if (event.state == KeyboardBatcher.KeyState.PRESS && event.button == 79) {
        // example: O key
        return@addCallbackKey true
    }
    false
}

MouseBatcher.addCallbackClick { event ->
    // return true to cancel the click
    false
}
```

## Images

You can load images from a classpath resource, a file path, or a URL. SVGs are supported.

```kotlin
import com.github.noamm9.nvgrenderer.nvg.NVG

val img1 = NVG.createImage("/assets/yourmodid/images/image.png")
val img2 = NVG.createImage("https://bigrat.monster/")
NVG.image(img1, 10f, 90f, 32f, 32f, 6f)

// when done with the image
NVG.deleteImage(img1)
```

## Manual PIP Rendering

If you want to draw in a specific place/area, you can use `PIPNVG.draw`.

```kotlin
import com.github.noamm9.nvgrenderer.nvg.PIPNVG

PIPNVG.draw(context, x, y, width, height) {
    // NVG drawing calls here
}
```

## License

Unlicense. Third-party BSD-3-Clause notices are included. See `LICENSE.txt`.

## Credits

odtheking - [Odin (Fabric)](https://github.com/odtheking/Odin): Portions of the NanoVG rendering/input logic are derived
from this project (BSD-3-Clause).
