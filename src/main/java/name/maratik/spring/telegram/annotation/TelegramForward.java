package name.maratik.spring.telegram.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Telegram forward message handler.
 *
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TelegramForward {
    /**
     * Alias for {@link #from()}.
     */
    @AliasFor("from")
    String[] value() default {};

    /**
     * Accepts forwards originated only from specified user ids.
     *
     * @return array of comma-delimited user ids
     */
    @AliasFor("value")
    String[] from() default {};
}
