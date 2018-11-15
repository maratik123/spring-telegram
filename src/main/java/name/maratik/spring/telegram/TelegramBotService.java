package name.maratik.spring.telegram;

import name.maratik.spring.telegram.annotation.TelegramCallbackQuery;
import name.maratik.spring.telegram.annotation.TelegramCommand;
import name.maratik.spring.telegram.annotation.TelegramForward;
import name.maratik.spring.telegram.annotation.TelegramHelp;
import name.maratik.spring.telegram.annotation.TelegramMessage;
import name.maratik.spring.telegram.model.TelegramBotCommand;
import name.maratik.spring.telegram.model.TelegramHandler;
import name.maratik.spring.telegram.model.TelegramMessageCommand;
import name.maratik.spring.telegram.util.Util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Telegram Bot Service.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class TelegramBotService implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(TelegramBotService.class);
    private static final String DEFAULT_PATTERN_COMMAND_SUFFIX = "*";
    private static final Comparator<Map.Entry<String, ?>> KEY_LENGTH_COMPARATOR =
        Comparator.comparing(Map.Entry::getKey, Comparator.comparingInt(String::length));
    private static final Comparator<TelegramBotCommand> TELEGRAM_BOT_COMMAND_COMPARATOR =
        Comparator.comparing(
            TelegramBotCommand::getCommand,
            Comparator.comparing(ImmutableSet.of("/license", "/help")::contains)
        ).thenComparing(TelegramBotCommand::getCommand);

    private final Map<OptionalLong, Handlers> handlers = new HashMap<>();
    private final EmbeddedValueResolver embeddedValueResolver;
    private final List<ProcessorDescriptor> processors;
    private final Map<Type, BiFunction<TelegramMessageCommand, Update, ?>> messageArgumentMapper;
    private final Map<Type, Function<Update, ?>> callbackQueryArgumentMapper;

    private String patternCommandSuffix = DEFAULT_PATTERN_COMMAND_SUFFIX;

    /**
     *
     * @param api initialized telegram bots api
     * @param embeddedValueResolver properties and SPeL resolver
     * @param patternCommandSuffix suffix for commands starts with {@code /do_some_*}.
     *                            Default value is {@link TelegramBotService#DEFAULT_PATTERN_COMMAND_SUFFIX}
     */
    public TelegramBotService(
        TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver, String patternCommandSuffix
    ) {
        this.patternCommandSuffix = patternCommandSuffix;
        this.embeddedValueResolver = embeddedValueResolver;

        BiFunction<TelegramMessageCommand, Update, Long> messageUserIdExtractor = (telegramMessageCommand, update) ->
            update.getMessage().getFrom().getId().longValue();

        Function<Update, Long> callbackQueryUserIdExtractor = update ->
            update.getCallbackQuery().getFrom().getId().longValue();

        processors = ImmutableList.of(
            new ProcessorDescriptor(Update::hasMessage, this::messageProcess),
            new ProcessorDescriptor(Update::hasCallbackQuery, this::callbackQueryProcess)
        );

        messageArgumentMapper = ImmutableMap.<Type, BiFunction<TelegramMessageCommand, Update, ?>>builder()
            .put(Update.class, (telegramMessageCommand, update) -> update)
            .put(TelegramMessageCommand.class, (telegramMessageCommand, update) -> telegramMessageCommand)
            .put(String.class, (telegramMessageCommand, update) -> telegramMessageCommand.getArgument().orElse(null))
            .put(TelegramBotsApi.class, (telegramMessageCommand, update) -> api)
            .put(TelegramBotService.class, (telegramMessageCommand, update) -> this)
            .put(DefaultAbsSender.class, (telegramMessageCommand, update) -> getClient())
            .put(Message.class, (telegramMessageCommand, update) -> update.getMessage())
            .put(User.class, (telegramMessageCommand, update) -> update.getMessage().getFrom())
            .put(long.class, messageUserIdExtractor)
            .put(Long.class, messageUserIdExtractor)
            .put(Instant.class, ((telegramMessageCommand, update) -> {
                Message message = update.getMessage();
                return Instant.ofEpochSecond(Optional.ofNullable(message.getForwardDate()).orElse(message.getDate()));
            }))
            .build();

        callbackQueryArgumentMapper = ImmutableMap.<Type, Function<Update, ?>>builder()
            .put(Update.class, update -> update)
            .put(String.class, update -> update.getCallbackQuery().getData())
            .put(TelegramBotsApi.class, update -> api)
            .put(TelegramBotService.class, update -> this)
            .put(DefaultAbsSender.class, update -> getClient())
            .put(CallbackQuery.class, Update::getCallbackQuery)
            .put(Message.class, update -> update.getCallbackQuery().getMessage())
            .put(User.class, update -> update.getCallbackQuery().getFrom())
            .put(long.class, callbackQueryUserIdExtractor)
            .put(Long.class, callbackQueryUserIdExtractor)
            .build();

    }

    public TelegramBotService(TelegramBotsApi api, EmbeddedValueResolver embeddedValueResolver) {
        this(api, embeddedValueResolver, DEFAULT_PATTERN_COMMAND_SUFFIX);
    }

    /**
     * @return suffix for pattern command
     */
    public String getPatternCommandSuffix() {
        return patternCommandSuffix;
    }

    public void setPatternCommandSuffix(String patternCommandSuffix) {
        this.patternCommandSuffix = patternCommandSuffix;
    }

    /**
     * Main dispatcher method which takes {@link Update} object and calls controller method to process update.
     */
    @SuppressWarnings("WeakerAccess")
    public Optional<BotApiMethod<?>> updateProcess(Update update) {
        logger.debug("Update {} received", update);
        return processors.stream()
            .filter(processorDescriptor -> processorDescriptor.test(update))
            .findFirst()
            .flatMap(processorDescriptor -> processorDescriptor.apply(update));
    }

    private Optional<BotApiMethod<?>> callbackQueryProcess(Update update) {
        return Optional.ofNullable(getOrDefault(Util.optionalOf(update
            .getCallbackQuery()
            .getFrom()
            .getId()
            .longValue()
        ))
            .getDefaultCallbackQueryHandler()
        ).flatMap(handler -> handleExceptions(
            () -> processHandler(handler, makeCallbackQueryArgumentList(handler.getMethod(), update)),
            update
        ));
    }

    private Optional<BotApiMethod<?>> messageProcess(Update update) {
        TelegramMessageCommand command = new TelegramMessageCommand(update);
        Optional<TelegramHandler> optionalCommandHandler;
        OptionalLong userKey = Util.optionalOf(update.getMessage().getChatId());
        Handlers handlers = getOrDefault(userKey);

        if (command.getForwardedFrom().isPresent()) {
            optionalCommandHandler = Optional.ofNullable(
                handlers.getForwardHandlerList().getOrDefault(command.getForwardedFrom().getAsLong(),
                    handlers.getDefaultForwardHandler()
                )
            );
        } else {
            Optional<String> commandCommandOpt = command.getCommand();
            optionalCommandHandler = commandCommandOpt.map(cmd -> handlers.getCommandList().get(cmd));
            if (!optionalCommandHandler.isPresent()) {
                if (commandCommandOpt.isPresent()) {
                    String commandCommand = commandCommandOpt.get();
                    optionalCommandHandler = handlers.getPatternCommandList().entrySet().stream()
                        .filter(entry -> commandCommand.startsWith(entry.getKey()))
                        .max(KEY_LENGTH_COMPARATOR)
                        .map(Map.Entry::getValue);
                }
                if (!optionalCommandHandler.isPresent()) {
                    optionalCommandHandler = Optional.ofNullable(handlers.getDefaultMessageHandler());
                }
            }
        }

        logger.debug("Command handler: {}", optionalCommandHandler);

        return optionalCommandHandler.flatMap(commandHandler -> handleExceptions(() -> {
            if (commandHandler.getTelegramCommand().filter(TelegramCommand::isHelp).isPresent()) {
                sendHelpList(update, userKey);
                return Optional.empty();
            }
            return processHandler(commandHandler, makeMessageArgumentList(
                commandHandler.getMethod(), command, update
            ));
        }, update));
    }

    private static <T> Optional<T> handleExceptions(Callable<Optional<T>> callable, Update update) {
        try {
            return callable.call();
        } catch (Exception e) {
            logger.error("Could not process update: {}", update, e);
            return Optional.empty();
        }
    }

    private Optional<BotApiMethod<?>> processHandler(TelegramHandler commandHandler, Object[] arguments) throws IllegalAccessException, InvocationTargetException {
        Method method = commandHandler.getMethod();
        Class<?> methodReturnType = method.getReturnType();
        logger.debug("Derived method return type: {}", methodReturnType);
        if (methodReturnType == void.class || methodReturnType == Void.class) {
            method.invoke(commandHandler.getBean(), arguments);
        } else if (methodReturnType != null &&
            BotApiMethod.class.isAssignableFrom(methodReturnType)) {
            return Optional.ofNullable((BotApiMethod<?>) method.invoke(commandHandler.getBean(), arguments));
        } else {
            logger.error("Unsupported handler '{}'", commandHandler);
        }
        return Optional.empty();
    }

    private <T> void sendHelpList(Update update, OptionalLong userKey) throws TelegramApiException {
        getClient().execute(new SendMessage()
            .setChatId(update.getMessage().getChatId())
            .setText(buildHelpMessage(userKey))
        );
    }

    private String buildHelpMessage(OptionalLong userKey) {
        StringBuilder sb = new StringBuilder();
        String prefixHelpMessage = getOrDefault(userKey).getPrefixHelpMessage();
        if (prefixHelpMessage != null) {
            sb.append(prefixHelpMessage);
        }
        getCommandList(userKey)
            .sorted(TELEGRAM_BOT_COMMAND_COMPARATOR)
            .forEach(method -> sb
                .append(method.getCommand())
                .append(' ')
                .append(embeddedValueResolver.resolveStringValue(method.getDescription()))
                .append('\n')
            );
        return sb.toString();
    }

    /**
     * Enumerates all visible command handlers for given user.
     */
    @SuppressWarnings("WeakerAccess")
    public Stream<TelegramBotCommand> getCommandList(OptionalLong userKey) {
        return Stream.concat(
            getOrDefault(userKey).getCommandList().entrySet().stream()
                .filter(entry -> !entry.getValue().getTelegramCommand().map(TelegramCommand::hidden).orElse(true))
                .map(entry -> new TelegramBotCommand(
                    entry.getKey(),
                    entry.getValue().getTelegramCommand().map(TelegramCommand::description).orElse("")
                )),
            getOrDefault(userKey).getPatternCommandList().entrySet().stream()
                .filter(entry -> !entry.getValue().getTelegramCommand().map(TelegramCommand::hidden).orElse(true))
                .map(entry -> new TelegramBotCommand(
                    entry.getKey() + patternCommandSuffix,
                    entry.getValue().getTelegramCommand().map(TelegramCommand::description).orElse("")
                ))
        );
    }

    /**
     * @return telegram api client implementation
     */
    public abstract DefaultAbsSender getClient();

    private Object[] makeMessageArgumentList(Method method, TelegramMessageCommand telegramMessageCommand, Update update) {
        return Arrays.stream(method.getGenericParameterTypes())
            .map(type -> messageArgumentMapper.getOrDefault(type, (t, u) -> null))
            .map(mapper -> mapper.apply(telegramMessageCommand, update))
            .toArray();
    }

    private Object[] makeCallbackQueryArgumentList(Method method, Update update) {
        return Arrays.stream(method.getGenericParameterTypes())
            .map(type -> callbackQueryArgumentMapper.getOrDefault(type, u -> null))
            .map(mapper -> mapper.apply(update))
            .toArray();
    }

    /**
     * Add {@link TelegramCommand} handler.
     */
    @SuppressWarnings("WeakerAccess")
    public void addHandler(Object bean, Method method, OptionalLong userId) {
        TelegramCommand command = AnnotatedElementUtils.findMergedAnnotation(method, TelegramCommand.class);
        if (command != null) {
            for (String cmd : command.commands()) {
                TelegramHandler telegramHandler = new TelegramHandler(bean, method, command);
                if (cmd.endsWith(patternCommandSuffix)) {
                    createOrGet(userId).getPatternCommandList()
                        .put(cmd.substring(0, cmd.length() - patternCommandSuffix.length()), telegramHandler);
                } else {
                    createOrGet(userId).getCommandList().put(cmd, telegramHandler);
                }
            }
        }
    }

    /**
     * Add {@link TelegramMessage} handler.
     */
    @SuppressWarnings("WeakerAccess")
    public void addDefaultMessageHandler(Object bean, Method method, OptionalLong userId) {
        createOrGet(userId).setDefaultMessageHandler(new TelegramHandler(bean, method, null));
    }

    /**
     * Add {@link TelegramCallbackQuery} handler.
     */
    @SuppressWarnings("WeakerAccess")
    public void addDefaultCallbackQueryHandler(Object bean, Method method, OptionalLong userId) {
        createOrGet(userId).setDefaultCallbackQueryHandler(new TelegramHandler(bean, method, null));
    }

    /**
     * Add {@link TelegramForward} handler.
     */
    @SuppressWarnings("WeakerAccess")
    public void addForwardMessageHandler(Object bean, Method method, OptionalLong userId) {
        TelegramForward forward = AnnotatedElementUtils.findMergedAnnotation(method, TelegramForward.class);
        if (forward != null) {
            String[] fromArr = forward.from();
            if (fromArr.length == 0) {
                createOrGet(userId).setDefaultForwardHandler(new TelegramHandler(bean, method, null));
            } else {
                for (String from : fromArr) {
                    String parsedFromStr = embeddedValueResolver.resolveStringValue(from);
                    if (parsedFromStr == null) {
                        throw new RuntimeException("NPE in " + from);
                    }
                    for (String fromValue : parsedFromStr.split(",")) {
                        Long parsedFrom = Long.valueOf(fromValue);
                        createOrGet(userId).getForwardHandlerList()
                            .put(parsedFrom, new TelegramHandler(bean, method, null));
                    }
                }
            }
        }
    }

    /**
     * Add help method for {@code userKey}.
     */
    @SuppressWarnings("WeakerAccess")
    public void addHelpMethod(OptionalLong userKey) {
        try {
            Method helpMethod = getClass().getMethod("helpMethod");
            TelegramCommand command = AnnotatedElementUtils.findMergedAnnotation(helpMethod, TelegramCommand.class);
            if (command != null) {
                for (String cmd : command.commands()) {
                    createOrGet(userKey).getCommandList()
                        .put(cmd, new TelegramHandler(this, helpMethod, command));
                }
            }
        } catch (Exception e) {
            logger.error("Could not add help method", e);
        }
    }

    /**
     * Default help method.
     */
    @SuppressWarnings("WeakerAccess")
    @TelegramCommand(
        commands = "/help",
        isHelp = true,
        description = "#{@loc?.t('TelegramBotService.HELP.DESC')?:'This help'}"
    )
    public void helpMethod() {
    }

    @Override
    public void close() {
    }

    /**
     * Handler for {@link TelegramHelp} method.
     */
    @SuppressWarnings("WeakerAccess")
    public void addHelpPrefixMethod(Object bean, Method method, OptionalLong userId) {
        try {
            createOrGet(userId).setPrefixHelpMessage(method.invoke(bean).toString());
        } catch (Exception e) {
            logger.error("Can not get help prefix", e);
        }
    }

    private Handlers getOrDefault(OptionalLong key) {
        if (!handlers.containsKey(key)) {
            return handlers.get(OptionalLong.empty());
        }
        return handlers.get(key);
    }

    private Handlers createOrGet(OptionalLong key) {
        return handlers.computeIfAbsent(key, k -> new Handlers());
    }

    private static class Handlers {
        private final Map<String, TelegramHandler> commandList = new HashMap<>();
        private final Map<String, TelegramHandler> patternCommandList = new HashMap<>();
        private final Map<Long, TelegramHandler> forwardHandlerList = new HashMap<>();
        private TelegramHandler defaultMessageHandler;
        private TelegramHandler defaultForwardHandler;
        private TelegramHandler defaultCallbackQueryHandler;
        private String prefixHelpMessage;

        private Map<String, TelegramHandler> getCommandList() {
            return commandList;
        }

        private Map<String, TelegramHandler> getPatternCommandList() {
            return patternCommandList;
        }

        private Map<Long, TelegramHandler> getForwardHandlerList() {
            return forwardHandlerList;
        }

        private TelegramHandler getDefaultMessageHandler() {
            return defaultMessageHandler;
        }

        private void setDefaultMessageHandler(TelegramHandler defaultMessageHandler) {
            this.defaultMessageHandler = defaultMessageHandler;
        }

        private TelegramHandler getDefaultCallbackQueryHandler() {
            return defaultCallbackQueryHandler;
        }

        private void setDefaultCallbackQueryHandler(TelegramHandler defaultCallbackQueryHandler) {
            this.defaultCallbackQueryHandler = defaultCallbackQueryHandler;
        }

        private TelegramHandler getDefaultForwardHandler() {
            return defaultForwardHandler;
        }

        private void setDefaultForwardHandler(TelegramHandler defaultForwardHandler) {
            this.defaultForwardHandler = defaultForwardHandler;
        }

        private String getPrefixHelpMessage() {
            return prefixHelpMessage;
        }

        private void setPrefixHelpMessage(String prefixHelpMessage) {
            this.prefixHelpMessage = prefixHelpMessage;
        }
    }

    private static class ProcessorDescriptor implements Predicate<Update>, Function<Update, Optional<BotApiMethod<?>>> {
        private final Predicate<Update> predicate;
        private final Function<Update, Optional<BotApiMethod<?>>> function;

        private ProcessorDescriptor(Predicate<Update> predicate, Function<Update, Optional<BotApiMethod<?>>> function) {
            this.predicate = predicate;
            this.function = function;
        }

        @Override
        public Optional<BotApiMethod<?>> apply(Update update) {
            return function.apply(update);
        }

        @Override
        public boolean test(Update update) {
            return predicate.test(update);
        }
    }
}
