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
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class TelegramBotConfiguration implements ImportAware {
    @Override
    public void setImportMetadata(@NonNull AnnotationMetadata importMetadata) {
    }

    @Bean
    public TelegramBeanPostProcessor telegramBeanPostProcessor(
        TelegramBotService telegramBotService, @TelegramBotInternal EmbeddedValueResolver embeddedValueResolver
    ) {
        return new TelegramBeanPostProcessor(telegramBotService, embeddedValueResolver);
    }

    @Bean
    public TelegramBotService telegramBotService(
        TelegramBotType telegramBotType, TelegramBotBuilder telegramBotBuilder, TelegramBotsApi api,
        @TelegramBotInternal EmbeddedValueResolver embeddedValueResolver
    ) {
        return telegramBotType.createService(telegramBotBuilder, api, embeddedValueResolver);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        ApiContextInitializer.init();
        return new TelegramBotsApi();
    }

    @Bean
    @TelegramBotInternal
    public EmbeddedValueResolver embeddedValueResolver(ConfigurableBeanFactory configurableBeanFactory) {
        return new EmbeddedValueResolver(configurableBeanFactory);
    }
}
