package com.acme.shop.api.order;

import com.acme.shop.application.order.CreateOrderDTO;
import com.acme.shop.application.order.OrderAppService;
import com.acme.shop.application.order.OrderViewDTO;
import com.acme.shop.contract.order.OrderApi;
import com.acme.shop.contract.order.vo.OrderCreateForm;
import com.acme.shop.contract.order.vo.OrderQueryQO;
import com.acme.shop.contract.order.vo.OrderVO;
import jakarta.validation.Valid;
import org.deep361.myddd.kernel.common.page.PageResult;
import org.deep361.myddd.kernel.common.response.Response;
import org.deep361.myddd.kernel.port.auth.RequiresPermission;
import org.deep361.myddd.kernel.port.consistency.Idempotent;
import org.deep361.myddd.kernel.port.resilience.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单 Controller：只做协议转换（HTTP ⇄ 用例），不写业务规则。
 *
 * <p>三个能力注解都来自内核端口，不含厂商类型：换成 security / resilience4j provider，本类一行不改。
 * 注解必须打在实现方法上而不是契约接口上——CGLIB 代理读不到接口上的注解。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController implements OrderApi {

    private final OrderAppService orders;
    private final OrderVoMapper mapper;

    public OrderController(OrderAppService orders, OrderVoMapper mapper) {
        this.orders = orders;
        this.mapper = mapper;
    }

    @Override
    @PostMapping
    @RequiresPermission("shop:order:create")
    @Idempotent(key = "#form.clientToken")
    @RateLimit(permits = 50)
    public Response<OrderVO> create(@Valid @RequestBody OrderCreateForm form) {
        List<CreateOrderDTO.CreateLineDTO> lines = form.lines().stream()
                .map(line -> new CreateOrderDTO.CreateLineDTO(line.skuId(), line.quantity(),
                        line.unitPriceCents()))
                .toList();
        return Response.ok(this.mapper.toVo(this.orders.create(
                new CreateOrderDTO(form.buyerId(), lines, form.clientToken()))));
    }

    @Override
    @PostMapping("/{orderId}/pay")
    @RequiresPermission("shop:order:pay")
    public Response<OrderVO> pay(@PathVariable long orderId) {
        return Response.ok(this.mapper.toVo(this.orders.pay(orderId)));
    }

    @Override
    @GetMapping("/{orderId}")
    @RequiresPermission("shop:order:query")
    public Response<OrderVO> detail(@PathVariable long orderId) {
        return Response.ok(this.mapper.toVo(this.orders.find(orderId)));
    }

    @Override
    @GetMapping
    @RequiresPermission("shop:order:query")
    public Response<PageResult<OrderVO>> page(@ModelAttribute OrderQueryQO query) {
        PageResult<OrderViewDTO> page = this.orders.page(query.pageNum(), query.pageSize());
        return Response.ok(page.map(this.mapper::toVo));
    }
}
