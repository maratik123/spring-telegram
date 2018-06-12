package name.maratik.spring.telegram.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that annotated class is Telegram Bot Controller.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@Inherited
public @interface TelegramBot {
    /**
     * Alias for {@link #userId()}.
     */
    @AliasFor("userId")
    String[] value() default {};

    /**
     * Limit this controller only for specified users.
     * Accepts array of comma-delimited user ids.
     */
    @AliasFor("value")
    String[] userId() default {};
}
