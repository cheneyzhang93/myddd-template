package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口。基础包 {@code ${package}} 是三个地方的共同锚点：组件扫描、{@code @Domain} 的缺省
 * 扫描范围、以及 {@code ArchitectureGuardTest} 传给架构守卫的包名。换名字时这三处一起换。
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
