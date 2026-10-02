package ${package}.contract.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 入参。校验注解开在这里，Controller 方法参数上写 {@code @Valid} 即可触发。 */
public record SampleCreateForm(@NotBlank @Size(max = 64) String name) {

    @Override
    public String toString() {
        return "SampleCreateForm[name=" + name + "]";
    }
}
