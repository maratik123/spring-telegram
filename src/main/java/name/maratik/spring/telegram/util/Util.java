package name.maratik.spring.telegram.util;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Various utils.
 *
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Util {
    /**
     * Make {@link OptionalLong} from {@link Long}.
     */
    public static OptionalLong optionalOf(Long l) {
        return toOptionalLong(Optional.ofNullable(l));
    }

    /**
     * Make {@link OptionalInt} from {@link Integer}.
     */
    public static OptionalInt optionalOf(Integer i) {
        return toOptionalInt(Optional.ofNullable(i));
    }

    /**
     * Make {@link OptionalLong} from {@link Optional}.
     */
    public static OptionalLong toOptionalLong(Optional<Long> optional) {
        return optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    }

    /**
     * Make {@link OptionalInt} from {@link Optional}.
     */
    public static OptionalInt toOptionalInt(Optional<Integer> optional) {
        return optional.map(OptionalInt::of).orElseGet(OptionalInt::empty);
    }
}
