import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun TextWriterUi() {
    var currentDocument by remember { mutableStateOf<Document?>(null) }
    val documents = remember { mutableStateListOf<Document>() }

    Column {
        TopAppBar(
            contentColor = Color.White,
            title = { Text("文本编辑器") },
            actions = {
                TextButton(onClick = {
                    currentDocument = Document("未命名", "")
                    documents.add(currentDocument!!)
                }) {
                    Text("New", color = Color.White)
                }

                TextButton(onClick = {
                    val dialog = FileDialog(Frame(), "Open", FileDialog.LOAD)
                    dialog.isVisible = true
                    dialog.file?.let {
                        currentDocument = Document(
                            it,
                            File(dialog.directory, it).readText()
                        )
                        documents.add(currentDocument!!)
                    }
                }) {
                    Text("打开", color = Color.White)
                }

                TextButton(onClick = {
                    currentDocument?.let { doc ->
                        val dialog = FileDialog(Frame(), "Save", FileDialog.SAVE)
                        dialog.file = doc.name
                        dialog.isVisible = true
                        dialog.file?.let {
                            File(dialog.directory, it).writeText(doc.content)
                        }
                    }
                }) {
                    Text("保存", color = Color.White)
                }

                TextButton(onClick = {
                    currentDocument?.let {
                        it.isBold = !it.isBold
                    }
                }) {
                    Text("黑体", color = Color.White)
                }

                TextButton(onClick = {
                    currentDocument?.let {
                        it.isItalic = !it.isItalic
                    }
                }) {
                    Text("斜体", color = Color.White)
                }
            }
        )

        Row(Modifier.fillMaxSize()) {
            Column(Modifier.width(200.dp).fillMaxHeight().background(Color.Gray)) {
                documents.forEach { doc ->
                    TextButton(onClick = { currentDocument = doc }) {
                        Text(doc.name)
                    }
                }
            }

            Box(Modifier.fillMaxSize()) {
                currentDocument?.let { doc ->
                    val text= remember { mutableStateOf(doc.content) }
                    BasicTextField(
                        value = text.value,
                        onValueChange = {
                            text.value = it
                        },
                        modifier = Modifier.fillMaxSize()
                            .padding(8.dp),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (doc.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (doc.isItalic) FontStyle.Italic else FontStyle.Normal
                        )
                    )
                }
            }
        }
    }
}

data class Document(var name: String, var content: String, var isBold: Boolean = false, var isItalic: Boolean = false)
