import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

//文本选择
data class Document(
    val file: File?,
    var content: TextFieldValue
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextWriterUi() {
    var documents by remember { mutableStateOf(listOf<Document>()) } // 文档列表
    var currentDocumentIndex by remember { mutableStateOf(-1) } //当前文档索引

    val defaultTextStyle = MaterialTheme.typography.body1
    var textStyle by remember { mutableStateOf(defaultTextStyle) }

    var showCloseConfirmationDialog by remember { mutableStateOf(false) } // 显示关闭确认对话框
    var closingDocumentIndex by remember { mutableStateOf(-1) } // 关闭文档索引

    // 文档选择器
    val fileChooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("Text Files", "txt")
    }

    fun closeTab(index: Int) {
        showCloseConfirmationDialog = true
        closingDocumentIndex = index
    }


    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            // 显示关闭确认对话框
            if (showCloseConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showCloseConfirmationDialog = false },
                    title = { Text("关闭文档") },
                    text = { Text("是否保存对文档的更改？") },
                    confirmButton = {
                        Button(
                            onClick = {
                                // 保存文档
                                val closingDocument = documents[closingDocumentIndex]
                                closingDocument.file?.writeText(closingDocument.content.text)
                                // 从列表中移除并关闭对话框
                                documents = documents.filterIndexed { i, _ -> i != closingDocumentIndex }
                                if (closingDocumentIndex <= currentDocumentIndex) {
                                    currentDocumentIndex = maxOf(0, currentDocumentIndex - 1)
                                }
                                showCloseConfirmationDialog = false
                            }
                        ) { Text("保存") }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                documents = documents.filterIndexed { i, _ -> i != closingDocumentIndex }
                                if (closingDocumentIndex <= currentDocumentIndex) {
                                    currentDocumentIndex = maxOf(0, currentDocumentIndex - 1)
                                }
                                showCloseConfirmationDialog = false
                            }
                        ) { Text("不保存") }
                    }
                )
            }

            // 文档选项卡
            DocumentTabs(documents, currentDocumentIndex, onTabSelected = { index ->
                currentDocumentIndex = index
            }, onCloseTab = ::closeTab)

            // 文档操作工具栏
            DocumentOperationsToolbar(
                onNew = {
                    val newDocument = Document(null, TextFieldValue())
                    documents = documents + newDocument
                    currentDocumentIndex = documents.lastIndex
                    println("${documents.size} + ${documents.lastIndex}")
                },

                //打开文档
                onOpen = {
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        val selectedFile = fileChooser.selectedFile
                        val newDocument = Document(selectedFile, TextFieldValue(selectedFile.readText()))
                        documents = documents + newDocument
                        currentDocumentIndex = documents.lastIndex
                    }
                },

                //保存文档
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

                //复制
                onCopy = {
                    if (currentDocumentIndex in documents.indices) {
                        val currentContent = documents[currentDocumentIndex].content.text
                        setClipboardContent(currentContent)
                    }
                },

                //粘贴
                onPaste = {
                    if (currentDocumentIndex in documents.indices) {
                        val clipboardContent = getClipboardContent()
                        val currentDocument = documents[currentDocumentIndex]
                        documents = documents.toMutableList().apply {
                            this[currentDocumentIndex] =
                                currentDocument.copy(content = TextFieldValue(clipboardContent))
                        }
                    }
                },

                onTextStyleChange = { newStyle -> textStyle = newStyle }, //更改文本样式
                textStyle = textStyle, //当前文本样式
            )

            if (currentDocumentIndex in documents.indices) {
                //显示文本
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

// 文档选项卡
@Composable
fun DocumentTabs(
    documents: List<Document>,
    currentDocumentIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    onCloseTab: (index: Int) -> Unit
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
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                document.file?.name ?: "未命名",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.weight(2f)
                            )
                            // 关闭按钮
                            IconButton(onClick = { onCloseTab(index) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    },
                    modifier = Modifier.padding(2.dp)
                        .border(color = Color.Gray, shape = RoundedCornerShape(4.dp), width = 1.dp)
                        .background(Color.White)
                )
            }
        }
    }
}


// 获取文本并复制
fun getClipboardContent(): String {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    return clipboard.getData(DataFlavor.stringFlavor) as String
}

// 在文档中粘贴文本
fun setClipboardContent(content: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val data = StringSelection(content)
    clipboard.setContents(data, data)
}

// 文档操作工具栏
@Composable
fun DocumentOperationsToolbar(
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
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

        Button(onClick = onCopy, colors = ButtonDefaults.buttonColors(Color(0xff708090))) {
            Text("复制")
        }

        Button(onClick = onPaste, colors = ButtonDefaults.buttonColors(Color(0xff708090))) {
            Text("粘贴")
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
