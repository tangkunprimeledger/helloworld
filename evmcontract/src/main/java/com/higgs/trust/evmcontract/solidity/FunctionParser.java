package com.higgs.trust.evmcontract.solidity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chen Jiawei
 * @date 2018-12-29
 */
public class FunctionParser implements MemberParser<String, Abi.Function> {
    private static final FunctionParser INSTANCE = new FunctionParser();

    private FunctionParser() {
    }

    public static FunctionParser getInstance() {
        return INSTANCE;
    }


    private static final Pattern FUNCTION_SIGNATURE =
            Pattern.compile("^\\s*+(\\((.*?)\\))?+\\s*+(\\w++)\\s*+(\\((.*?)\\))?+\\s*+$");
    private static final String FUNCTION_SIGNATURE_FORMAT =
            "([returnType1[, returnType2[...]]) functionName([parameterType1[, parameterType2[...]])";

    private static final Pattern TYPE_LIST_SIGNATURE =
            Pattern.compile("^\\s*$|^\\s*\\w+\\s*$|^(\\s*\\w+\\s*,)+\\s*\\w+\\s*$");

    private static final Pattern REPLACE_SOURCE = Pattern.compile("\\s");
    private static final String EMPTY_STRING = "";
    private static final String TYPE_SEPARATOR = ",";

    @Override
    public Abi.Function parse(String functionSignature) {
        Matcher matcherFunction = FUNCTION_SIGNATURE.matcher(functionSignature);
        if (!matcherFunction.find()) {
            throw new IllegalArgumentException(
                    String.format("Function signature is inappropriate: %s. Its format " +
                            "should be like this: %s", functionSignature, FUNCTION_SIGNATURE_FORMAT));
        }

        String parameterTypes = matcherFunction.group(5);
        parameterTypes = parameterTypes == null ? EMPTY_STRING : parameterTypes;
        Matcher matcherParameters = TYPE_LIST_SIGNATURE.matcher(parameterTypes);
        if (!matcherParameters.find()) {
            throw new IllegalArgumentException(String.format("Parameter types are inappropriate: %s", parameterTypes));
        }

        String returnTypes = matcherFunction.group(2);
        returnTypes = returnTypes == null ? EMPTY_STRING : returnTypes;
        Matcher matcherReturns = TYPE_LIST_SIGNATURE.matcher(returnTypes);
        if (!matcherReturns.find()) {
            throw new IllegalArgumentException(String.format("Return types are inappropriate: %s", returnTypes));
        }

        List<Abi.Entry.Param> inputs = buildParameters(extractWords(normalize(parameterTypes)));
        List<Abi.Entry.Param> outputs = buildParameters(extractWords(normalize(returnTypes)));
        String functionName = matcherFunction.group(3);

        return new Abi.Function(false, functionName, inputs, outputs, false);
    }


    private String normalize(String typeListSignature) {
        return REPLACE_SOURCE.matcher(typeListSignature).replaceAll(EMPTY_STRING);
    }

    private List<String> extractWords(String normalizedTypeListSignature) {
        if (normalizedTypeListSignature.startsWith(TYPE_SEPARATOR) || normalizedTypeListSignature.endsWith(TYPE_SEPARATOR)) {
            throw new IllegalArgumentException(
                    String.format("Type list cannot begin or end with %s", TYPE_SEPARATOR));
        }

        if (StringUtils.isEmpty(normalizedTypeListSignature)) {
            return Collections.emptyList();
        }

        return Arrays.asList(normalizedTypeListSignature.split(TYPE_SEPARATOR));
    }

    private List<Abi.Entry.Param> buildParameters(List<String> solidityTypes) {
        List<Abi.Entry.Param> parameters = new ArrayList<>();

        solidityTypes.forEach(solidityType -> {
            Abi.Entry.Param param = new Abi.Entry.Param();
            param.type = SolidityType.getType(solidityType);
            parameters.add(param);
        });

        return parameters;
    }
}
