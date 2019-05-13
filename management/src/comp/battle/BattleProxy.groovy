package comp.battle

import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.buding.common.result.Result
import com.buding.hall.module.user.dao.UserDao
import com.buding.hall.module.ws.BattlePortalBroadcastService
import org.springframework.stereotype.Component


@Component
class BattleProxy {
    BattlePortalBroadcastService service = null;
    UserDao userDao = null;


    public void init() {
        if(service != null) {
            return;
        }
        ApplicationConfig app = new ApplicationConfig()
        app.setName("BattleServerConsumer")
        RegistryConfig registry = new RegistryConfig()
        registry.setAddress("zookeeper://127.0.0.1:2181")
        ReferenceConfig<BattlePortalBroadcastService> reference = new ReferenceConfig<BattlePortalBroadcastService>()
        reference.setApplication(app)
        reference.setRegistry(registry)
        reference.setInterface(BattlePortalBroadcastService.class)
//        reference.setVersion("1.0.0")


        service = reference.get();
    }

    public Result replay(int playerId, String gameData) {
        init();
        return service.setDeskRelayData("all", playerId, gameData)
    }
}
