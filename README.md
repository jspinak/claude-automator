# Claude Automator - Brobot Example Application

This is a modern Brobot application that demonstrates GUI automation using the latest Brobot patterns and APIs.

## Overview

The Claude Automator monitors and interacts with a Claude AI interface. It showcases:
- State-based GUI automation
- Modern fluent API with action chaining
- JavaStateTransition for code-based transitions
- Spring Boot integration
- Automatic state monitoring

## Project Structure

```
claude-automator/
├── build.gradle              # Gradle build configuration
├── settings.gradle           # Includes local Brobot library
├── src/main/
│   ├── java/com/claude/automator/
│   │   ├── ClaudeAutomatorApplication.java    # Spring Boot main class
│   │   ├── states/
│   │   │   ├── PromptState.java              # Claude prompt state
│   │   │   └── WorkingState.java             # Working state with icons
│   │   ├── transitions/
│   │   │   ├── PromptTransitions.java        # Transitions for Prompt state
│   │   │   └── WorkingTransitions.java       # Transitions for Working state
│   │   ├── automation/
│   │   │   └── ClaudeMonitoringAutomation.java # Main automation logic
│   │   └── config/
│   │       └── StateConfiguration.java        # Spring configuration
│   └── resources/
│       ├── application.properties             # Spring Boot config
│       └── images/
│           ├── prompt/
│           │   └── claude-prompt.png         # Prompt image
│           └── working/
│               ├── claude-icon-1.png         # Claude icon variants
│               ├── claude-icon-2.png
│               ├── claude-icon-3.png
│               └── claude-icon-4.png

## Key Features

### 1. Modern Brobot Patterns

- **StateEnum**: Type-safe state references
- **StateImage.Builder**: Fluent image configuration
- **StateString**: Text input with state context
- **StateTransitions.Builder**: Clean transition setup

### 2. Fluent Action Chaining

The application demonstrates the modern fluent API for chaining actions:

```java
PatternFindOptions findClickType = new PatternFindOptions.Builder()
    .setPauseAfterEnd(0.5) // Pause before clicking
    .then(new ClickOptions.Builder()
            .setPauseAfterEnd(0.5) // Pause before typing
            .build())
    .then(new TypeOptions.Builder()
            .build())
    .build();
```

### 3. JavaStateTransition

Uses code-based transitions for complex logic:

```java
new JavaStateTransition.Builder()
    .setFunction(() -> executePromptToWorking())
    .addToActivate(WorkingState.Name.WORKING.toString())
    .setStaysVisibleAfterTransition(true)
    .build();
```

### 4. Spring Boot Integration

- Component scanning for both app and Brobot packages
- Dependency injection throughout
- Configuration management
- Lifecycle management with @PostConstruct

## Automation Flow

1. **Initial State**: Working state is set as active
2. **Monitoring**: Continuously checks for Claude icon presence
3. **Icon Disappears**: Removes Working state and reopens it
4. **Prompt Appears**: Clicks prompt, types "continue", presses Enter
5. **Cycle Continues**: Returns to monitoring

## Running the Application

```bash
./gradlew bootRun
```

## Requirements

- Java 21
- Gradle 8.x
- Brobot library (included via composite build)
- Screenshot images in the images folder

## Code Highlights

### State Definition with Builder Pattern
```java
state = new State.Builder(Name.WORKING)
        .withImages(claudeIcon)
        .build();
```

### Combined Text Input
```java
continueCommand = new StateString.Builder()
    .setOwnerStateName(Name.PROMPT.toString())
    .setName("ContinueCommand")
    .build("continue\n");  // \n represents ENTER key
```

### Modern Find Options
```java
PatternFindOptions findOptions = new PatternFindOptions.Builder()
    .setPauseBeforeBegin(10.0)  // 10 seconds timeout
    .build();
```

## Notes

- Uses the latest Brobot ActionConfig classes instead of deprecated ActionOptions
- Demonstrates proper Lombok configuration with Brobot
- Shows composite build setup for local library development
- Images are automatically copied to build resources

This example serves as a template for creating modern Brobot applications with clean architecture and the latest API patterns.