package com.acme.shop.infrastructure.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import org.deep361.myddd.persistence.mybatis.AbstractPO;

/**
 * 订单持久化对象。
 *
 * <p>四个审计字段（创建/更新时间和创建/更新人）来自 {@link AbstractPO}，由框架的
 * {@code AuditMetaObjectHandler} 自动填充，业务一行不写；PO 只允许出现在 infrastructure 层（架构规则 6b）。
 *
 * <p>{@code @Version} 是逐表决策：订单要并发安全，日志类表就不该有。
 */
@TableName(value = "shop_order", autoResultMap = true)
public class OrderPO extends AbstractPO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("status")
    private String status;

    @TableField("total_cents")
    private Long totalCents;

    @Version
    @TableField("version")
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotalCents() {
        return totalCents;
    }

    public void setTotalCents(Long totalCents) {
        this.totalCents = totalCents;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
