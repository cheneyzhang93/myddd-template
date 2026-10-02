# myddd-template

myddd 脚手架的**消费侧**样例工程：一个订单 + 积分业务，六层多模块，外加一个 `mvn archetype:generate` 用的骨架。
这里不重复框架文档，只回答一件事——业务工程接到 myddd 上到底长什么样、哪些键能换实现而不动业务代码。

## 前置

- JDK 21、Maven 3.9+（仓库带 wrapper，`./mvnw` 即可）
- 先把框架装进本地仓：在 myddd 框架仓根目录执行 `./mvnw -B -ntp clean install`
  （本仓只 `import` `org.deep361.myddd:myddd-dependencies` BOM，不继承 `myddd-parent`）

## 构建与运行

```bash
./mvnw -B -ntp clean install
```

**没有 Docker 也必须全绿**：唯一需要真库的 `ShopOrderFlowMySqlIT` 整类干净跳过，其余全部实跑
（单元 + MockMvc 端到端 + 架构守卫，都打在进程内 H2 上）。

跑起来：

```bash
./mvnw -B -ntp -pl shop-bootstrap spring-boot:run
```

```bash
# 登录：token 在响应头 satoken 里返回
curl -s -i -X POST localhost:8080/api/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"username":"alice","password":"shop123456"}'

# 带 token 下单（clientToken 是幂等键，重复提交第二次 409）
curl -s -X POST localhost:8080/api/orders \
     -H "Content-Type: application/json" -H "satoken: <上一步的 token>" \
     -d '{"buyerId":1,"clientToken":"t-1","lines":[{"skuId":9001,"quantity":2,"unitPriceCents":1500}]}'

curl -s -X POST localhost:8080/api/orders/1/pay -H "satoken: <token>"
curl -s localhost:8080/api/points/mine -H "satoken: <token>"
```

内置账号（`shop-bootstrap/src/main/resources/data.sql` 种子，跑在 H2 上）：`alice / shop123456`
有下单、支付、查、兑换全部权限；`bob / shop123456` 只有查询权限，用来演示 403。

## 分层与职责

| 模块 | 包 | 放什么 | 不放什么 |
| --- | --- | --- | --- |
| shop-contract | `contract` | 对外接口 + `*Form`/`*VO`/`*QO` | 任何框架注解、任何业务逻辑 |
| shop-domain | `domain` | 聚合、值对象、状态机、领域事件、仓储端口 | Spring / MyBatis / Jackson / JPA（规则 1） |
| shop-application | `application` | 用例编排、事务边界、`*DTO`、事件监听器、定时任务 | contract、infrastructure（规则 2、3） |
| shop-infrastructure | `infrastructure` | MyBatis-Plus 仓储实现、PO、转换器、发号器 | 业务规则 |
| shop-api | `api` | Controller 只做协议转换，一律实现 contract 接口（规则 7） | 业务判断、直连 infrastructure（规则 4） |
| shop-bootstrap | 根包 + `config`/`auth` | 启动类、yml、provider bean、架构守卫与端到端测试 | — |

包结构以根包为锚点：组件扫描、`@Domain` 缺省扫描范围、`MydddArchitectureSpec.verify("com.acme.shop")`
三处共用它，改名要一起改。

## provider 开关

换实现只改 yml 与依赖，业务代码一行不动。样例选的是**不需要任何中间件**的那一档：

| 键 | 样例取值 | 另一档 |
| --- | --- | --- |
| `myddd.auth.provider` | `satoken` | `security`（Spring Security） |
| `myddd.limiter.provider` | `sentinel` + `rule-source: memory` | `resilience4j` |
| `myddd.cache.type` | `caffeine` | `multi`（需要 Redis） |
| `myddd.consistency.idempotent.store` | `db`（占位表 `myddd_idempotent_record`） | `redis` |
| `myddd.consistency.lock.enabled` | `false`（锁要 Redisson） | `true` |
| `myddd.job.provider` | 只引 `myddd-job-spring-starter` ⇒ 用 Spring cron，不接调度中心 | `xxljob` |
| `myddd.domain.outbox.enabled` / `myddd.mq.enabled` | `false`（进程内事件） | `true` + 引 MQ starter |

鉴权侧业务必须自己给两个 bean：`CredentialVerifier`（校验账号）与 `StpInterface`（给角色/权限），
见 `shop-bootstrap/.../config/ShopAuthConfiguration.java`；发号器同理，框架只给端口
（`infrastructure/support/MpIdGenerator.java`）。

`@EnableCaching` 在消费侧声明。AOP 不用加依赖：Boot 4 里 `spring-boot-starter-aop` 已不存在（改名
`spring-boot-starter-aspectj`），而 persistence / consistency / resilience 三个 starter 都带
`aspectjweaver`，`org.aspectj.weaver.Advice` 在类路径上时 Boot 的 `AopAutoConfiguration` 自动开代理。

## 样例证明了什么

端到端测试 `ShopOrderFlowMockMvcTest` 逐条钉住：下单 → 支付 → 领域事件在事务提交后加积分（3000 分 → 30 分）；
重复 `clientToken` 得 `MYDDD_40904`；匿名请求得 `MYDDD_40101`；`bob` 下单得 `MYDDD_40301`；
空订单行得 `MYDDD_40001`。定时任务 `OrderTimeoutJob` 关超时未付单；`ShopArchitectureTest` 跑框架的 11 条架构规则。

## 本地环境（devbox）

副本；正本在框架仓 `myddd-devbox/`，镜像 tag 的真源是框架父 POM。唯一入口：

```bash
cp myddd-devbox/.env.example myddd-devbox/.env   # 填 MYSQL_ROOT_PASSWORD，.env 不入库
bash myddd-devbox/devbox.sh up                   # 默认 mysql + redis；档位 kafka rocketmq job limiter
bash myddd-devbox/devbox.sh ps | logs | down | reset --yes
```

起了 MySQL 后切真库跑（表结构由 `schema.sql` 提供，devbox 已建好 `shop_app` / `shop_app_test`）：

```bash
MYDDD_MYSQL_URL='jdbc:mysql://127.0.0.1:3306/shop_app?...' \
./mvnw -B -ntp -pl shop-bootstrap -Dspring-boot.run.profiles=mysql spring-boot:run
```

## 骨架（shop-archetype）

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=com.acme.shop -DarchetypeArtifactId=shop-archetype -DarchetypeVersion=1.0.0-SNAPSHOT \
  -DgroupId=com.acme.demo -DartifactId=demo -Dversion=1.0.0-SNAPSHOT -Dpackage=com.acme.demo \
  -DmydddVersion=1.0.0-SNAPSHOT -DspringBootVersion=4.1.1
```

生成的是**单模块五个分层包**的最小可跑骨架：`contract / domain / application / infrastructure / api`
+ 启动类 + `@Domain` 声明 + 一行架构守卫测试。仓储是内存实现（不需要数据源），接库时按
`shop-infrastructure` 的 MyBatis-Plus 样板替换。`archetype:integration-test` 会真实生成并构建一次这个骨架，
骨架编不过就是构建红。

## CI

`.github/workflows/ci.yml` 需要先给仓库配一个 Variable `MYDDD_FRAMEWORK_REPO`（框架仓的可克隆地址），
CI 先装框架再 `./mvnw -B -ntp clean install`，最后断言 failsafe 报告里确实跑过 IT 且没有 skip。
