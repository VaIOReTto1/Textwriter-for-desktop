import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

data class Document(
    val file: File?,
    var content: TextFieldValue
)

@Composable
fun TextWriterUi() {
    var documents by remember { mutableStateOf(listOf<Document>()) }
    var currentDocumentIndex by remember { mutableStateOf(-1) }

    val defaultTextStyle = MaterialTheme.typography.body1
    var textStyle by remember { mutableStateOf(defaultTextStyle) }
    val fileChooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("Text Files", "txt")
    }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DocumentTabs(documents, currentDocumentIndex) { index ->
                currentDocumentIndex = index
            }

            DocumentOperationsToolbar(
                onNew = {
                    val newDocument = Document(null, TextFieldValue())
                    documents = documents + newDocument
                    currentDocumentIndex = documents.lastIndex // 确保更新为新文档的索引
                    println("${documents.size} + ${documents.lastIndex}")
                },

                onOpen = {
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        val selectedFile = fileChooser.selectedFile
                        val newDocument = Document(selectedFile, TextFieldValue(selectedFile.readText()))
                        documents = documents + newDocument
                        currentDocumentIndex = documents.lastIndex // 确保更新为新文档的索引
                    }
                },
                onSave = {
                    currentDocumentIndex.takeIf { it >= 0 }?.let { index ->
                        val currentDocument = documents[index]
                        currentDocument.file?.writeText(currentDocument.content.text) ?: run {
                            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                                val newFile = fileChooser.selectedFile
                                newFile.writeText(currentDocument.content.text)
                                documents = documents.toMutableList().apply {
                                    this[index] = currentDocument.copy(file = newFile)
                                }
                            }
                        }
                    }
                },
                onTextStyleChange = { newStyle -> textStyle = newStyle },
                textStyle = textStyle,
            )

            if (currentDocumentIndex in documents.indices) {
                val currentDocument = documents[currentDocumentIndex]
                BasicTextField(
                    value = currentDocument.content,
                    onValueChange = { newValue ->
                        documents = documents.toMutableList().apply {
                            this[currentDocumentIndex] = currentDocument.copy(content = newValue)
                        }
                    },
                    textStyle = textStyle,
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DocumentTabs(
    documents: List<Document>,
    currentDocumentIndex: Int,
    onTabSelected: (index: Int) -> Unit
) {
    if (documents.isNotEmpty()) {
        TabRow(
            selectedTabIndex = currentDocumentIndex,
            backgroundColor = Transparent,
            modifier = Modifier.width(120 * documents.size.dp).height(45.dp).padding(4.dp)
        ) {
            documents.forEachIndexed { index, document ->
                Tab(
                    selected = index == currentDocumentIndex,
                    onClick = { onTabSelected(index) },
                    text = { Text(document.file?.name ?: "未命名", overflow = TextOverflow.Ellipsis,maxLines = 1) },
                    modifier = Modifier.padding(2.dp)
                        .border(color = Color.Gray, shape = RoundedCornerShape(4.dp), width = 1.dp).background(
                            Color.White
                        )
                )
            }
        }
    }
}

@Composable
fun DocumentOperationsToolbar(
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onTextStyleChange: (TextStyle) -> Unit,
    textStyle: TextStyle,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Button(onClick = onNew, colors = ButtonDefaults.buttonColors(Color(0xff708090))) {
            Text("新建")
        }
        Button(onClick = onOpen, colors = ButtonDefaults.buttonColors(Color(0xff708090))) {
            Text("打开")
        }
        Button(onClick = onSave, colors = ButtonDefaults.buttonColors(Color(0xff708090))) {
            Text("保存")
        }
        Button(
            onClick = { onTextStyleChange(textStyle.copy(fontWeight = FontWeight.Bold)) },
            colors = ButtonDefaults.buttonColors(Color(0xff708090))
        ) {
            Text("黑体")
        }
        Button(
            onClick = { onTextStyleChange(textStyle.copy(fontStyle = FontStyle.Italic)) },
            colors = ButtonDefaults.buttonColors(Color(0xff708090))
        ) {
            Text("斜体")
        }
    }
}
