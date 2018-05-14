package com.higgs.trust.rs.custom.filter;

import cn.primeledger.pl.crypto.ECKey;
import cn.primeledger.pl.wallet.dock.util.AesUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.model.RespData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by liuyu on 17/12/14.
 * 参数解析过滤器
 */
@Component @WebFilter(urlPatterns = "/*", filterName = "parseParamFilter", asyncSupported = true)
public class ParseParamFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParseParamFilter.class);

    @Autowired private RsConfig rsConfig;
    /**
     * 放过的请求
     */
    private static final Set<String> PASS_PATHS = Collections.unmodifiableSet(new HashSet<>(Arrays
        .asList("/v1/blockchain/block/query", "/v1/blockchain/account/query", "/v1/blockchain/transaction/query",
            "/v1/blockchain/utxo/query")));

    /**
     * 读取流
     *
     * @param inStream
     * @return 字节数组
     **/
    public static String readStream(InputStream inStream) {
        ByteArrayOutputStream outSteam = null;
        try {
            outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            return new String(outSteam.toByteArray(), "UTF-8");
        } catch (Throwable e) {
            LOGGER.error("[readStream]has error", e);
        } finally {
            if (outSteam != null) {
                try {
                    outSteam.close();
                } catch (IOException e) {
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        response.setCharacterEncoding("UTF-8");
        String path = request.getRequestURI().substring(request.getContextPath().length()).replaceAll("[/]+$", "");
        //是否是不走拦截器的请求
        boolean isPassedPath = PASS_PATHS.contains(path);

        InputStream is = request.getInputStream();
        if (is == null) {
            LOGGER.info("[doFilter]inputStream is null");
            chain.doFilter(request, response);
            return;
        }
        String inputStr = readStream(is);
        LOGGER.info(inputStr);
        if (StringUtils.isEmpty(inputStr)) {
            LOGGER.info("[doFilter]inputStr is null");
            chain.doFilter(request, response);
            return;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(inputStr);
            if (!jsonObject.containsKey("requestParam")) {
                LOGGER.error("[doFilter] requestParam doesn't exist");
                response.getWriter().println(JSON.toJSONString(new RespData<>(RespCodeEnum.PARAM_NOT_VALID)));
                return;
            }
            if (!jsonObject.containsKey("signature") && !isPassedPath) {
                LOGGER.error("[doFilter] signature doesn't exist");
                response.getWriter().println(JSON.toJSONString(new RespData<>(RespCodeEnum.PARAM_NOT_VALID)));
                return;
            }
            String requestParam = jsonObject.getString("requestParam");
            String signature = jsonObject.getString("signature");

            String message = null;
            if (isPassedPath){
                message = requestParam;
            }else {
                message = AesUtil.decryptToString(requestParam, rsConfig.getAesKey());
                //验证签名
                if (!ECKey.verify(message, signature, rsConfig.getPubKey())) {
                    LOGGER.error("[doFilter] signature verification errors");
                    response.getWriter().println(JSON.toJSONString(new RespData<>(RespCodeEnum.SIGNATURE_VERIFY_FAIL)));
                    return;
                }
            }

            Map<String, Object> map = JSON.parseObject(message, Map.class);
            chain.doFilter(new RequestWrapper(request, map), res);
        } catch (Throwable e) {
            LOGGER.error("[doFilter] has error ", e);
        }
    }

    @Override public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override public void destroy() {
    }

    /**
     * request wrapper
     */
    public class RequestWrapper extends HttpServletRequestWrapper {
        private Map<String, Object> params = new HashMap<String, Object>();

        public RequestWrapper(HttpServletRequest request, Map<String, Object> addParams) {
            super(request);
            //把request原生的 请求参数put进去
            Map<String, String[]> map = request.getParameterMap();
            for (String key : map.keySet()) {
                String[] value = map.get(key);
                this.params.put(key, value[0]);
            }
            //put additional params
            if (addParams != null) {
                this.params.putAll(addParams);
            }
        }

        @Override public String getParameter(final String name) {
            Object value = this.params.get(name);
            if (value == null) {
                return null;
            } else {
                return String.valueOf(value);
            }
        }

        @Override public Map<String, String[]> getParameterMap() {
            Map<String, String[]> map = new HashMap<String, String[]>();
            for (String key : this.params.keySet()) {
                Object value = this.params.get(key);
                map.put(key, new String[] {String.valueOf(value)});
            }
            return map;
        }

        @Override public Enumeration<String> getParameterNames() {
            return Collections.enumeration(this.params.keySet());
        }

        @Override public String[] getParameterValues(String name) {
            Object value = this.params.get(name);
            if (value == null) {
                return null;
            } else {
                return new String[] {String.valueOf(value)};
            }
        }

    }

}
