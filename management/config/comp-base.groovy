import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

beans {
    xmlns context: 'http://tech11.cn/schema/groovy-comp'
    context.'component-groovy'('base-package': 'comp')

    threadPool(ThreadPoolTaskExecutor) {
        corePoolSize = 20
        maxPoolSize = 50
        queueCapacity = 1000
    }
}