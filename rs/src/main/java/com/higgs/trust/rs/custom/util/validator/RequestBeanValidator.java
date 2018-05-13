package com.higgs.trust.rs.custom.util.validator;

import com.google.common.base.Preconditions;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * 请求参数校验
 *
 * @author kongyu
 * @create 2017-12-11
 */
public class RequestBeanValidator {
    // validator is thread-safe
    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RequestBeanValidator() {
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
