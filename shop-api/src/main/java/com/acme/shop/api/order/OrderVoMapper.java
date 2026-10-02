package com.acme.shop.api.order;

import com.acme.shop.application.order.OrderViewDTO;
import com.acme.shop.contract.order.vo.OrderVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 应用层视图 → 对外契约：MapStruct 生成，编译期就报"目标字段没有来源"。
 *
 * <p>这里内联声明策略而不是复用框架的 {@code MydddMapperConfig}，是因为 api 层不该为了一个注解
 * 去依赖持久化 starter；已经依赖它的模块（如基础设施层）应当直接写
 * {@code @Mapper(config = MydddMapperConfig.class)}。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderVoMapper {

    OrderVO toVo(OrderViewDTO view);

    OrderVO.OrderLineVO toVo(OrderViewDTO.LineViewDTO line);
}
