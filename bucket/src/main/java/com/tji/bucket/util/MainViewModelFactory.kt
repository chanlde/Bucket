package com.tji.bucket.util


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tji.bucket.data.repository.AuthRepository
import com.tji.bucket.data.repository.LinkRepository
import com.tji.bucket.data.repository.SwitchRepository
import com.tji.bucket.data.viewmodel.LinkViewModel
import com.tji.bucket.data.viewmodel.LoginViewModel
import com.tji.bucket.data.viewmodel.MainViewModel
import com.tji.bucket.data.viewmodel.SwitchViewModel
import com.tji.bucket.data.vminterface.LinkViewModelInterface
import com.tji.bucket.data.vminterface.LoginViewModelInterface
import com.tji.bucket.data.vminterface.SwitchViewModelInterface

/**
 * 主视图模型工厂，用于创建 MainViewModel 及其依赖的 ViewModel 实例。
 * 通过依赖注入提供 LinkViewModel、SwitchViewModel 和 LoginViewModel。
 * 使用接口解耦具体实现，便于测试和扩展。
 */
/**
 * 主视图模型工厂，用于创建 MainViewModel 及其依赖的 ViewModel 实例。
 * 通过依赖注入提供 LinkViewModel、SwitchViewModel 和 LoginViewModel。
 */
class MainViewModelFactory(
    private val linkRepository: LinkRepository,
    private val switchRepository: SwitchRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                val deviceViewModel = createDeviceViewModel()
                val switchControlViewModel = createSwitchControlViewModel()
                val loginViewModel = createLoginViewModel()
                @Suppress("UNCHECKED_CAST")
                MainViewModel(deviceViewModel, switchControlViewModel, loginViewModel) as T
            }
            modelClass.isAssignableFrom(LinkViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                createDeviceViewModel() as T
            }
            modelClass.isAssignableFrom(SwitchViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                createSwitchControlViewModel() as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                createLoginViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private fun createDeviceViewModel(): LinkViewModelInterface {
        return LinkViewModel(linkRepository)
    }

    private fun createSwitchControlViewModel(): SwitchViewModelInterface {
        return SwitchViewModel(switchRepository)
    }

    private fun createLoginViewModel(): LoginViewModelInterface {
        return LoginViewModel(authRepository)
    }
}