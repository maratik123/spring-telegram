package maratik.name.spring.telegram.annotation;

import maratik.name.spring.telegram.config.TelegramBotConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Telegram Bot annotations processing.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(TelegramBotConfiguration.class)
@Documented
@Inherited
public @interface EnableTelegramBot {
}
