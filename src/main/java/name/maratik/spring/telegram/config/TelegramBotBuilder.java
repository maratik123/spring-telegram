package name.maratik.spring.telegram.config;

/**
 * Builder for Telegram Bot API. Should be provided as bean.
 *
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class TelegramBotBuilder {

    private static final int DEFAULT_MAX_THREADS = 30;

    private String username;
    private String token;
    private String path;
    private int maxThreads = DEFAULT_MAX_THREADS;

    public TelegramBotBuilder() {
    }

    public TelegramBotBuilder(String username, String token) {
        this.username = username;
        this.token = token;
    }

    /**
     * Bot username.
     */
    public TelegramBotBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Bot token.
     */
    public TelegramBotBuilder token(String token) {
        this.token = token;
        return this;
    }

    /**
     * URL path used for webhook. It is not required for long polling mode.
     */
    public TelegramBotBuilder path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Maximum parallel threads used to process messages in long polling mode.
     */
    public TelegramBotBuilder maxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public String toString() {
        return "TelegramBotBuilder{" +
            "username='" + username + '\'' +
            ", token='" + token + '\'' +
            ", path='" + path + '\'' +
            ", maxThreads=" + maxThreads +
            '}';
    }
}
