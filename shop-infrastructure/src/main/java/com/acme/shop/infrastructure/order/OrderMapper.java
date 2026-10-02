package com.acme.shop.infrastructure.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 订单头表映射：MP 的 BaseMapper 提供 CRUD，业务查询在仓储实现里用 Wrapper 表达。 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderPO> {
}
