/*
 * MIT Licence
 * Copyright (c) 2025 Simon Frankenberger
 *
 * Please see LICENCE.md for complete licence text.
 */
package eu.fraho.spring.securityJwt.base.starter;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class ConditionalOnRefreshEnabled implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return !Objects.equals(
                SecurityJwtNoRefreshStoreAutoConfiguration.class.getName(),
                Objects.requireNonNull(conditionContext.getBeanFactory()).getBeanDefinition("refreshTokenStore").getFactoryBeanName()
        );
    }
}
