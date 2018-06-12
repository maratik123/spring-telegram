package name.maratik.spring.telegram.model;

import name.maratik.spring.telegram.annotation.TelegramCommand;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Telegram Bot command handler descriptor.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class TelegramHandler {
    private final Object bean;
    private final Method method;
    private final TelegramCommand telegramCommand;

    public TelegramHandler(Object bean, Method method, TelegramCommand telegramCommand) {
        this.bean = bean;
        this.method = method;
        this.telegramCommand = telegramCommand;
    }

    /**
     * Bean which contains handler.
     */
    public Object getBean() {
        return bean;
    }

    /**
     * Method which processes command
     */
    public Method getMethod() {
        return method;
    }

    /**
     * TelegramCommand annotation for this method.
     */
    public Optional<TelegramCommand> getTelegramCommand() {
        return Optional.ofNullable(telegramCommand);
    }

    @Override
    public String toString() {
        return "TelegramHandler{" +
            "bean=" + bean +
            ", method=" + method +
            ", telegramCommand=" + telegramCommand +
            '}';
    }
}
