package com.example.taller_dos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.taller_dos.model.Post
import com.example.taller_dos.network.RetrofitInstance
import com.example.taller_dos.ui.theme.components.CrudResultsStyled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var crudLog by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                // 1) GET all (primeros 5)
                val all = try {
                    withContext(Dispatchers.IO) { RetrofitInstance.api.getPosts() }
                } catch (e: Exception) {
                    crudLog += "GET all Error: ${e.message}\n\n"
                    return@LaunchedEffect
                }
                crudLog += "GET all → ${all.size} posts:\n"
                all.take(5).forEach { crudLog += "[${it.id}] ${it.title}\n" }
                crudLog += "\n"

                // 2) GET single (id = 2)
                val single = try {
                    withContext(Dispatchers.IO) { RetrofitInstance.api.getPost(2) }
                } catch (e: Exception) {
                    crudLog += "GET(2) Error: ${e.message}\n\n"
                    return@LaunchedEffect
                }
                crudLog += "GET(2) → ${single.title}\n\n"

                // 3) POST
                val new = try {
                    withContext(Dispatchers.IO) {
                        RetrofitInstance.api.createPost(
                            Post(userId = 42, id = 0, title = "Reportes Mapify", body = "Reporta cierres")
                        )
                    }.also {
                        crudLog += "POST → id=${it.id}, title='${it.title}'\n\n"
                    }
                } catch (e: Exception) {
                    crudLog += "POST Error: ${e.message}\n\n"
                    return@LaunchedEffect
                }

                // 4) PUT sobre #1
                try {
                    crudLog += "PUT → Antes(1): '${all[0].title}'\n"
                    val upd = withContext(Dispatchers.IO) {
                        RetrofitInstance.api.updatePost(
                            1,
                            all[0].copy(title = "Reporta en Mapify")
                        )
                    }
                    crudLog += "PUT → Después(1): '${upd.title}'\n\n"
                } catch (e: Exception) {
                    crudLog += "PUT Error: ${e.message}\n\n"
                    return@LaunchedEffect
                }

                // 5) DELETE sobre #1
                try {
                    crudLog += "DELETE → Eliminando id=1, title='${all[0].title}'\n"
                    val resp = withContext(Dispatchers.IO) { RetrofitInstance.api.deletePost(1) }
                    crudLog += "DELETE → código=${resp.code()}\n"
                } catch (e: Exception) {
                    crudLog += "DELETE Error: ${e.message}\n"
                }
            }

            CrudResultsStyled(crudLog = crudLog)
        }
    }
}
