package com.higgs.trust.consensus.p2pvalid.api.controller;

import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author cwy
 */
@RequestMapping(value = "/consensus/p2p")
@RestController
@Slf4j
public class P2pConsensusController implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Map<Class<?>, ValidConsensus> validConsensusRegistry = new HashMap<>();

    @RequestMapping(value = "/receive_command", method = RequestMethod.POST)
    @ResponseBody
    public String receiveCommand(@RequestBody ValidCommandWrap validCommandWrap) {
        ValidConsensus validConsensus = validConsensusRegistry.get(validCommandWrap.getCommandClass());
        if (null == validConsensus) {
            throw new RuntimeException(String.format("no validConsensus mapping to the validCommandClass %s", validCommandWrap.getCommandClass().getSimpleName()));
        }
        validConsensus.receive(validCommandWrap);
        return "SUCCESS";
    }


    @PostConstruct
    private void getValidConsensusMap() {
        Map<String, ValidConsensus> validConsensusMap = applicationContext.getBeansOfType(ValidConsensus.class);
        if (null == validConsensusMap || validConsensusMap.size() == 0) {
            return;
        }
        validConsensusMap.forEach((name, validConsensus) -> {
            Set<Class<?>> classSet = validConsensus.getValidExecutor().getKeySet();
            classSet.forEach((Class<?> clazz) -> {
                if (validConsensusRegistry.keySet().contains(clazz)) {
                    throw new RuntimeException(String.format("duplicate validConsensus %s to validCommand %s", name, clazz.getName()));
                }
                validConsensusRegistry.putIfAbsent(clazz, validConsensus);
            });
        });

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
