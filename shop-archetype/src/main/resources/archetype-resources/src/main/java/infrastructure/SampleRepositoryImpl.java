package ${package}.infrastructure;

import ${package}.domain.Sample;
import ${package}.domain.SampleRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存实现，用来让骨架在没有数据源的情况下也能起来、能测。
 *
 * <p>接真实库时把这里换成 MyBatis-Plus 仓储（{@code shop-infrastructure} 的
 * {@code MybatisOrderRepository} 是完整样板：PO + {@code @Version} 乐观锁 + 聚合重建），
 * 并在 bootstrap 侧引 {@code myddd-persistence-starter}。存储细节只准出现在这一层。
 */
@Repository
public class SampleRepositoryImpl implements SampleRepository {

    private final Map<Long, Sample> store = new ConcurrentHashMap<>();

    @Override
    public void save(Sample sample) {
        Objects.requireNonNull(sample, "sample");
        // 存快照而不是活对象：聚合是可变的，直接引用会把内存里的旧值一起改掉。
        this.store.put(sample.getId(), Sample.reconstitute(sample.getId(), sample.name()));
    }

    @Override
    public Optional<Sample> findById(long id) {
        return Optional.ofNullable(this.store.get(id));
    }
}
