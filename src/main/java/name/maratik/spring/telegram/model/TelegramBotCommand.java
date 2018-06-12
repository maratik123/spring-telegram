package name.maratik.spring.telegram.model;

/**
 * Telegram bot command descriptor.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class TelegramBotCommand {
    private final String command;
    private final String description;

    public TelegramBotCommand(String command, String description) {
        this.command = command;
        this.description = description;
    }

    /**
     * Command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Command description.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "TelegramBotCommand{" +
            "command='" + command + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}
