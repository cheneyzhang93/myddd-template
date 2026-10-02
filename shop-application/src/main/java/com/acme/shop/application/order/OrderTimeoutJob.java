package com.acme.shop.application.order;

import com.acme.shop.domain.order.Order;
import com.acme.shop.domain.order.OrderRepository;
import org.deep361.myddd.kernel.port.job.JobContext;
import org.deep361.myddd.kernel.port.job.JobHandler;
import org.deep361.myddd.kernel.port.job.MydddJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 定时关单：业务代码只写"做什么"，"什么时候跑、在哪个调度平台上跑"由适配器决定。
 *
 * <p>{@code myddd.job.provider=spring} 时按 {@link MydddJob#cron()} 本地调度；换成 xxljob 后
 * 本类一行不改，调度周期改由 XXL-Job 控制台配置。
 *
 * <p>参数（超时分钟数）从 {@link JobContext#param()} 传入，缺省 30 分钟——这样"临时补跑一次两小时窗口"
 * 不需要发版。
 */
@Component
@MydddJob(name = "shopOrderTimeoutClose", cron = "0 0 3 * * ?", description = "关闭超时未支付订单")
public class OrderTimeoutJob implements JobHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutJob.class);

    static final int BATCH_SIZE = 200;
    static final long DEFAULT_TIMEOUT_MINUTES = 30L;

    private final OrderRepository orders;

    public OrderTimeoutJob(OrderRepository orders) {
        this.orders = orders;
    }

    @Override
    @Transactional
    public void execute(JobContext context) {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(parseTimeoutMinutes(context.param())));
        List<Order> timedOut = this.orders.findUnpaidCreatedBefore(cutoff, BATCH_SIZE);
        for (Order order : timedOut) {
            order.cancel();
            this.orders.save(order);
        }
        log.info("任务 {} 关单 {} 笔（窗口起点 {}）", context.jobName(), timedOut.size(), cutoff);
    }

    private static long parseTimeoutMinutes(String param) {
        if (param == null || param.isBlank()) {
            return DEFAULT_TIMEOUT_MINUTES;
        }
        try {
            long minutes = Long.parseLong(param.trim());
            return minutes > 0 ? minutes : DEFAULT_TIMEOUT_MINUTES;
        } catch (NumberFormatException e) {
            log.warn("任务参数 {} 不是数字，按缺省 {} 分钟处理", param, DEFAULT_TIMEOUT_MINUTES);
            return DEFAULT_TIMEOUT_MINUTES;
        }
    }
}
