package com.acme.shop.api.points;

import com.acme.shop.application.points.PointsAppService;
import com.acme.shop.contract.points.PointsApi;
import com.acme.shop.contract.points.vo.PointsRedeemForm;
import com.acme.shop.contract.points.vo.PointsVO;
import jakarta.validation.Valid;
import org.deep361.myddd.kernel.common.error.BizException;
import org.deep361.myddd.kernel.common.error.CommonErrorCode;
import org.deep361.myddd.kernel.common.response.Response;
import org.deep361.myddd.kernel.port.auth.AuthPort;
import org.deep361.myddd.kernel.port.auth.RequiresPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分 Controller。
 *
 * <p>{@code mine()} 从端口取当前用户，不读 HTTP 头、不碰 Sa-Token 的 API——这就是"换 provider 业务零改动"
 * 在 Controller 侧的落点。
 */
@RestController
@RequestMapping("/api/points")
public class PointsController implements PointsApi {

    private final PointsAppService points;
    private final AuthPort auth;
    private final PointsVoMapper mapper;

    public PointsController(PointsAppService points, AuthPort auth, PointsVoMapper mapper) {
        this.points = points;
        this.auth = auth;
        this.mapper = mapper;
    }

    @Override
    @GetMapping("/mine")
    public Response<PointsVO> mine() {
        return Response.ok(this.mapper.toVo(this.points.findByBuyer(currentBuyerId())));
    }

    @Override
    @PostMapping("/redeem")
    @RequiresPermission("shop:points:redeem")
    public Response<PointsVO> redeem(@Valid @RequestBody PointsRedeemForm form) {
        long currentBuyerId = currentBuyerId();
        if (form.buyerId() != currentBuyerId) {
            throw new BizException(CommonErrorCode.FORBIDDEN);
        }
        return Response.ok(this.mapper.toVo(this.points.redeem(currentBuyerId, form.points())));
    }

    private long currentBuyerId() {
        return this.auth.currentUser()
                .map(user -> Long.parseLong(user.userId()))
                .orElseThrow(() -> new BizException(CommonErrorCode.UNAUTHORIZED));
    }
}
