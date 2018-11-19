package name.maratik.spring.telegram.config;

import name.maratik.spring.telegram.TelegramBeanPostProcessor;
import name.maratik.spring.telegram.TelegramBotService;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

/**
 * Configuration which will be used to initialize telegram bot api.
 *
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class TelegramBotConfiguration implements ImportAware {
    @Override
    public void setImportMetadata(@NonNull AnnotationMetadata importMetadata) {
    }

    /**
     * Bean post-processor to process Telegram Bot API annotations.
     */
    @Bean
    public TelegramBeanPostProcessor telegramBeanPostProcessor(
        TelegramBotService telegramBotService, ConfigurableBeanFactory configurableBeanFactory
    ) {
        return new TelegramBeanPostProcessor(telegramBotService, configurableBeanFactory);
    }

    /**
     * Telegram Bot Service to dispatch messages.
     */
    @Bean
    public TelegramBotService telegramBotService(
        TelegramBotType telegramBotType, TelegramBotBuilder telegramBotBuilder, TelegramBotsApi api,
        ConfigurableBeanFactory configurableBeanFactory
    ) {
        return telegramBotType.createService(telegramBotBuilder, api, configurableBeanFactory);
    }

    /**
     * Telegram Bots API.
     */
    @Bean
    public TelegramBotsApi telegramBotsApi() {
        ApiContextInitializer.init();
        return new TelegramBotsApi();
    }
}
