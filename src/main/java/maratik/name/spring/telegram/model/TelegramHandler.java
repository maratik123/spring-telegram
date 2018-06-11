package maratik.name.spring.telegram.model;

import maratik.name.spring.telegram.annotation.TelegramCommand;

import java.lang.reflect.Method;
import java.util.Optional;

/**
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

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

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
