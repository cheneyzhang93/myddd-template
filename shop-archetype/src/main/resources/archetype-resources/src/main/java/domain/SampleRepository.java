package ${package}.domain;

import java.util.Optional;

/** 样本仓储端口。实现在 infrastructure 层，本层不知道它用 JDBC 还是内存。 */
public interface SampleRepository {

    void save(Sample sample);

    Optional<Sample> findById(long id);
}
