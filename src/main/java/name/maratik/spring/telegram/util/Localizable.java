package name.maratik.spring.telegram.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.NonNull;

/**
 * Helper is used for localization. Can be inherited by user. Or used as "loc" bean solely.
 *
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class Localizable implements ApplicationContextAware {
    private MessageSourceAccessor messageSourceAccessor;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        messageSourceAccessor = new MessageSourceAccessor(applicationContext);
    }

    /**
     * Search in resource bundle by code.
     */
    public String t(String code) {
        return messageSourceAccessor.getMessage(code);
    }

    /**
     * Search in resource bundle by code with arguments.
     */
    public String t(String code, Object... args) {
        return messageSourceAccessor.getMessage(code, args);
    }

    /**
     * Search in resource bundle by code in {@link LocalizableValue#getTranslationTag()}.
     */
    public String t(LocalizableValue localizableValue) {
        return t(localizableValue.getTranslationTag());
    }

    /**
     * Search in resource bundle by code in {@link LocalizableValue#getTranslationTag()} with arguments.
     */
    public String t(LocalizableValue localizableValue, Object... args) {
        return t(localizableValue.getTranslationTag(), args);
    }
}
