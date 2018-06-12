package name.maratik.spring.telegram;

import name.maratik.spring.telegram.annotation.TelegramBot;
import name.maratik.spring.telegram.annotation.TelegramForward;
import name.maratik.spring.telegram.annotation.TelegramHelp;
import name.maratik.spring.telegram.annotation.TelegramMessage;
import name.maratik.spring.telegram.annotation.TelegramCommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;

import javax.annotation.Priority;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

/**
 * Bean Post Processor from Telegram Bot API.
 * @author <a href="mailto:maratik@yandex-team.ru">Marat Bukharov</a>
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Priority(10)
public class TelegramBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LogManager.getLogger(TelegramBeanPostProcessor.class);

    private final TelegramBotService telegramBotService;
    private final Map<String, Class<?>> botControllerMap = new HashMap<>();
    private final Map<OptionalLong, Map<String, Class<?>>> botControllerMapByUserId = new HashMap<>();
    private final EmbeddedValueResolver embeddedValueResolver;

    public TelegramBeanPostProcessor(
        TelegramBotService telegramBotService, EmbeddedValueResolver embeddedValueResolver
    ) {
        this.telegramBotService = telegramBotService;
        this.embeddedValueResolver = embeddedValueResolver;
    }

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        TelegramBot telegramBot = AnnotatedElementUtils.findMergedAnnotation(beanClass, TelegramBot.class);
        if (telegramBot != null) {
            if (telegramBot.userId().length != 0) {
                for (String userId : telegramBot.userId()) {
                    String evalUserId = embeddedValueResolver.resolveStringValue(userId);
                    if (evalUserId == null) {
                        throw new RuntimeException("NPE at beanClass: " + beanClass + " on userId: " + userId);
                    }
                    for (String evaluatedUserId : evalUserId.split(",")) {
                        logger.info("Init TelegramBot controller: {} for userId: {}", beanClass, userId);
                        botControllerMapByUserId.computeIfAbsent(OptionalLong.of(Long.parseLong(evaluatedUserId)), key -> new HashMap<>())
                            .put(beanName, beanClass);
                    }
                }
            } else {
                logger.info("Init TelegramBot controller: {}", beanClass);
                botControllerMap.put(beanName, beanClass);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, String beanName) throws BeansException {
        bindControllers(bean, beanName, botControllerMap.get(beanName), OptionalLong.empty());
        botControllerMapByUserId.forEach((userId, original) ->
            bindControllers(bean, beanName, original.get(beanName), userId)
        );
        return bean;
    }

    private void bindControllers(@NonNull Object bean, String beanName, Class<?> original, OptionalLong userId) {
        if (original != null) {
            logger.info("Processing class {} as bean {} for user {}",
                bean::getClass,  () -> beanName, () -> userId
            );
            for (Method method : original.getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                logger.info("Found method {}", method::getName);
                if (AnnotatedElementUtils.hasAnnotation(method, TelegramCommand.class)) {
                    bindCommandController(bean, method, userId);
                }
                if (AnnotatedElementUtils.hasAnnotation(method, TelegramMessage.class)) {
                    bindMessageController(bean, method, userId);
                }
                if (AnnotatedElementUtils.hasAnnotation(method, TelegramForward.class)) {
                    bindForwardController(bean, method, userId);
                }
                if (AnnotatedElementUtils.hasAnnotation(method, TelegramHelp.class)) {
                    bindHelpPrefix(bean, method, userId);
                }
            }
        }
        telegramBotService.addHelpMethod(userId);
    }

    private void bindMessageController(Object bean, Method method, OptionalLong userId) {
        logger.info("Init TelegramBot message controller: {}:{} for {}",
            bean::getClass, method::getName, () -> userId
        );
        telegramBotService.addDefaultMessageHandler(bean, method, userId);
    }

    private void bindCommandController(Object bean, Method method, OptionalLong userId) {
        logger.info("Init TelegramBot command controller: {}:{} for {}",
            bean::getClass, method::getName, () -> userId
        );
        telegramBotService.addHandler(bean, method, userId);
    }

    private void bindForwardController(Object bean, Method method, OptionalLong userId) {
        logger.info("Init TelegramBot forward controller: {}:{} for {}",
            bean::getClass, method::getName, () -> userId
        );
        telegramBotService.addForwardMessageHandler(bean, method, userId);
    }

    private void bindHelpPrefix(Object bean, Method method, OptionalLong userId) {
        logger.info("Init TelegramBot help prefix method: {}:{} for {}",
            bean::getClass, method::getName, () -> userId
        );
        telegramBotService.addHelpPrefixMethod(bean, method, userId);
    }
}
