package com.acme.shop.contract.order;

import com.acme.shop.contract.order.vo.OrderCreateForm;
import com.acme.shop.contract.order.vo.OrderQueryQO;
import com.acme.shop.contract.order.vo.OrderVO;
import org.deep361.myddd.kernel.common.page.PageResult;
import org.deep361.myddd.kernel.common.response.Response;

/**
 * 订单对外契约。
 *
 * <p>契约只有接口 + 传输对象，没有 Spring / MyBatis 依赖：Controller 实现它（架构规则 7），
 * 其他进程（Feign 客户端、文档生成、契约测试）也实现或消费它。
 */
public interface OrderApi {

    /** 建单。 */
    Response<OrderVO> create(OrderCreateForm form);

    /** 支付。 */
    Response<OrderVO> pay(long orderId);

    /** 查询单笔。 */
    Response<OrderVO> detail(long orderId);

    /** 分页查询。 */
    Response<PageResult<OrderVO>> page(OrderQueryQO query);
}
