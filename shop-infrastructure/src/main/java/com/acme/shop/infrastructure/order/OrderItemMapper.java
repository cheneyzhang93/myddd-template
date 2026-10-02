package com.acme.shop.infrastructure.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 订单行表映射。 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemPO> {
}
