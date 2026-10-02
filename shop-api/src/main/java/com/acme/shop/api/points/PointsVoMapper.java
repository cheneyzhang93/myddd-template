package com.acme.shop.api.points;

import com.acme.shop.application.points.PointsViewDTO;
import com.acme.shop.contract.points.vo.PointsVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** 积分视图 → 契约出参。 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PointsVoMapper {

    PointsVO toVo(PointsViewDTO view);
}
