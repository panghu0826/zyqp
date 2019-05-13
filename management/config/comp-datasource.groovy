import com.guosen.webx.web.ServerJetty
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy

String dsPrd = '''
com.mysql.jdbc.Driver
jdbc:mysql://192.168.0.14:3306/zyqp?useUnicode=true&characterEncoding=UTF-8&autoReconnectForPools=true&allowMultiQueries=true&connectTimeout=3000
root
admin

initialPoolSize=10
maxPoolSize=100
maxIdleTime=30
'''

String dsLocal = '''
com.mysql.jdbc.Driver
jdbc:mysql://192.168.0.14:3306/zyqp?useUnicode=true&characterEncoding=UTF-8&autoReconnectForPools=true&allowMultiQueries=true&connectTimeout=3000
root
admin

initialPoolSize=2
maxPoolSize=5
maxIdleTime=30
'''


def getConf = { str ->
    def p = [:]

    def ll = str.readLines().collect { it.trim() }.grep { it }
    p.driver = ll[0]
    p.url = ll[1]
    p.u = ll[2]
    p.p = ll[3]

    if (ll.size() > 4) {
        for (one in ll[4..-1]) {
            String[] arr = one.split('=')
            p[arr[0]] = arr[1]
        }
    }

    p
}

def config = ServerJetty.config
println 'server config - ' + config
boolean isPrd = 'true' == config.isPrd
def conf = getConf(isPrd ? dsPrd : dsLocal)

beans {
    defaultDsTarget(ComboPooledDataSource) {
        it.destroyMethod = 'close'
        driverClass = conf.driver
        jdbcUrl = conf.url
        user = conf.u
        password = conf.p

        initialPoolSize = conf.initialPoolSize
        maxPoolSize = conf.maxPoolSize
        maxIdleTime = conf.maxIdleTime
    }

    defaultDs(TransactionAwareDataSourceProxy, defaultDsTarget)

    transactionManager(DataSourceTransactionManager) {
        dataSource = defaultDsTarget
    }

    sqlExecutor(groovy.sql.Sql, defaultDs)
    sqlDialect(com.guosen.webx.d.Dialect) {
        limitSupport = true
    }
    d(com.guosen.webx.d.D) {
        db = sqlExecutor
        dialect = sqlDialect
    }
}