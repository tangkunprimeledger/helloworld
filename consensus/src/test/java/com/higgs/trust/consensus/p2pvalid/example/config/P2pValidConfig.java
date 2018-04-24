package com.higgs.trust.consensus.p2pvalid.example.config;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.consensus.p2pvalid.example.StringValidConsensus;
import com.higgs.trust.consensus.p2pvalid.example.spi.impl.ClusterInfoImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class P2pValidConfig {

    @Autowired
    private P2pConsensusClient p2pConsensusClient;

    @Value("${higgs.trust.nodeName}")
    private String myNodeName;

    @Bean
    public HttpMessageConverters HttpMessageConverters(){
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteMapNullValue, SerializerFeature.SortField, SerializerFeature.MapSortField,
                SerializerFeature.WriteClassName);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(supportedMediaTypes);
        return new HttpMessageConverters(fastConverter);
    }

    @Bean
    public ValidConsensus validConsensus(){
        if(StringUtils.isEmpty(myNodeName)){
            myNodeName = "";
        }
        myNodeName = myNodeName.toUpperCase();
        ClusterInfo clusterInfo = ClusterInfoImpl.of().setPubKey("pubKey").setPrivateKey("privateKey").setMyNodeName(myNodeName).setClusterNodeNames(new ArrayList<String>(){{add(myNodeName);}});
        ValidConsensus validConsensus = new StringValidConsensus(clusterInfo, p2pConsensusClient,"D:/temp/1/2/3/testTheValidFile");
        log.info("{}", validConsensus);
        return validConsensus;
    }
}
