package com.acme.shop.api.auth;

import com.acme.shop.contract.auth.vo.AuthUserVO;
import org.deep361.myddd.kernel.port.auth.AuthUser;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** 鉴权端口的用户快照 → 契约出参。 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuthVoMapper {

    AuthUserVO toVo(AuthUser user);
}
