# Spring Boot Simple State Machine

A simple, JSON-driven state machine for Spring Boot with support for success, error, and conditional (SpEL) transitions.

## Features

- Define states and transitions in a JSON file
- Three transition types: `SUCCESS`, `ERROR`, and `CONDITIONAL`
- Conditional transitions via Spring Expression Language (SpEL)
- Action-based authorization per state
- Spring Application Events for invalid state and illegal action errors
- Auto-configuration via `simple-state-machine.definition` property
- Multiple independent state machines per project via `SimpleStateMachineFactory`

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>de.nilskasseckert</groupId>
    <artifactId>spring-boot-simple-state-machine</artifactId>
    <version>0.0.1</version>
</dependency>
```

For GitHub Packages, add the repository:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/nilskasseckert/spring-boot-simple-state-machine</url>
    </repository>
</repositories>
```

## Quick Start

### 1. Create a State Machine Definition

Create a JSON file in your classpath, e.g. `src/main/resources/state-machine/order.json`:

```json
{
  "states": [
    { "state": "CREATED", "allowedActions": [] },
    { "state": "PROCESSING", "allowedActions": [] },
    { "state": "REVIEW", "allowedActions": ["APPROVE", "REJECT"] },
    { "state": "APPROVED", "allowedActions": ["SHIP"] },
    { "state": "SHIPPED", "allowedActions": [] },
    { "state": "COMPLETED", "allowedActions": [] },
    { "state": "ERROR_PROCESSING", "allowedActions": ["RETRY"] }
  ],
  "transitions": [
    { "type": "SUCCESS", "from": "CREATED", "to": "PROCESSING" },

    {
      "type": "CONDITIONAL",
      "from": "PROCESSING",
      "conditions": [
        { "when": "#order.totalAmount > 1000", "to": "REVIEW" },
        { "else": "APPROVED" }
      ]
    },
    { "type": "ERROR", "from": "PROCESSING", "to": "ERROR_PROCESSING" },

    { "type": "SUCCESS", "from": "REVIEW", "to": "APPROVED" },
    { "type": "SUCCESS", "from": "APPROVED", "to": "SHIPPED" },
    { "type": "SUCCESS", "from": "SHIPPED", "to": "COMPLETED" },
    { "type": "SUCCESS", "from": "ERROR_PROCESSING", "to": "CREATED" }
  ]
}
```

### 2. Configure the Property

In your `application.properties` or `application.yml`:

```properties
simple-state-machine.definition=state-machine/order.json
```

### 3. Use the Service

Inject `SimpleStateMachineService` into your components:

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final SimpleStateMachineService stateMachineService;

    public void processOrder(Order order) {
        // Transition on success
        String nextState = stateMachineService.nextStateForSuccess(order.getState());
        order.setState(nextState);
    }

    public void handleOrderFailure(Order order) {
        // Transition on error
        String nextState = stateMachineService.nextStateForError(order.getState());
        order.setState(nextState);
    }

    public void processWithConditions(Order order) {
        // Conditional transition with SpEL variables
        String nextState = stateMachineService.nextStateForSuccess(
                order.getState(),
                Map.of("order", order)
        );
        order.setState(nextState);
    }

    public void shipOrder(Order order) {
        // Validate that the action is allowed in the current state
        stateMachineService.requireActionAllowed(order.getState(), "SHIP");

        // Validate that the order is in the expected state
        stateMachineService.requireState(order.getState(), "APPROVED");

        String nextState = stateMachineService.nextStateForSuccess(order.getState());
        order.setState(nextState);
    }
}
```

## JSON Definition Format

### States

Each state defines its name and a list of allowed actions:

```json
{
  "state": "REVIEW",
  "allowedActions": ["APPROVE", "REJECT"]
}
```

### Transitions

#### SUCCESS

Transitions to the target state when the operation completed successfully:

```json
{ "type": "SUCCESS", "from": "CREATED", "to": "PROCESSING" }
```

#### ERROR

Transitions to the target state when the operation failed:

```json
{ "type": "ERROR", "from": "PROCESSING", "to": "ERROR_PROCESSING" }
```

#### CONDITIONAL

Evaluates SpEL conditions in order. The first matching condition determines the target state. An `else` clause serves as the default fallback:

```json
{
  "type": "CONDITIONAL",
  "from": "PROCESSING",
  "conditions": [
    { "when": "#order.totalAmount > 1000", "to": "REVIEW" },
    { "when": "#order.priority == 'HIGH'", "to": "REVIEW" },
    { "else": "APPROVED" }
  ]
}
```

Variables referenced in SpEL expressions (e.g. `#order`) must be passed via the `variables` map:

