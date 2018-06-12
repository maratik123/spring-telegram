# spring-telegram
Spring Controllers for Telegram API (with multithreaded support).

## Add to your project
### Maven
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

```xml
	<dependency>
	    <groupId>com.github.maratik123</groupId>
	    <artifactId>spring-telegram</artifactId>
	    <version>1.4</version>
	</dependency>
```
### Gradle
```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```groovy
	dependencies {
		implementation 'com.github.maratik123:spring-telegram:1.4'
	}
```
## Setup project
### Create TelegramBot
Create one Telegram Bot by the [link](https://telegram.me/botfather).

### Initialize Spring Boot Application
`[username]` and `[token]` should be replaced with real values, which you get at previous step.
Annotation `@EnableTelegramBot` can be used to enable telegram bot controllers support.
```java
@SpringBootApplication
@EnableTelegramBot
@Configuration
public class Application {
    @Bean
    public TelegramBotType telegramBotType() {
        return TelegramBotType.LONG_POLLING;
    }

    @Bean
    public TelegramBotBuilder telegramBotBuilder() {
        return new TelegramBotBuilder()
            .username("[username]")
            .token("[token]");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Write controller
```java
@TelegramBot
public class SimpleController {
    @TelegramMessage
    public SendMessage defaultAction(long userId, User user, String message) {
        return new SendMessage()
            .setChatId(userId)
            .setText(String.format("Hello %s, you've send me %s", user.getFirstName(), message));
    }

    @TelegramCommand(commands = "/some_command", description = "Some command")
    public SendMessage someCommand(long userId, TelegramMessageCommand telegramMessageCommand) {
        return new SendMessage()
            .setChatId(userId)
            .setText(String.format("You've send me %s command with arguments: %s",
                telegramMessageCommand.getCommand(), telegramMessageCommand.getArgument()
            ));
    }
}
```
That's all.

## Example
For full example see [link](https://github.com/maratik123/spring-telegram-example).

## License
spring-telegram is distributed under Apache-2.0 license.
