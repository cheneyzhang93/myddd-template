package com.acme.shop.infrastructure.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.deep361.myddd.persistence.mybatis.AbstractPO;

/** 订单行持久化对象（子表：与订单头分表，聚合内一致性由仓储实现在同一事务里保证）。 */
@TableName("shop_order_item")
public class OrderItemPO extends AbstractPO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("sku_id")
    private Long skuId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit_price_cents")
    private Long unitPriceCents;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getUnitPriceCents() {
        return unitPriceCents;
    }

    public void setUnitPriceCents(Long unitPriceCents) {
        this.unitPriceCents = unitPriceCents;
    }
}
