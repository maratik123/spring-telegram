package maratik.name.spring.telegram.config;

import maratik.name.spring.telegram.TelegramBeanPostProcessor;
import maratik.name.spring.telegram.TelegramBotService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;

/**
 * Configuration which will be used to initialize telegram bot api.
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
        TelegramBotService telegramBotService, @TelegramBotInternal EmbeddedValueResolver embeddedValueResolver
    ) {
        return new TelegramBeanPostProcessor(telegramBotService, embeddedValueResolver);
    }

    /**
     * Telegram Bot Service to dispatch messages.
     */
    @Bean
    public TelegramBotService telegramBotService(
        TelegramBotType telegramBotType, TelegramBotBuilder telegramBotBuilder, TelegramBotsApi api,
        @TelegramBotInternal EmbeddedValueResolver embeddedValueResolver
    ) {
        return telegramBotType.createService(telegramBotBuilder, api, embeddedValueResolver);
    }

    /**
     * Telegram Bots API.
     */
    @Bean
    public TelegramBotsApi telegramBotsApi() {
        ApiContextInitializer.init();
        return new TelegramBotsApi();
    }

    /**
     * Internal value resolver to support value processing using properties sources and SPeL.
     */
    @Bean
    @TelegramBotInternal
    public EmbeddedValueResolver internalTelegramApiEmbeddedValueResolver(
        ConfigurableBeanFactory configurableBeanFactory
    ) {
        return new EmbeddedValueResolver(configurableBeanFactory);
    }
}
