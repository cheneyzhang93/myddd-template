package ${package}.api;

import ${package}.application.SampleAppService;
import ${package}.application.SampleViewDTO;
import ${package}.contract.SampleApi;
import ${package}.contract.vo.SampleCreateForm;
import ${package}.contract.vo.SampleVO;
import jakarta.validation.Valid;
import org.deep361.myddd.kernel.common.response.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 协议转换层：实现 contract 接口（架构规则 7），只做 DTO ↔ VO 的映射，不写业务判断。
 *
 * <p>注意注解位置：{@code @RequiresPermission}/{@code @Idempotent}/{@code @RateLimit} 这类切面注解
 * 一律写在实现方法上而不是接口方法上——CGLIB 代理的是实现类，接口上的注解不会被读到。
 */
@RestController
@RequestMapping("/api/samples")
public class SampleController implements SampleApi {

    private final SampleAppService samples;

    public SampleController(SampleAppService samples) {
        this.samples = samples;
    }

    @Override
    @PostMapping
    public Response<SampleVO> create(@Valid @RequestBody SampleCreateForm form) {
        return Response.ok(toVo(this.samples.create(form.name())));
    }

    @Override
    @GetMapping("/{id}")
    public Response<SampleVO> get(@PathVariable long id) {
        return Response.ok(toVo(this.samples.get(id)));
    }

    private SampleVO toVo(SampleViewDTO dto) {
        return new SampleVO(dto.id(), dto.name());
    }
}
