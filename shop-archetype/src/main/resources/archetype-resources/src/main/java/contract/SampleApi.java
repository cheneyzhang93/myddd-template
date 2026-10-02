package ${package}.contract;

import ${package}.contract.vo.SampleCreateForm;
import ${package}.contract.vo.SampleVO;
import org.deep361.myddd.kernel.common.response.Response;

/**
 * 对外契约。Controller 必须实现本接口（架构规则 7），于是一致性、限流、鉴权这些注解只出现在
 * 实现类的对应方法上，接口本身不携带任何框架注解。
 */
public interface SampleApi {

    Response<SampleVO> create(SampleCreateForm form);

    Response<SampleVO> get(long id);
}
