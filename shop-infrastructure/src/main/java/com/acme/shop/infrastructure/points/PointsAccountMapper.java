package com.acme.shop.infrastructure.points;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 积分账户表映射。 */
@Mapper
public interface PointsAccountMapper extends BaseMapper<PointsAccountPO> {
}
