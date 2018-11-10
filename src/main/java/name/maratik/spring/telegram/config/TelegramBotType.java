package name.maratik.spring.telegram.config;

import name.maratik.spring.telegram.LongPollingTelegramBotService;
import name.maratik.spring.telegram.TelegramBotService;
import name.maratik.spring.telegram.WebhookTelegramBotService;

import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.telegram.telegrambots.meta.TelegramBotsApi;

/**
 * Telegram Bot type.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public enum TelegramBotType {
    /**
     * Use long polling mode.
     */
    LONG_POLLING {
        @Override
        public TelegramBotService createService(
            TelegramBotBuilder botBuilder, TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver
        ) {
            return new LongPollingTelegramBotService(botBuilder, api, embeddedValueResolver);
        }
    },
    /**
     * Use webhook mode.
     */
    WEBHOOK {
        @Override
        public TelegramBotService createService(
            TelegramBotBuilder botBuilder, TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver
        ) {
            return new WebhookTelegramBotService(botBuilder, api, embeddedValueResolver);
        }
    };

    /**
     * Creates TelegramBotService bean.
     */
    public abstract TelegramBotService createService(
        TelegramBotBuilder botBuilder, TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver
    );
}
