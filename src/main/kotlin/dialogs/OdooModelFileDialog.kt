package dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import icons.OdooIcons
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class OdooModelFileDialog(project: Project) : DialogWrapper(project) {

    enum class FileType {
        INIT, MANIFEST, MODEL, CONTROLLER
    }

    data class FileTypeItem(val displayName: String, val fileType: FileType, val icon: Icon)

    private val nameTextField = JBTextField()

    private val listModel = DefaultListModel<FileTypeItem>().apply {
        addElement(FileTypeItem("__init__.py", FileType.INIT, OdooIcons.INIT_ICON))
        addElement(FileTypeItem("__manifest__.py", FileType.MANIFEST, OdooIcons.MANIFEST_ICON))
        addElement(FileTypeItem("Odoo Model", FileType.MODEL, OdooIcons.MODEL_ICON))
        addElement(FileTypeItem("Odoo Controller", FileType.CONTROLLER, OdooIcons.CONTROLLER_ICON))
    }

    private val fileTypeList = JBList(listModel)
    private var selectedType: FileTypeItem? = null

    init {
        title = "New Odoo Model File"
        init()

        fileTypeList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        fileTypeList.setCellRenderer(FileTypeRenderer())
        fileTypeList.addListSelectionListener {
            selectedType = fileTypeList.selectedValue
            when (selectedType?.fileType) {
                FileType.INIT -> nameTextField.text = "__init__.py"
                FileType.MANIFEST -> nameTextField.text = "__manifest__.py"
                else -> nameTextField.text = ""
            }
        }

        fileTypeList.selectedIndex = 0
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(400, 250)

        val namePanel = JPanel(BorderLayout())
        namePanel.add(JLabel("File Name:"), BorderLayout.WEST)
        namePanel.add(nameTextField, BorderLayout.CENTER)

        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(namePanel, BorderLayout.NORTH)
        contentPanel.add(JScrollPane(fileTypeList), BorderLayout.CENTER)

        panel.add(contentPanel, BorderLayout.CENTER)
        return panel
    }

    fun getFileName(): String = nameTextField.text.trim()

    fun getSelectedFileType(): FileType = selectedType?.fileType ?: FileType.MODEL

    private inner class FileTypeRenderer : ColoredListCellRenderer<FileTypeItem>() {
        override fun customizeCellRenderer(
            list: JList<out FileTypeItem>,
            value: FileTypeItem?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            if (value == null) return
            icon = value.icon
            append(value.displayName)
            if (selected) {
                background = JBColor.namedColor("List.selectionBackground", JBColor(0x3875D6, 0x2F65CA))
                foreground = JBColor.namedColor("List.selectionForeground", JBColor.WHITE)
            }
        }
    }
}
