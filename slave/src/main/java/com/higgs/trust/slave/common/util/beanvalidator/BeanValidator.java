package com.higgs.trust.slave.common.util.beanvalidator;

import com.google.common.base.Preconditions;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author ShenTeng
 */
public class BeanValidator {

    // validator is thread-safe
    private static Validator validator;

    static {
        validator = Validation.byProvider(HibernateValidator.class).configure().failFast(true).buildValidatorFactory()
            .getValidator();
    }

    private BeanValidator() {
    }

    public static Validator getValidator() {
        return validator;
    }

    public static <T> BeanValidateResult<T> validate(T object) {
        Preconditions.checkNotNull(object, "validate object is null");
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object);
        return new BeanValidateResult<>(constraintViolations);
    }

    public static <T> BeanValidateResult<T> validate(T object, Class<?>... groups) {
        Preconditions.checkNotNull(object, "validate object is null");
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, groups);
        return new BeanValidateResult<>(constraintViolations);
    }
}
