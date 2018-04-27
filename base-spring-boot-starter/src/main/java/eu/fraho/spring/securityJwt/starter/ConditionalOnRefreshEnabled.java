package eu.fraho.spring.securityJwt.starter;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class ConditionalOnRefreshEnabled implements Condition {
    @Override
    public boolean matches(@NotNull @NonNull ConditionContext conditionContext, @NotNull @NonNull AnnotatedTypeMetadata annotatedTypeMetadata) {
        return !Objects.equals(
                SecurityJwtNoRefreshStoreAutoConfiguration.class.getName(),
                conditionContext.getBeanFactory().getBeanDefinition("refreshTokenStore").getFactoryBeanName()
        );
    }
}
