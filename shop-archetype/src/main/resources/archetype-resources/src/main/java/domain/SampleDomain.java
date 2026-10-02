package ${package}.domain;

import org.deep361.myddd.kernel.ddd.context.Domain;

/**
 * 领域模块声明。{@code scanPackages} 必须把应用层监听器所在的包一起列上：框架在启动期校验每个
 * {@code @DomainEventListener} bean 的包被某个模块覆盖，违约是启动失败而不是运行期静默不触发。
 */
@Domain(value = "sample", scanPackages = {"${package}.domain", "${package}.application"})
public final class SampleDomain {

    private SampleDomain() {
    }
}
