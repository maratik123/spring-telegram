package maratik.name.spring.telegram.util;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Util {
    public static OptionalLong optionalOf(Long l) {
        return toOptionalLong(Optional.ofNullable(l));
    }

    public static OptionalInt optionalOf(Integer i) {
        return toOptionalInt(Optional.ofNullable(i));
    }

    public static OptionalLong toOptionalLong(Optional<Long> optional) {
        return optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    }

    public static OptionalInt toOptionalInt(Optional<Integer> optional) {
        return optional.map(OptionalInt::of).orElseGet(OptionalInt::empty);
    }
}
