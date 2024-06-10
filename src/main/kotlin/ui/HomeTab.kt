package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class HomeTabs{

    object Code : Tab, HomeTabs() {
        override val options: TabOptions
            @Composable
            get() {
                val title = "代码片段解释器"
                val icon = rememberVectorPainter(Icons.Default.Build)
                return remember { TabOptions(index = 0u, title = title, icon = icon) }
            }

        @Composable
        override fun Content() {
            CodeCalculatorPage()
        }
    }
    object Single : Tab,HomeTabs() {
        override val options: TabOptions
            @Composable
            get() {
                val title = "Lambda表达式演算"
                val icon = rememberVectorPainter(Icons.Default.Edit)
                return remember { TabOptions(index = 1u, title = title, icon = icon) }
            }

        @Composable
        override fun Content() {
            SingleCalculatorPage()
        }

    }

    companion object{
        var scaffoldState:ScaffoldState? = null
        var scope:CoroutineScope? = null
        fun showInfo(info:String){
            scope?.launch {
                scaffoldState?.snackbarHostState?.showSnackbar(info)
            }
        }
    }
}


@Composable
fun HomeTabBar(){
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    HomeTabs.scope=scope
    HomeTabs.scaffoldState=scaffoldState

    TabNavigator(HomeTabs.Single) {
        Scaffold(
            content = {
                CurrentTab()
            },
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(it, snackbar = { st ->
                    Snackbar(
                        st, backgroundColor = SnackbarDefaults.backgroundColor,
                        contentColor = MaterialTheme.colors.onBackground,
                        actionColor = SnackbarDefaults.primaryActionColor,
                    )
                })
            },
            topBar = {
                TopAppBar {
                    TabNavigationItem(HomeTabs.Single)
                    TabNavigationItem(HomeTabs.Code)
                }
            }
        )
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    BottomNavigationItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
        label = { Text(tab.options.title) },
        modifier = Modifier.background(MaterialTheme.colors.surface)
    )
}