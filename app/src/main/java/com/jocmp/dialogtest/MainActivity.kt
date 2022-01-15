package com.jocmp.dialogtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jocmp.dialogtest.ui.theme.DialogTestTheme

const val homeRoute = "home"
const val dialogRoute = "dialog"
const val listRoute = "list"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DialogTestTheme {
                var showFullScreenDialog by rememberSaveable { mutableStateOf(false) }

                if (showFullScreenDialog)
                    FullScreenDialog(onDismissRequest = { showFullScreenDialog = false })

                App(onClick = { showFullScreenDialog = true })
            }
        }
    }
}

@Composable
fun App(onClick: () -> Unit) = Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color.Green
) {
    Box {
        Button(onClick = onClick) {
            Text("Open Full Screen Dialog")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FullScreenDialog(onDismissRequest: () -> Unit) = Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(usePlatformDefaultWidth = false)
) {
    var width by rememberSaveable { mutableStateOf(0) }
    var height by rememberSaveable { mutableStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned {
                width = it.size.width
                height = it.size.height
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Full Screen Dialog\nwidth:$width\nheight:$height",
            color = Color.White
        )
    }
}



@ExperimentalComposeUiApi
class MainActivityAgain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            val windowInsetsControllerCompat =
                remember { WindowInsetsControllerCompat(window, window.decorView) }
            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }
            val navController = rememberNavController()
            CompositionLocalProvider(
                LocalWindowInsetsController provides windowInsetsControllerCompat,
            ) {
                DialogTestTheme {
                    ProvideWindowInsets {
                        Scaffold { padding ->
                            NavHost(
                                navController = navController,
                                startDestination = listRoute,
                                modifier = Modifier.padding(padding)
                            ) {
                                composable(homeRoute) { HomeScreen(navController = navController) }
                                composable(listRoute) { ListScreen(navController = navController) }
                                dialog(dialogRoute, dialogProperties = DialogProperties()) {
                                    DialogScreen(
                                        navController = navController,
                                        systemUiController = systemUiController
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Button(onClick = { navController.navigate(dialogRoute) }) {
            Text("To Dialog")
        }
    }
}

@Composable
fun DialogScreen(navController: NavController, systemUiController: SystemUiController) {
   Scaffold { contentPadding ->
       Insets(
           control = InsetsControl(
               extendToTop = true,
               extendToBottom = true,
               extendToStart = true,
               extendToEnd = true,
           ),
           color = InsetsColor(
               start = Color.Cyan,
               end = Color.Red
           )
       ) {
           Box(
               contentAlignment = Alignment.Center,
           ) {
               Box(Modifier
                   .background(Color.Black)
                   .fillMaxSize()
                   .padding(contentPadding)
               )
               Image(
                   painterResource(id = R.drawable.jacob_van_ruisdael),
                   contentDescription = null,
                   contentScale = ContentScale.Fit,
                   modifier = Modifier.clickable { navController.popBackStack() }
               )
           }
       }
   }
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Black, darkIcons = true)
    }
    DisposableEffect(Unit) {
        onDispose {
            systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = true)
        }
    }
}


@Composable
private fun ListScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Whatever") },
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.95f),
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            // We add a spacer as a bottom bar, which is the same height as
            // the navigation bar
            Spacer(Modifier
                .navigationBarsHeight()
                .fillMaxWidth())
        }
    ) { contentPadding ->
        Box {
            // We apply the contentPadding passed to us from the Scaffold
            LazyColumn(
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = listItems, key = { it }) { imageUrl ->
                    ListItem(imageUrl,
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(dialogRoute)
                            })
                }
            }
        }
    }
}

@Composable
fun ListItem(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Image(
            painter = rememberImagePainter(imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = "Text",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
    }
}

private val listItems = List(40) { randomSampleImageUrl(it) }

private val rangeForRandom = (0..100000)

fun randomSampleImageUrl(
    seed: Int = rangeForRandom.random(),
    width: Int = 300,
    height: Int = width,
): String {
    return "https://picsum.photos/seed/$seed/$width/$height"
}

/**
 * Remember a URL generate by [randomSampleImageUrl].
 */
@Composable
fun rememberRandomSampleImageUrl(
    seed: Int = rangeForRandom.random(),
    width: Int = 300,
    height: Int = width,
): String = remember { randomSampleImageUrl(seed, width, height) }
