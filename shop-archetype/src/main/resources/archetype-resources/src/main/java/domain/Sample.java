package ${package}.domain;

import java.util.Objects;

import org.deep361.myddd.kernel.ddd.model.AggregateRoot;

/**
 * 示例聚合根。三条约定就够了：只有两个构造入口（{@link #create} 建、{@link #reconstitute} 从存储重建）、
 * 状态变更只走方法、事件用 {@code registerEvent} 登记而不是自己发。
 *
 * <p>本层零框架依赖（架构规则 1）：没有 Spring、没有 MyBatis、没有 Jackson，所以领域测试是纯 Java
 * 测试，起不起得来容器与它无关。
 */
public class Sample extends AggregateRoot<Long> {

    private String name;

    private Sample(Long id, String name) {
        super(id);
        this.name = name;
    }

    public static Sample create(Long id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        Sample sample = new Sample(Objects.requireNonNull(id, "id"), name.trim());
        sample.registerEvent(new SampleCreatedEvent(sample.getId(), sample.name));
        return sample;
    }

    /** 从存储重建：不登记事件，重建不是业务发生。 */
    public static Sample reconstitute(Long id, String name) {
        return new Sample(Objects.requireNonNull(id, "id"), name);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        this.name = newName.trim();
    }

    public String name() {
        return name;
    }
}
