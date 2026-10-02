package ${package};

import org.deep361.myddd.archunit.MydddArchitectureSpec;
import org.junit.jupiter.api.Test;

/**
 * 架构守卫：框架的 7 条红线（展开为 11 条规则）钉在本工程上，接线只有一行。
 *
 * <p>基础包写错的症状是"永远绿"——选择集为空，而规则一律开了 {@code allowEmptyShould}。
 * 所以改包名时这一行必须和 {@link Application} 所在的包一起改。
 */
class ArchitectureGuardTest {

    @Test
    void satisfiesMydddArchitecture() {
        MydddArchitectureSpec.verify("${package}");
    }
}
