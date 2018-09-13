package com.higgs.trust.presstest.controller;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.presstest.service.AccountService;
import com.higgs.trust.presstest.service.StoreService;
import com.higgs.trust.presstest.vo.*;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyu
 * @description
 * @date 2018-08-30
 */
@RequestMapping(value = "/press") @RestController @Slf4j public class PressController {
    @Autowired private AccountService accountService;
    @Autowired private StoreService storeService;

    /**
     * 创建币种
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/createCurrency") RespData createCurrency(@RequestBody CurrencyVO vo) {
        RespData respData = new RespData();
        Profiler.start("createCurrency is start");
        try {
            Profiler.enter("createCurrency");
            respData = accountService.createCurrency(vo);
        } catch (RsCoreException e) {
            log.error("[createCurrency]has error", e);
            respData.setCode(e.getCode().getCode());
            respData.setMsg(e.getCode().getDescription());
        } catch (Throwable t) {
            log.error("[createCurrency]has error", t);
            respData.setCode(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getDescription());
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
        return respData;
    }

    /**
     * 开户
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/openAccount") RespData openAccount(@RequestBody OpenAccountVO vo) {
        RespData respData = new RespData();
        Profiler.start("openAccount is start");
        try {
            Profiler.enter("openAccount");
            respData = accountService.openAccount(vo);
        } catch (RsCoreException e) {
            log.error("[openAccount]has error", e);
            respData.setCode(e.getCode().getCode());
            respData.setMsg(e.getCode().getDescription());
        } catch (Throwable t) {
            log.error("[openAccount]has error", t);
            respData.setCode(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getDescription());
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
        return respData;
    }

    /**
     * 账务操作
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/accounting") RespData accounting(@RequestBody AccountingVO vo) {
        RespData respData = new RespData();
        Profiler.start("accounting is start");
        try {
            Profiler.enter("accounting");
            respData = accountService.accounting(vo);
        } catch (RsCoreException e) {
            log.error("[accounting]has error", e);
            respData.setCode(e.getCode().getCode());
            respData.setMsg(e.getCode().getDescription());
        } catch (Throwable t) {
            log.error("[accounting]has error", t);
            respData.setCode(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getDescription());
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
        return respData;
    }

    /**
     * 冻结
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/freeze") RespData freeze(@RequestBody FreezeVO vo) {
        RespData respData = new RespData();
        Profiler.start("freeze is start");
        try {
            Profiler.enter("freeze");
            respData = accountService.freeze(vo);
        } catch (RsCoreException e) {
            log.error("[freeze]has error", e);
            respData.setCode(e.getCode().getCode());
            respData.setMsg(e.getCode().getDescription());
        } catch (Throwable t) {
            log.error("[freeze]has error", t);
            respData.setCode(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getDescription());
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
        return respData;
    }

    /**
     * 解冻
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/unfreeze") RespData unfreeze(@RequestBody UnFreezeVO vo) {
        RespData respData = new RespData();
        Profiler.start("unfreeze is start");
        try {
            Profiler.enter("unfreeze");
            respData = accountService.unfreeze(vo);
        } catch (RsCoreException e) {
            log.error("[unfreeze]has error", e);
            respData.setCode(e.getCode().getCode());
            respData.setMsg(e.getCode().getDescription());
        } catch (Throwable t) {
            log.error("[unfreeze]has error", t);
            respData.setCode(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getDescription());
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
        return respData;
    }

    /**
     * 存证
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/store") RespData store(@RequestBody StoreVO vo) {
        RespData respData = new RespData();
        Profiler.start("store is start");
        try {
            Profiler.enter("unfreeze");
            respData = storeService.store(vo);
        } catch (RsCoreException e) {
            log.error("[store]has error", e);
            respData.setCode(e.getCode().getCode());
            respData.setMsg(e.getCode().getDescription());
        } catch (Throwable t) {
            log.error("[store]has error", t);
            respData.setCode(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getCode());
            respData.setMsg(RsCoreErrorEnum.RS_CORE_UNKNOWN_EXCEPTION.getDescription());
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > Constant.PERF_LOG_THRESHOLD) {
                Profiler.logDump();
            }
        }
        return respData;
    }
}
