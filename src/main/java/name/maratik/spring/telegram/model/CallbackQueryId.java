package name.maratik.spring.telegram.model;

/**
 * Callback query ID.
 *
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
public class CallbackQueryId {
    private final String id;

    /**
     * Constructs callback query id.
     *
     * @param id callback query id
     */
    public CallbackQueryId(String id) {
        this.id = id;
    }

    /**
     * Returns callback query id.
     *
     * @return callback query id
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CallbackQueryId)) {
            return false;
        }

        CallbackQueryId that = (CallbackQueryId) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "CallbackQueryId{" +
            "id='" + id + '\'' +
            '}';
    }
}
