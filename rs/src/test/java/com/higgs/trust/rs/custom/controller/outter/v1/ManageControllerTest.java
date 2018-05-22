package com.higgs.trust.rs.custom.controller.outter.v1;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.rs.custom.api.vo.manage.RegisterPolicyVO;
import com.higgs.trust.rs.custom.api.vo.manage.RegisterRsVO;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import org.assertj.core.util.Lists;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author WangQuanzhou
 * @Description: TODO
 * @date ${date} ${time}
 */
public class ManageControllerTest {



    @Test public void testRegisterRs() throws Exception {
        String url = "http://localhost:7070/v1/manage/rs/register";
//        String url = "http://192.168.11.197:7070/v1/manage/rs/register";
        RegisterRsVO registerRsVO = new RegisterRsVO();
        registerRsVO.setRequestId("13");
        registerRsVO.setRsId("11r");
        registerRsVO.setPubKey("pubkkkk4444");
        registerRsVO.setDesc("2018年5月18日15:41:09");

        String params = JSON.toJSONString(registerRsVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url, params);

        System.out.println("res.data:" + res);
    }

    @Test public void testRegisterPolicy() throws Exception {
        String url = "http://localhost:7070/v1/manage/policy/register";
//        String url = "http://192.168.11.197:7070/v1/manage/policy/register";

        RegisterPolicyVO registerPolicyVO = new RegisterPolicyVO();
        registerPolicyVO.setRequestId(UUID.randomUUID().toString());
        registerPolicyVO.setPolicyId(UUID.randomUUID().toString().replaceAll("-",""));
        registerPolicyVO.setPolicyName(UUID.randomUUID().toString());
        List list = new ArrayList();
        list.add("TRUST-NODE-112");

        registerPolicyVO.setRsIds(list);

        String params = JSON.toJSONString(registerPolicyVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url, params);

        System.out.println("res.data:" + res);
    }
//rsList多个
    @Test public void testRegisterPolicy1() throws Exception {
        String url = "http://localhost:7070/v1/manage/policy/register";

        RegisterPolicyVO registerPolicyVO = new RegisterPolicyVO();
        registerPolicyVO.setRequestId(UUID.randomUUID().toString());
        registerPolicyVO.setPolicyId(UUID.randomUUID().toString().replaceAll("-",""));
        registerPolicyVO.setPolicyName(UUID.randomUUID().toString());
        List list = new ArrayList();
        list.add("TRUST-NODE-113");
        list.add("TRUST-NODE-114");
        registerPolicyVO.setRsIds(list);

        String params = JSON.toJSONString(registerPolicyVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url, params);

        System.out.println("res.data:" + res);
    }
//rsList为空
    @Test public void testRegisterPolicy2() throws Exception {
        String url = "http://localhost:7070/v1/manage/policy/register";

        RegisterPolicyVO registerPolicyVO = new RegisterPolicyVO();
        registerPolicyVO.setRequestId(UUID.randomUUID().toString());
        registerPolicyVO.setPolicyId(UUID.randomUUID().toString().replaceAll("-",""));
        registerPolicyVO.setPolicyName(UUID.randomUUID().toString());
        List list = new ArrayList();
        registerPolicyVO.setRsIds(list);
        String params = JSON.toJSONString(registerPolicyVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url, params);

        System.out.println("res.data:" + res);
    }
//policyID已存在
    @Test public void testRegisterPolicy3() throws Exception {
        String url = "http://localhost:7070/v1/manage/policy/register";

        RegisterPolicyVO registerPolicyVO = new RegisterPolicyVO();
        registerPolicyVO.setRequestId(UUID.randomUUID().toString());
        registerPolicyVO.setPolicyId("11");
        registerPolicyVO.setPolicyName(UUID.randomUUID().toString());
        List list = new ArrayList();
        list.add("TRUST-NODE-113");
        list.add("TRUST-NODE-114");
        registerPolicyVO.setRsIds(list);

        String params = JSON.toJSONString(registerPolicyVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url, params);
        System.out.println("res.data:" + res);
    }

    //policyID为空
    @Test public void testRegisterPolicy4() throws Exception {
        String url = "http://localhost:7070/v1/manage/policy/register";

        RegisterPolicyVO registerPolicyVO = new RegisterPolicyVO();
        registerPolicyVO.setRequestId(UUID.randomUUID().toString());
        registerPolicyVO.setPolicyId("");
        registerPolicyVO.setPolicyName(UUID.randomUUID().toString());
        List list = new ArrayList();
        list.add("TRUST-NODE-113");
        list.add("TRUST-NODE-114");
        registerPolicyVO.setRsIds(list);

        String params = JSON.toJSONString(registerPolicyVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url, params);

        System.out.println("res.data:" + res);
    }
}