package maratik.name.spring.telegram.config;

/**
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

    public TelegramBotBuilder username(String username) {
        this.username = username;
        return this;
    }

    public TelegramBotBuilder token(String token) {
        this.token = token;
        return this;
    }

    @SuppressWarnings("unused")
    public TelegramBotBuilder path(String path) {
        this.path = path;
        return this;
    }

    @SuppressWarnings("unused")
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
