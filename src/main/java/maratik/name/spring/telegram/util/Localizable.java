package maratik.name.spring.telegram.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.NonNull;

/**
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class Localizable implements ApplicationContextAware {
    private MessageSourceAccessor messageSourceAccessor;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        messageSourceAccessor = new MessageSourceAccessor(applicationContext);
    }

    public String t(String code) {
        return messageSourceAccessor.getMessage(code);
    }

    public String t(String code, Object... args) {
        return messageSourceAccessor.getMessage(code, args);
    }

    public String t(LocalizableValue localizableValue) {
        return t(localizableValue.getTranslationTag());
    }

    public String t(LocalizableValue localizableValue, Object... args) {
        return t(localizableValue.getTranslationTag(), args);
    }
}
