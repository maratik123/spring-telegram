package maratik.name.spring.telegram.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TelegramCommand {
    @AliasFor("commands")
    String[] value() default {};
    @AliasFor("value")
    String[] commands() default {};
    String description() default "";

    boolean hidden() default false;
    boolean isHelp() default false;
}
