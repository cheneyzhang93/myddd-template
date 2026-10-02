package com.acme.shop.contract.order.vo;

/** 订单分页查询对象：QO 承载"怎么查"，不承载业务状态。 */
public record OrderQueryQO(long pageNum, long pageSize) {

    public OrderQueryQO {
        if (pageNum <= 0) {
            pageNum = 1L;
        }
        if (pageSize <= 0) {
            pageSize = 10L;
        }
    }
}
