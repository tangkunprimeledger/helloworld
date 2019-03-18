package com.higgs.trust.evmcontract.solidity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chen Jiawei
 * @date 2019-01-02
 */
public class EventParser implements MemberParser<String, Abi.Event> {
    private static final EventParser INSTANCE = new EventParser();

    private EventParser() {
    }

    public static EventParser getInstance() {
        return INSTANCE;
    }


    private static final Pattern EVENT_SIGNATURE = Pattern.compile("^(\\w+?)\\((.+?)\\)$");
    private static final String PARAMETER_TYPE_SEPARATOR = ",";
    private static final String INDEXED_SEPARATOR = " ";

    @Override
    public Abi.Event parse(String eventSignature) {
        Matcher matcher = EVENT_SIGNATURE.matcher(eventSignature);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Event signature is illegal");
        }

        String params = matcher.group(2).trim();
        if (StringUtils.isEmpty(params)) {
            throw new IllegalArgumentException("Event parameter list cannot be empty");
        }
        if (params.startsWith(PARAMETER_TYPE_SEPARATOR) || params.endsWith(PARAMETER_TYPE_SEPARATOR)) {
            throw new IllegalArgumentException(
                    String.format("Event signature can not begin or end with %s", PARAMETER_TYPE_SEPARATOR));
        }

        String eventName = matcher.group(1).trim();
        List<Abi.Entry.Param> eventInputs = new ArrayList<>();
        boolean indexedOver = false;
        int indexedCount = 0;
        for (String paramType : params.split(PARAMETER_TYPE_SEPARATOR)) {
            String[] paramPart = paramType.split(INDEXED_SEPARATOR);
            Abi.Entry.Param param = new Abi.Entry.Param();
            if (paramPart.length == 1) {
                param.type = SolidityType.getType(paramPart[0]);
                param.indexed = false;
                indexedOver = true;
            } else if (paramPart.length == 2
                    && "indexed".equals(paramPart[1]) && !indexedOver && indexedCount < 3) {
                param.type = SolidityType.getType(paramPart[0]);
                param.indexed = true;
                indexedCount++;
            } else {
                throw new IllegalArgumentException(
                        String.format("Event parameter \"%s\" is illegal", paramType));
            }
            eventInputs.add(param);
        }

        return new Abi.Event(false, eventName, eventInputs, null);
    }
}
