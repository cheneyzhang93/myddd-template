package com.acme.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端样例：登录 → 建单 → 支付 → 加积分，全程只走 HTTP。
 *
 * <p>这条用例覆盖的是脚手架最难自证的一段：自动装配是否真的把鉴权、幂等、限流、缓存、
 * 事件（AFTER_COMMIT）与持久化串起来。它只在 H2 上跑，不需要 Docker，也不需要任何中间件。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShopOrderFlowMockMvcTest {

    private static final String TOKEN_HEADER = "satoken";

    @Autowired
    private MockMvc mvc;

    private String login(String username, String password) throws Exception {
        MockHttpServletResponse response = this.mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andReturn().getResponse();
        String token = response.getHeader(TOKEN_HEADER);
        if (token == null && response.getCookie(TOKEN_HEADER) != null) {
            token = response.getCookie(TOKEN_HEADER).getValue();
        }
        assertThat(token)
                .as("登录响应里没有携带 token（header/cookie 均为空）：检查 sa-token 的 is-write-header 配置")
                .isNotBlank();
        return token;
    }

    private static String orderBody(long buyerId, String clientToken) {
        return "{\"buyerId\":" + buyerId + ",\"clientToken\":\"" + clientToken + "\",\"lines\":["
                + "{\"skuId\":101,\"quantity\":2,\"unitPriceCents\":1500}]}";
    }

    @Test
    @DisplayName("下单 → 支付 → 积分自动到账（AFTER_COMMIT 监听器）")
    void paidOrderGrantsPoints() throws Exception {
        String token = login("alice", "shop123456");

        ResultActions created = this.mvc.perform(post("/api/orders")
                        .header(TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(1L, uniqueToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalCents").value(3000));
        long orderId = Long.parseLong(extractOrderId(created));

        this.mvc.perform(post("/api/orders/" + orderId + "/pay").header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));

        // 3000 分 = 30 元 → 按 1 元 1 分给 30 分
        this.mvc.perform(get("/api/points/mine").header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(30));
    }

    @Test
    @DisplayName("同一个 clientToken 提交两次：第二次 409（DB 幂等占位表生效）")
    void duplicateClientTokenIsRejected() throws Exception {
        String token = login("alice", "shop123456");
        String clientToken = uniqueToken();

        this.mvc.perform(post("/api/orders")
                        .header(TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(1L, clientToken)))
                .andExpect(status().isOk());

        this.mvc.perform(post("/api/orders")
                        .header(TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(1L, clientToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MYDDD_40904"));
    }

    @Test
    @DisplayName("未登录：401，而不是 500")
    void anonymousIsUnauthorized() throws Exception {
        this.mvc.perform(get("/api/orders/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MYDDD_40101"));
    }

    @Test
    @DisplayName("已登录但没这个权限：403")
    void withoutPermissionIsForbidden() throws Exception {
        String token = login("bob", "shop123456");

        this.mvc.perform(post("/api/orders")
                        .header(TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody(2L, uniqueToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MYDDD_40301"));
    }

    @Test
    @DisplayName("参数校验失败：400，且不进业务")
    void invalidFormIsBadRequest() throws Exception {
        String token = login("alice", "shop123456");

        this.mvc.perform(post("/api/orders")
                        .header(TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"buyerId\":1,\"clientToken\":\"" + uniqueToken() + "\",\"lines\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("当前用户：角色与权限来自 DB，经端口返回")
    void currentUserCarriesPermissions() throws Exception {
        String token = login("alice", "shop123456");

        this.mvc.perform(get("/api/auth/current-user").header(TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.roles[*]").value(hasItem("shop:buyer")))
                .andExpect(jsonPath("$.data.permissions[*]").value(hasItem("shop:order:create")));
    }

    private static String uniqueToken() {
        return UUID.randomUUID().toString();
    }

    /** 从建单响应里取订单 id：不引入 ObjectReader，测试自己也不依赖 Jackson 的具体大版本。 */
    private static String extractOrderId(ResultActions actions) throws Exception {
        String json = actions.andReturn().getResponse().getContentAsString();
        int start = json.indexOf("\"orderId\":") + "\"orderId\":".length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return json.substring(start, end);
    }
}
