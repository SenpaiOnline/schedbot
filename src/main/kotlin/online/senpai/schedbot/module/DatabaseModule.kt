package online.senpai.schedbot.module

import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.h2.H2ConnectionOption
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import org.koin.core.module.Module
import org.koin.dsl.module

private const val H2_DATABASE_PATH = "./data/main"
private const val CONNECTION_POOL_INITIAL_SIZE = 5
private const val CONNECTION_POOL_RETRY_ATTEMPTS = 10

val databaseModule: Module = module {
    single<ConnectionPool> {
        val connectionFactory = H2ConnectionFactory(
            H2ConnectionConfiguration.builder()
                .file(H2_DATABASE_PATH)
                .property(H2ConnectionOption.AUTO_SERVER, "TRUE")
                .build()
        )
        ConnectionPool(
            ConnectionPoolConfiguration.builder()
                .connectionFactory(connectionFactory)
                .initialSize(CONNECTION_POOL_INITIAL_SIZE)
                .acquireRetry(CONNECTION_POOL_RETRY_ATTEMPTS)
                .name("h2-pool")
                .build()
        )
    }
}
