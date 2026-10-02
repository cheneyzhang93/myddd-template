-- shop 样例的表结构。
-- 写法刻意保持"H2(MySQL 模式) 与 MySQL 8 都能直接执行"：不写 ENGINE/CHARSET/内联 COMMENT/内联 KEY，
-- 也不写 MySQL 8 不支持的 create index if not exists。
-- 真实项目请把这里的 DDL 复制进自己的 Flyway/Liquibase 脚本，改名后按自己的迁移流程执行，
-- 并按查询需要补二级索引（本样例数据量为一行，补索引只会增加噪声）。

create table if not exists shop_user (
    id bigint not null,
    username varchar(64) not null,
    password_hash varchar(100) not null,
    primary key (id),
    constraint uk_shop_user_username unique (username)
);

create table if not exists shop_user_role (
    id bigint not null,
    user_id bigint not null,
    role_code varchar(64) not null,
    primary key (id),
    constraint uk_shop_user_role unique (user_id, role_code)
);

create table if not exists shop_user_permission (
    id bigint not null,
    user_id bigint not null,
    permission_code varchar(64) not null,
    primary key (id),
    constraint uk_shop_user_permission unique (user_id, permission_code)
);

-- 订单头。四个审计列由框架的 AuditMetaObjectHandler 自动填充，version 由 @Version 乐观锁维护。
create table if not exists shop_order (
    id bigint not null,
    buyer_id bigint not null,
    status varchar(16) not null,
    total_cents bigint not null,
    version int not null default 0,
    create_time datetime(3),
    update_time datetime(3),
    creator varchar(64),
    updater varchar(64),
    primary key (id)
);

create table if not exists shop_order_item (
    id bigint not null,
    order_id bigint not null,
    sku_id bigint not null,
    quantity int not null,
    unit_price_cents bigint not null,
    create_time datetime(3),
    update_time datetime(3),
    creator varchar(64),
    updater varchar(64),
    primary key (id)
);

create table if not exists shop_points_account (
    id bigint not null,
    buyer_id bigint not null,
    balance bigint not null,
    version int not null default 0,
    create_time datetime(3),
    update_time datetime(3),
    creator varchar(64),
    updater varchar(64),
    primary key (id),
    constraint uk_shop_points_account_buyer_id unique (buyer_id)
);

-- 幂等占位表：复制自框架的参考 DDL（myddd-persistence-starter 的
-- db/ddl-reference/V2__myddd_idempotent_record.sql），去掉 MySQL 专有子句以保持双库可执行。
-- myddd.consistency.idempotent.store=db 时由 @Idempotent 使用；表名可用 myddd.consistency.idempotent.db.table 改。
create table if not exists myddd_idempotent_record (
    idem_key varchar(255) not null,
    token varchar(64) not null,
    created_at datetime(3) not null,
    expires_at datetime(3) not null,
    primary key (idem_key),
    constraint ck_myddd_idempotent_ttl check (expires_at > created_at)
);
