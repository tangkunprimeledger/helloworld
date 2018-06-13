package com.higgs.trust.slave.core.managment.master;

import com.higgs.trust.slave.core.managment.listener.MasterChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 * @date 2018/06/13 17:41
 * @desc set packHeight=null when master change
 */
@Service
public class MasterPackageService implements MasterChangeListener{
    @Autowired Long packHeight;

    @Override public void masterChanged(String masterName) {
        packHeight = null;
    }

}
