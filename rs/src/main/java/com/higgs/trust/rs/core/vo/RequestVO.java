package com.higgs.trust.rs.core.vo;import com.higgs.trust.common.mybatis.BaseEntity;import lombok.Getter;import lombok.Setter;import java.util.Date;@Getter@Setterpublic class RequestVO extends BaseEntity<RequestVO> {	/**	 * request id	 */	private String requestId;	/**	 * request status	 */	private String status;	/**	 *  response code	 */	private String respCode;	/**	 * response msg	 */	private String respMsg;	/**	 * the create time	 */	private Date createTime;	/**	 * the update time	 */	private Date updateTime;}