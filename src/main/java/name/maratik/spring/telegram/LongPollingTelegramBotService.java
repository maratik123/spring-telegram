package name.maratik.spring.telegram;

import name.maratik.spring.telegram.config.TelegramBotBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Long polling implementation of Telegram Bot Service.
 *
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class LongPollingTelegramBotService extends TelegramBotService implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(LongPollingTelegramBotService.class);

    private final String username;
    private final String token;
    private final ExecutorService botExecutor;
    private final TelegramLongPollingBot client;

    public LongPollingTelegramBotService(
        TelegramBotBuilder botBuilder, TelegramBotsApi api, ConfigurableBeanFactory configurableBeanFactory
    ) {
        super(api, configurableBeanFactory);
        logger.info("Registering Long Polling with {}", botBuilder);
        username = botBuilder.getUsername();
        token = botBuilder.getToken();
        botExecutor = new ThreadPoolExecutor(1, botBuilder.getMaxThreads(),
            1L, TimeUnit.HOURS,
            new SynchronousQueue<>()
        );

        client = new TelegramBotLongPollingImpl();
        try {
            api.registerBot(client);
        } catch (TelegramApiRequestException e) {
            logger.error("Can not register Long Polling with {}", botBuilder, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public TelegramLongPollingBot getClient() {
        return client;
    }

    @Override
    public void close() {
        botExecutor.shutdown();
        boolean terminated = false;
        try {
            terminated = botExecutor.awaitTermination(5, TimeUnit.SECONDS);
            if (!terminated) {
                logger.error("Bot executor did not terminated in 5 seconds");
            }
        } catch (InterruptedException e) {
            logger.error("Bot executor service termination awaiting failed", e);
        }

        if (!terminated) {
            int droppedTasks = botExecutor.shutdownNow().size();
            logger.error("Executor was abruptly shut down. {} tasks will not be executed", droppedTasks);
        }
    }

    private class TelegramBotLongPollingImpl extends TelegramLongPollingBot {
        @Override
        public void onUpdateReceived(Update update) {
            CompletableFuture.runAsync(() ->
                updateProcess(update).ifPresent(result -> {
                    try {
                        getClient().execute(result);
                        logger.debug("Update: {}. Message: {}. Successfully sent", update, result);
                    } catch (TelegramApiException e) {
                        logger.error("Update: {}. Can not send message {} to telegram: ", update, result, e);
                    }
                }), botExecutor);
        }

        @Override
        public String getBotUsername() {
            return username;
        }

        @Override
        public String getBotToken() {
            return token;
        }
    }
}
