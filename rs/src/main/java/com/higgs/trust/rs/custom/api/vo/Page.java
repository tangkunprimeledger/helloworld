package com.higgs.trust.rs.custom.api.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author tangfashuang
 * @date 2018/05/13 22:35
 * @desc
 * @param <T>
 */
@Setter
@Getter
public class Page<T> extends BaseVO {

    private Long total;

    private Integer pageNo;

    private Integer pageSize;

    private List<T> data;
}
