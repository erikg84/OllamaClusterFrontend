package di

import com.fasterxml.jackson.module.kotlin.KotlinModule
import data.service.LLMApiService
import data.service.LLMApiServiceImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.jackson.*
import mu.KotlinLogging
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import repository.*
import usecase.*
import viewmodel.AdminViewModel
import viewmodel.DashboardViewModel
import viewmodel.InteractViewModel
import viewmodel.MetricsViewModel

private val logger = KotlinLogging.logger {}

val networkModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson {
                    registerModule(KotlinModule.Builder().build())
                }
            }
            engine {
                requestTimeout = 120_000  // Increase to 120 seconds
                maxConnectionsCount = 10_000
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 120_000
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        KotlinLogging.logger("HttpClient").debug(message)
                    }
                }
                level = LogLevel.BODY
            }
            defaultRequest {
                url("http://localhost:3001/")
            }
        }
    }
}

val serviceModule = module {
    single<LLMApiService> { LLMApiServiceImpl(get()) }
}

val repositoryModule = module {
    single<NodeRepository> { NodeRepositoryImpl(get()) }
    single<ModelRepository> { ModelRepositoryImpl(get()) }
    single<QueueRepository> { QueueRepositoryImpl(get()) }
    single<ClusterRepository> { ClusterRepositoryImpl(get()) }
    single<AdminRepository> { AdminRepositoryImpl(get()) }
    single<LLMInteractionRepository> { LLMInteractionRepositoryImpl(get()) }
}

val useCaseModule = module {
    // Node use cases
    factoryOf(::GetAllNodesUseCaseImpl) bind GetAllNodesUseCase::class
    factoryOf(::GetNodeStatusUseCaseImpl) bind GetNodeStatusUseCase::class
    factoryOf(::GetNodeByIdUseCaseImpl) bind GetNodeByIdUseCase::class

    // Model use cases
    factoryOf(::GetAllModelsUseCaseImpl) bind GetAllModelsUseCase::class
    factoryOf(::GetModelsByNodeUseCaseImpl) bind GetModelsByNodeUseCase::class
    factoryOf(::GetModelByIdUseCaseImpl) bind GetModelByIdUseCase::class

    // Queue use cases
    factoryOf(::GetQueueStatusUseCaseImpl) bind GetQueueStatusUseCase::class
    factoryOf(::PauseQueueUseCaseImpl) bind PauseQueueUseCase::class
    factoryOf(::ResumeQueueUseCaseImpl) bind ResumeQueueUseCase::class

    // Cluster use cases
    factoryOf(::GetClusterStatusUseCaseImpl) bind GetClusterStatusUseCase::class

    // Admin use cases
    factoryOf(::GetSystemInfoUseCaseImpl) bind GetSystemInfoUseCase::class
    factoryOf(::GetSystemMetricsUseCaseImpl) bind GetSystemMetricsUseCase::class
    factoryOf(::ResetStatsUseCaseImpl) bind ResetStatsUseCase::class

    // LLM Interaction use cases
    factoryOf(::ChatWithLLMUseCaseImpl) bind ChatWithLLMUseCase::class
    factoryOf(::GenerateTextUseCaseImpl) bind GenerateTextUseCase::class
}

val viewModelModule = module {
    factory { DashboardViewModel(get(), get(), get(), get(), get(), get()) }
    factory { InteractViewModel(get(), get(), get(), get()) }
    factory { MetricsViewModel(get(), get(), get()) }
    factory { AdminViewModel(get(), get(), get()) }
}

val appModule = module {
    includes(networkModule, serviceModule, repositoryModule, useCaseModule, viewModelModule)
}
