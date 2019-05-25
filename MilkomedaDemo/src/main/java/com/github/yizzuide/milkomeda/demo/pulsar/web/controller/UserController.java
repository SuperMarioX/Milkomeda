package com.github.yizzuide.milkomeda.demo.pulsar.web.controller;

import com.github.yizzuide.milkomeda.comet.Comet;
import com.github.yizzuide.milkomeda.demo.pulsar.pojo.User;
import com.github.yizzuide.milkomeda.pulsar.PulsarAsync;
import com.github.yizzuide.milkomeda.pulsar.PulsarDeferredResult;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.github.yizzuide.milkomeda.pulsar.PulsarHolder.async;

/**
 * UserController
 *
 * @author yizzuide
 * Create at 2019/03/30 19:17
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    // 使用默认的 WebAsyncTask 方式（实际应用场景可用于上传文件处理）
    @PulsarAsync
    @GetMapping("login")
    public Object /* TODO 这里的返回值必需为Object */ login(String username) throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName(username);
        // 查看当前线程：pulsar-1
        log.info("login user: {}", username);

        // 模拟耗时处理
//        Thread.sleep(2000);

        // 测试运行时异常
//        throw new YizException(user.getId(), "user", "用户不存在");
        // 测试普通异常
//        throw new IOException("file not find");
        return user;
    }

    // 使用灵活的DeferredResult异步处理，需要注入PulsarDeferredResult
    @PulsarAsync(useDeferredResult = true)
    @GetMapping("{id:\\d+}")
    public Object /* TODO 这里的返回值必需为Object */ getInfo(@PathVariable("id") Long id, PulsarDeferredResult pulsarDeferredResult) throws Exception {
        log.info("findById：" + id);

        // 设置当前DeferredResult唯一标识，用于另一线程（在QueueListener类）获取
        pulsarDeferredResult.setDeferredResultID(/*RandomStringUtils.random(8)*/"84735252737");

        // 测试运行时异常
//        throw new YizException(id, "user", "用户不存在");
        // 测试普通异常
//        throw new IOException();

        // 实际结果由DeferredResult返回，这里返回null即可
        return null;
    }


    @Comet
    // 使用DeferredResult异步处理在同一方法使用的例子
    @PulsarAsync(useDeferredResult = true)
    @GetMapping("notice/{id:\\d+}")
    public Object /* TODO 这里的返回值必需为Object */ sendNotice(@PathVariable("id") Long id,
                                                        PulsarDeferredResult pulsarDeferredResult) {
        log.info("sendNotice：" + id);

        log.info("pulsarHolder: {}", PulsarHolder.getPulsar());
        log.info("errorCallback: {}", PulsarHolder.getErrorCallback());

        // 同步测试（如果返回值不为 null，则采用同步执行）
//        return ResponseEntity.ok("OK");

        // 模拟当前方法中调用耗时数业务处理（这种方式不支持内部异常捕获，不推荐使用）
        /*PulsarHolder.getPulsar().asyncRun(() -> {
            log.info("execute business");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pulsarDeferredResult.take().setResult("send OK");
        });*/

        // async为PulsarHolder的静态方法（内部可自动捕获异常，推荐使用）
        return async(() -> {
//            throw new RuntimeException("测试抛出异常");

            // 这里的返回，内部会使用DeferredResult返回
            return ResponseEntity.ok( "{\"code\": \"0000\"}");
        }, pulsarDeferredResult);
    }
}