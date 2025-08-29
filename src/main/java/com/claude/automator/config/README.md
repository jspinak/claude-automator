# Claude Automator Configuration

## Configuration Now Handled by Brobot Framework

As of Brobot v1.2.0, initial state configuration is fully handled by the framework. The `@State(initial = true)` annotation on PromptState is all that's needed.

## Previous Configuration Classes (Removed)

The following configuration classes have been removed as they are no longer needed:
- `InitialStateConfiguration` - Now handled by `InitialStateAutoConfiguration` in Brobot
- `InitialStateInterceptor` - Framework ensures correct initial state activation
- `MockInitialStateActivator` - Framework's `ApplicationReadyEvent` listener handles this

## Current Configuration

The application now uses framework-provided configuration via `application.properties`:

```properties
# Initial state configuration (from application.properties)
brobot.startup.auto-activate=true
brobot.startup.initial-delay=5  # Was claude.automator.monitoring.initial-delay
brobot.startup.verify=true
brobot.startup.activate-first-only=true
```

The PromptState is automatically detected and activated because it's marked with:
```java
@State(initial = true)
```

No additional configuration classes are needed!