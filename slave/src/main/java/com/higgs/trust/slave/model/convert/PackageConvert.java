package com.higgs.trust.slave.model.convert;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.util.DeflateUtil;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.model.bo.Package;

/**
 * @author tangfashuang
 * @date 2018/04/11 19:57
 * @desc package convert util
 */
public class PackageConvert {
    public static Package convertPackVOToPack(PackageVO packageVO) {
        Package pack = new Package();
        pack.setPackageTime(packageVO.getPackageTime());
        pack.setHeight(packageVO.getHeight());
        pack.setSignedTxList(packageVO.getSignedTxList());
        return pack;
    }

    public static PackageVO convertPackToPackVO(Package pack) {
        PackageVO packVO = new PackageVO();
        packVO.setPackageTime(pack.getPackageTime());
        packVO.setHeight(pack.getHeight());
        packVO.setSignedTxList(pack.getSignedTxList());
        return packVO;
    }

    public static byte[] convertPackToPackVOToBytes(Package pack){
        String result = JSON.toJSONString(pack);
        return DeflateUtil.compress(result.getBytes());
    }
}
