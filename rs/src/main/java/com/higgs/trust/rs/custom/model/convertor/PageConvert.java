package com.higgs.trust.rs.custom.model.convertor;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.custom.api.vo.Page;
import com.higgs.trust.slave.api.vo.PageVO;

/**
 * page convert
 * @author tangfashuang
 * @date 2018/05/13 22:17
 */
public class PageConvert {
    public static<T> Page<T> convertPageVOToPage(PageVO pageVO, Class<T> toClazz) {
        Page page = new Page();
        page.setPageNo(pageVO.getPageNo());
        page.setTotal(pageVO.getTotal());
        page.setPageSize(pageVO.getPageSize());
        page.setData(BeanConvertor.convertList(pageVO.getData(), toClazz));
        return page;
    }
}
