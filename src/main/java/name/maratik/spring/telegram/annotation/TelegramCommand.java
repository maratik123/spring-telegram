package name.maratik.spring.telegram.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Telegram command handler.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TelegramCommand {
    /**
     * Alias for {@link #commands()}.
     */
    @AliasFor("commands")
    String[] value() default {};

    /**
     * List of commands, which will be processed by annotated method.
     */
    @AliasFor("value")
    String[] commands() default {};

    /**
     * Command description for /help method.
     * Supports <code>${..}</code> and <code>#{...}</code> processing as
     * in {@link org.springframework.beans.factory.annotation.Value} annotation.
     */
    String description() default "";

    /**
     * If {@code true} this command will not be listed in /help command.
     */
    boolean hidden() default false;

    /**
     * Marks that this method is implementation for /help command.
     */
    boolean isHelp() default false;
}
