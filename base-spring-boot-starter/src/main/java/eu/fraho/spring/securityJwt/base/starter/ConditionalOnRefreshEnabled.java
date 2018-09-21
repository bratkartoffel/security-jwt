package eu.fraho.spring.securityJwt.base.starter;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class ConditionalOnRefreshEnabled implements Condition {
    @Override
    public boolean matches(@NotNull ConditionContext conditionContext, @NotNull AnnotatedTypeMetadata annotatedTypeMetadata) {
        return !Objects.equals(
                SecurityJwtNoRefreshStoreAutoConfiguration.class.getName(),
                Objects.requireNonNull(conditionContext.getBeanFactory()).getBeanDefinition("refreshTokenStore").getFactoryBeanName()
        );
    }
}
