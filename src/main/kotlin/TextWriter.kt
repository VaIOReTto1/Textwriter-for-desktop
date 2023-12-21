import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

val Black_color = 0xff2d343c
val fontSizes = listOf(8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextWriterUi() {
    var documents by remember { mutableStateOf(listOf<Document>()) } // 文档列表
    var currentDocumentIndex by remember { mutableStateOf(-1) } //当前文档索引

    val defaultTextStyle = MaterialTheme.typography.body1
    var textStyle by remember { mutableStateOf(defaultTextStyle) }

    var selectedFontSize by remember { mutableStateOf(12) }

    textStyle = textStyle.copy(fontSize = selectedFontSize.sp)

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
                    modifier = Modifier
                        .clip(RoundedCornerShape(8)),
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
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xffe6ecf5)
                            ),
                            modifier = Modifier
                                .shadow(
                                    elevation = 15.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    spotColor = Color(0xffc0c3d0)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.elevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
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
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xffe6ecf5)
                            ),
                            modifier = Modifier
                                .shadow(
                                    elevation = 15.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    spotColor = Color(0xffc0c3d0)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.elevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
                        ) { Text("不保存") }
                    }
                )
            }

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

                onFontSizeChange = { newSize ->
                    selectedFontSize = newSize
                },
                currentFontSize = selectedFontSize
            )

            // 文档选项卡
            DocumentTabs(documents, currentDocumentIndex, onTabSelected = { index ->
                currentDocumentIndex = index
            }, onCloseTab = ::closeTab)

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

@Composable
fun CustomTextButton(
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color(Black_color),
    backgroundColor: Color = Transparent,
    clickedColor: Color = Color(0xffeaebec)// 点击时的颜色
) {
    val interactionSource = remember { MutableInteractionSource() }

    TextButton(
        onClick = onClick,
        // 使用interactionSource来追踪按钮的交互状态
        interactionSource = interactionSource,
        // 定制按钮颜色
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor
        ),
        modifier = Modifier.background(
            brush = rememberUpdatedState(
                Brush.verticalGradient(
                    listOf(
                        if (interactionSource.collectIsPressedAsState().value) clickedColor else backgroundColor,
                        backgroundColor
                    )
                )
            ).value
        )
    ) {
        Text(text = text, color = textColor)
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
            modifier = Modifier.width(120 * documents.size.dp).height(45.dp).padding(4.dp),
            indicator = {}
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
                        .border(color = Color.Gray, shape = RoundedCornerShape(2.dp), width = 1.dp)
                        .background(Color(0xffeaebec))
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
    onFontSizeChange: (Int) -> Unit,
    currentFontSize: Int
) {
    val _isBold = remember { mutableStateOf(false) }
    val _isItalic = remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(2.dp)) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val shadowColor = Color.Gray
        val shadowAlpha = 0.1f // 减少阴影的透明度以适应工具栏
        val cornerRadius = CornerRadius(if (width < height) width / 2 else height / 2) // 设置圆角半径

        Canvas(modifier = Modifier.matchParentSize()) {
            // 绘制底部阴影
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Transparent, shadowColor.copy(alpha = shadowAlpha)),
                    startY = height - 10f,
                    endY = height
                ),
                size = Size(width, 10f),
                topLeft = Offset(0f, height - 10f),
                cornerRadius = cornerRadius
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp), // 为整个工具栏设置外边距
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // 在主轴方向上分配空间
        ) {
            // 第一个带圆角边框的Row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(Black_color), RoundedCornerShape(50.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly // 按钮均匀分布
            ) {
                // 左侧黑点
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(Black_color), CircleShape)
                )

                CustomTextButton("NEW FILE", onNew)
                CustomTextButton("OPEN FILE", onOpen)
                CustomTextButton("SAVE", onSave)

                // 右侧黑点
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(Black_color), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 第二个带圆角边框的Row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(Black_color), RoundedCornerShape(50.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly // 按钮均匀分布
            ) {
                // 左侧黑点
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(Black_color), CircleShape)
                )

                CustomTextButton("COPY", onCopy)
                CustomTextButton("PASTE", onPaste)
                CustomTextButton("BOLD", {
                    _isBold.value = !_isBold.value
                    onTextStyleChange(textStyle.copy(fontWeight = if (_isBold.value) FontWeight.Bold else FontWeight.Normal))
                })
                CustomTextButton(
                    "ITALIC",
                    {
                        _isItalic.value = !_isItalic.value
                        onTextStyleChange(textStyle.copy(fontStyle = if (_isItalic.value) FontStyle.Italic else FontStyle.Normal))
                    },
                )

                var expanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                    CustomTextButton("FontSize", { expanded = true })

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .height(200.dp).width(80.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        fontSizes.forEach { size ->
                            DropdownMenuItem(onClick = {
                                onFontSizeChange(size)
                                expanded = false
                            }) {
                                Text("$size")
                            }
                        }
                    }
                }

                // 右侧黑点
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(Black_color), CircleShape)
                )
            }
        }
    }
}
