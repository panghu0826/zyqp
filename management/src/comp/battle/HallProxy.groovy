package comp.battle

import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.buding.hall.module.ws.BattlePortalBroadcastService
import com.buding.hall.module.ws.HallPortalService
import com.buding.hall.module.ws.MsgPortalService
import com.buding.hall.module.ws.TaskPortalService
import org.springframework.stereotype.Component

/**
 * Created by admin on 2017-03-11         .
 */
@Component
class HallProxy {
    HallPortalService hallPortalService;
    BattlePortalBroadcastService battleService;
    MsgPortalService msgServicePortal;
    TaskPortalService taskPortalService;

    public BattlePortalBroadcastService getBattleService(String zkHost) {
        if(battleService != null) {
            return battleService;
        }
        ApplicationConfig app = new ApplicationConfig()
        app.setName("BattleServerConsumer")
        RegistryConfig registry = new RegistryConfig()
        registry.setAddress("zookeeper://$zkHost:2181")
        ReferenceConfig<BattlePortalBroadcastService> reference = new ReferenceConfig<BattlePortalBroadcastService>()
        reference.setApplication(app)
        reference.setRegistry(registry)
        reference.setInterface(BattlePortalBroadcastService.class)
        battleService = reference.get();
        return battleService;
    }

    public HallPortalService getHallService(String zkHost) {
        if(hallPortalService != null) {
            return hallPortalService;
        }
        ApplicationConfig app = new ApplicationConfig()
        app.setName("BattleServerConsumer")
        RegistryConfig registry = new RegistryConfig()
        registry.setAddress("zookeeper://$zkHost:2181")
        ReferenceConfig<HallPortalService> reference = new ReferenceConfig<HallPortalService>()
        reference.setApplication(app)
        reference.setRegistry(registry)
        reference.setInterface(HallPortalService.class)
        hallPortalService = reference.get();
        return hallPortalService;
    }

    public TaskPortalService getTaskService(String zkHost) {
        if(taskPortalService != null) {
            return taskPortalService;
        }
        ApplicationConfig app = new ApplicationConfig()
        app.setName("TaskServerConsumer")
        RegistryConfig registry = new RegistryConfig()
        registry.setAddress("zookeeper://$zkHost:2181")
        ReferenceConfig<TaskPortalService> reference = new ReferenceConfig<TaskPortalService>()
        reference.setApplication(app)
        reference.setRegistry(registry)
        reference.setInterface(TaskPortalService.class)
        taskPortalService = reference.get();
        return taskPortalService;
    }

    public MsgPortalService getMsgServicePortal(String zkHost) {
        if(msgServicePortal != null) {
            return msgServicePortal;
        }
        ApplicationConfig app = new ApplicationConfig()
        app.setName("BattleServerConsumer")
        RegistryConfig registry = new RegistryConfig()
        registry.setAddress("zookeeper://$zkHost:2181")
        ReferenceConfig<MsgPortalService> reference = new ReferenceConfig<MsgPortalService>()
        reference.setApplication(app)
        reference.setRegistry(registry)
        reference.setInterface(MsgPortalService.class)
        msgServicePortal = reference.get();
        return msgServicePortal;
    }

    public BattlePortalBroadcastService getDirectBattleService(String host) {
        ApplicationConfig app = new ApplicationConfig()
        app.setName("BattleServerConsumer")
        ReferenceConfig<BattlePortalBroadcastService> reference = new ReferenceConfig<BattlePortalBroadcastService>()
        reference.setApplication(app)
        reference.setInterface(BattlePortalBroadcastService.class)
        reference.setUrl(host)
        return reference.get();
    }

    public HallPortalService getDirectHallService(String host) {
        ApplicationConfig app = new ApplicationConfig()
        app.setName("HallServerConsumer")
        ReferenceConfig<HallPortalService> reference = new ReferenceConfig<HallPortalService>()
        reference.setApplication(app)
        reference.setUrl(host)
        reference.setInterface(HallPortalService.class)
        return reference.get();
    }
}
