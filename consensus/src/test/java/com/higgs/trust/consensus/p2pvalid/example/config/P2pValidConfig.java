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
        String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDT3gcMfUYTHCyRQ6t89oVC1ZwS" +
                "bjj045VZOPDyqFlcfJK2ZAdw3qw1Io/A47BmtHw0XNS1DsltiA/Kgdl2UKeej73a" +
                "tNNccfTuZE89GRtN5Fp983Wa1Fr9gPHooljUdp2+QldbjaoQ/pZGX33wkkwK77Ac" +
                "ynCEelWUFgkAYKnZwQIDAQAB";
        String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANPeBwx9RhMcLJFD" +
                "q3z2hULVnBJuOPTjlVk48PKoWVx8krZkB3DerDUij8DjsGa0fDRc1LUOyW2ID8qB" +
                "2XZQp56Pvdq001xx9O5kTz0ZG03kWn3zdZrUWv2A8eiiWNR2nb5CV1uNqhD+lkZf" +
                "ffCSTArvsBzKcIR6VZQWCQBgqdnBAgMBAAECgYA2P9rUMtuXlxY+V/J4O/Nhaqrc" +
                "+UYyRnf+cZsYt0yyZt92PmS9XPcBYAkpHeQUWFCZY8+/ULr5BebtNpSFPB++/X+B" +
                "ztAdOS04fvffdXYo93logxS80r4mpPC0KpqhQbuDaQrYwyAn17+buvB/9kcuZUCC" +
                "CbH1nykOSLfrjLrkRQJBAPqL4MqH/7Y0EJ61a26WG13W+OKpxL+YPYGEMjZBkJXB" +
                "RQpFMzaqoO4hFy1ndE5XFwMPX0yiwyR9TDAxRzq6iJsCQQDYep4WoKtqgwqW1mwH" +
                "pQ8rS9ptVNSrlzxfjBWYfK9Q9gYnm0Kn3h7cjVtWFkIR10FyBapIJpUP/trXPXzR" +
                "kObTAkA/MpdSHvnmaL2ketiNfXmLsxT2f6IsPeeNyt2rh+BDlgunKotfh6yuRFSH" +
                "VGgm9prMX81HFGsqwhw8r8Fq0/BvAkBFUsbrYhpiqoIqmZHQxOfdqpXRKzhLlsvL" +
                "oTWNNmiCGbcQ2eR3k2b4o//aypfv1KntlKjaIBjeHXQBN3yQM8HnAkEAvUP7rWFf" +
                "oALVO8HSexrjXnq5KTZlwLBo8mbjNvepYhSSUzweWWAF01wb2RE6iEKVi/eAbdxf" +
                "y1z4W6gwd0LJEg==";

        if(StringUtils.isEmpty(myNodeName)){
            myNodeName = "";
        }
        myNodeName = myNodeName.toUpperCase();
        ClusterInfo clusterInfo = ClusterInfoImpl.of()
                .setPubKey(pubKey)
                .setPrivateKey(privateKey)
                .setMyNodeName(myNodeName)
                .setClusterNodeNames(new ArrayList<String>(){{add(myNodeName);}})
                .setFaultNodeNum(0);
        ValidConsensus validConsensus = new StringValidConsensus(clusterInfo, p2pConsensusClient,"D:/temp/1/2/3/testTheValidFile");
        log.info("{}", validConsensus);
        return validConsensus;
    }
}
