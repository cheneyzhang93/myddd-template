package com.acme.shop.contract.points;

import com.acme.shop.contract.points.vo.PointsRedeemForm;
import com.acme.shop.contract.points.vo.PointsVO;
import org.deep361.myddd.kernel.common.response.Response;

/** 积分对外契约。 */
public interface PointsApi {

    /** 查当前登录用户的积分。 */
    Response<PointsVO> mine();

    /** 兑换积分。 */
    Response<PointsVO> redeem(PointsRedeemForm form);
}
