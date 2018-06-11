package maratik.name.spring.telegram.config;

import maratik.name.spring.telegram.LongPollingTelegramBotService;
import maratik.name.spring.telegram.TelegramBotService;
import maratik.name.spring.telegram.WebhookTelegramBotService;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.telegram.telegrambots.TelegramBotsApi;

/**
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public enum TelegramBotType {
    LONG_POLLING {
        @Override
        public TelegramBotService createService(
            TelegramBotBuilder botBuilder, TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver
        ) {
            return new LongPollingTelegramBotService(botBuilder, api, embeddedValueResolver);
        }
    },
    WEBHOOK {
        @Override
        public TelegramBotService createService(
            TelegramBotBuilder botBuilder, TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver
        ) {
            return new WebhookTelegramBotService(botBuilder, api, embeddedValueResolver);
        }
    };

    public abstract TelegramBotService createService(
        TelegramBotBuilder botBuilder, TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver
    );
}
