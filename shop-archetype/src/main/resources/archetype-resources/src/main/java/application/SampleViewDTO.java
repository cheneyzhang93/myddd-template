package ${package}.application;

import ${package}.domain.Sample;

/** 用例出参。{@code *DTO} 只允许出现在 application 层（架构规则 6c），与 contract 的 {@code *VO} 分开演进。 */
public record SampleViewDTO(long id, String name) {

    public static SampleViewDTO of(Sample sample) {
        return new SampleViewDTO(sample.getId(), sample.name());
    }
}
