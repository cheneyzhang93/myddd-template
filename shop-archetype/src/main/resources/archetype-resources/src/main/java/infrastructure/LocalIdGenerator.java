package ${package}.infrastructure;

import org.deep361.myddd.kernel.ddd.model.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 发号器：框架只给端口 {@code IdGenerator}，实现由工程自己选（数据库自增、号段、雪花都行）。
 * 这里的自增只保证单进程唯一，多实例部署必须换掉。
 */
@Component
public class LocalIdGenerator implements IdGenerator {

    private final AtomicLong sequence = new AtomicLong(1000L);

    @Override
    public long nextId() {
        return this.sequence.incrementAndGet();
    }
}
