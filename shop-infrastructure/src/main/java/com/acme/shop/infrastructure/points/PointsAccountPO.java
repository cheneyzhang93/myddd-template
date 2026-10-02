package com.acme.shop.infrastructure.points;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import org.deep361.myddd.persistence.mybatis.AbstractPO;

/** 积分账户持久化对象。 */
@TableName("shop_points_account")
public class PointsAccountPO extends AbstractPO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("balance")
    private Long balance;

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

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
