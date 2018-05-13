package com.higgs.trust.rs.custom.api.vo;

import java.util.List;

/*
 * @desc 通过地址进行交易查询 分页Response对象
 * @author WangQuanzhou
 * @date 2018/2/11 10:36
 */  
public class TransAddrListVO  extends BaseVO{

    /**
     * 查询结果总条数
     */
    private Long totalCount;

    /**
     * 当前分页
     */
    private Long pageNo;

    /**
     * 分页数据
     */
    private List<TransAddrVO> paginationData;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public List<TransAddrVO> getPaginationData() {
        return paginationData;
    }

    public void setPaginationData(List<TransAddrVO> paginationData) {
        this.paginationData = paginationData;
    }
}