```java
stateMachineService.nextStateForSuccess(currentState, Map.of("order", myOrder));
```

You can also register enum constants as variables to use them in expressions:

```java
Map<String, Object> variables = new HashMap<>();
variables.put("order", myOrder);
for (Priority p : Priority.values()) {
    variables.put(p.name(), p);
}
stateMachineService.nextStateForSuccess(currentState, variables);
```

This allows expressions like `#order.priority == #HIGH`.

## API Reference

### SimpleStateMachineService

| Method | Description |
|---|---|
| `nextStateForSuccess(String currentState)` | Returns the next state for a successful transition |
| `nextStateForSuccess(String currentState, Map<String, Object> variables)` | Returns the next state for a successful transition with SpEL variables |
| `nextStateForError(String currentState)` | Returns the next state for an error transition |
| `nextStateForError(String currentState, Map<String, Object> variables)` | Returns the next state for an error transition with SpEL variables |
| `requireActionAllowed(String currentState, String action)` | Throws `StateMachineIllegalActionException` if action is not allowed |
| `isActionAllowedForState(String state, String action)` | Returns whether an action is allowed in the given state |
| `requireState(String currentState, String expectedState)` | Throws `StateMachineInvalidStateException` if states don't match |
| `requireOneStateOf(String currentState, String... expectedStates)` | Throws `StateMachineInvalidStateException` if current state is not in the list |
| `isInOneStateOf(String currentState, String... expectedStates)` | Returns whether the current state is in the expected list |

### Events

The service publishes Spring Application Events that you can listen to:

- `StateMachineInvalidStateEvent` - published when a state validation fails
- `StateMachineIllegalActionEvent` - published when an action is not allowed

```java
@Component
public class StateMachineEventListener {

    @EventListener
    public void onInvalidState(StateMachineInvalidStateEvent event) {
        log.warn("Invalid state: current={}, expected={}", event.currentState(), event.expectedStates());
    }

    @EventListener
    public void onIllegalAction(StateMachineIllegalActionEvent event) {
        log.warn("Illegal action: state={}, action={}", event.currentState(), event.action());
    }
}
```

### Exceptions

- `StateMachineInvalidStateException` - thrown when a state assertion fails or no valid transition is found
- `StateMachineIllegalActionException` - thrown when an action is not allowed in the current state

Both extend `StateMachineException` which extends `RuntimeException`.

## Multiple State Machines

If your project requires multiple independent state machines (e.g. one for orders and one for payments), use the `SimpleStateMachineFactory`.

### 1. Define Multiple State Machines

Create separate JSON files for each state machine:

- `src/main/resources/state-machine/order.json`
- `src/main/resources/state-machine/payment.json`

### 2. Configure the Properties

```properties
simple-state-machine.definitions.order=state-machine/order.json
simple-state-machine.definitions.payment=state-machine/payment.json
```

Each key after `definitions.` becomes the name used to retrieve the state machine.

### 3. Use the Factory

Inject `SimpleStateMachineFactory` and retrieve state machines by name:

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final SimpleStateMachineFactory stateMachineFactory;

    public void processOrder(Order order) {
        var orderStateMachine = stateMachineFactory.get("order");
        String nextState = orderStateMachine.nextStateForSuccess(order.getState());
        order.setState(nextState);
    }

    public void processPayment(Payment payment) {
        var paymentStateMachine = stateMachineFactory.get("payment");
        String nextState = paymentStateMachine.nextStateForSuccess(payment.getState());
        payment.setState(nextState);
    }
}
```

Each state machine is fully independent with its own states, transitions, and allowed actions.

> **Note:** You can use both modes together. Set `simple-state-machine.definition` for a single auto-configured `SimpleStateMachineService` bean, and `simple-state-machine.definitions.*` for additional state machines via the factory.

## Advanced: Custom StateMachineConfig Bean

If you need to load the definition from a custom source, provide your own `StateMachineConfig` bean:

```java
@Configuration
public class MyStateMachineConfig {

    @Bean
    StateMachineConfig stateMachineConfig(ObjectMapper objectMapper) throws Exception {
        var resource = new ClassPathResource("my-custom-path/states.json");
        try (InputStream stream = resource.getInputStream()) {
            return objectMapper.readValue(stream, StateMachineConfig.class);
        }
    }
}
```

The auto-configuration will back off when a custom `StateMachineConfig` bean is detected.

## Release

The project includes a GitHub Actions pipeline for automated releases. To create a new release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

This will:
1. Build and test the project
2. Publish the artifact to GitHub Packages
3. Create a GitHub Release with auto-generated release notes

## License

[MIT](LICENSE)
