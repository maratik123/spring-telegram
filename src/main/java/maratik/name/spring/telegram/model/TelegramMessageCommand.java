package maratik.name.spring.telegram.model;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;

import java.util.Optional;
import java.util.OptionalLong;

import static maratik.name.spring.telegram.util.Util.optionalOf;

/**
 * Telegram bot command splitted by command and arguments.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class TelegramMessageCommand {
    private final String command;
    private final String argument;
    private final boolean isCommand;
    private final Long forwardedFrom;

    public TelegramMessageCommand(Update update) {
        Message message = update.getMessage();
        String messageText = message.getText();
        if (isSlashStart(messageText)) {
            int spacePos = messageText.indexOf(' ');
            if (spacePos != -1) {
                command = messageText.substring(0, spacePos);
                argument = messageText.substring(spacePos + 1);
            } else {
                command = messageText;
                argument = null;
            }
            isCommand = true;
        } else {
            command = null;
            argument = messageText;
            isCommand = false;
        }
        this.forwardedFrom = Optional.ofNullable(message.getForwardFrom())
            .map(User::getId)
            .map(Integer::longValue)
            .orElse(null);
    }

    /**
     * Current command received from user.
     */
    public Optional<String> getCommand() {
        return Optional.ofNullable(command);
    }

    /**
     * Command arguments.
     */
    public Optional<String> getArgument() {
        return Optional.ofNullable(argument);
    }

    /**
     * {@code true} if current message is command.
     */
    public boolean isCommand() {
        return isCommand;
    }

    /**
     * User ID, from whom this forward is originated.
     */
    public OptionalLong getForwardedFrom() {
        return optionalOf(forwardedFrom);
    }

    @Override
    public String toString() {
        return "TelegramMessageCommand{" +
            "command='" + command + '\'' +
            ", argument='" + argument + '\'' +
            ", isCommand=" + isCommand +
            ", forwardedFrom=" + forwardedFrom +
            '}';
    }

    private static boolean isSlashStart(String message) {
        return message != null && message.startsWith("/");
    }
}
