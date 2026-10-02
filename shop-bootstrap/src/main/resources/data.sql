-- 演示数据。两个账号的口令都是 shop123456（BCrypt, strength=10）。
-- 全部用 "insert ... select ... from dual where not exists" 写成幂等：
-- 同一进程内起第二个 Spring 上下文时，data.sql 会再跑一遍，普通 insert 会撞主键。
insert into shop_user (id, username, password_hash)
select 1, 'alice', '$2a$10$76XvrO5WbUCAqpfZIhJ/Uer5MBkhZiY0ny4FI0T/ecaZPheJ2xFUK' from dual
where not exists (select 1 from shop_user where id = 1);

insert into shop_user (id, username, password_hash)
select 2, 'bob', '$2a$10$76XvrO5WbUCAqpfZIhJ/Uer5MBkhZiY0ny4FI0T/ecaZPheJ2xFUK' from dual
where not exists (select 1 from shop_user where id = 2);

-- alice：买家 + 全部下单/支付/查询/兑换权限
insert into shop_user_role (id, user_id, role_code)
select 1, 1, 'shop:buyer' from dual
where not exists (select 1 from shop_user_role where id = 1);

insert into shop_user_permission (id, user_id, permission_code)
select 1, 1, 'shop:order:create' from dual
where not exists (select 1 from shop_user_permission where id = 1);

insert into shop_user_permission (id, user_id, permission_code)
select 2, 1, 'shop:order:pay' from dual
where not exists (select 1 from shop_user_permission where id = 2);

insert into shop_user_permission (id, user_id, permission_code)
select 3, 1, 'shop:order:query' from dual
where not exists (select 1 from shop_user_permission where id = 3);

insert into shop_user_permission (id, user_id, permission_code)
select 4, 1, 'shop:points:redeem' from dual
where not exists (select 1 from shop_user_permission where id = 4);

-- bob：只能查，用来把 403 与 401 分开演示
insert into shop_user_role (id, user_id, role_code)
select 2, 2, 'shop:guest' from dual
where not exists (select 1 from shop_user_role where id = 5);

insert into shop_user_permission (id, user_id, permission_code)
select 5, 2, 'shop:order:query' from dual
where not exists (select 1 from shop_user_permission where id = 5);
