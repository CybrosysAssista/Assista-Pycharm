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

class OdooViewFileDialog(project: Project) : DialogWrapper(project) {
    enum class ViewType {
        EMPTY, BASIC, ADVANCED, INHERIT, REPORT, SECURITY_GROUP, SECURITY_RULE, SEQUENCE, SETTINGS, CRON_JOB
    }

    data class ViewTypeItem(val displayName: String, val viewType: ViewType, val icon: Icon)

    private val nameTextField = JBTextField()
    private val listModel = DefaultListModel<ViewTypeItem>().apply {
        addElement(ViewTypeItem("Empty View", ViewType.EMPTY, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Basic View", ViewType.BASIC, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Advanced View", ViewType.ADVANCED, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Inherit View", ViewType.INHERIT, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Report View", ViewType.REPORT, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Security Group View", ViewType.SECURITY_GROUP, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Security Rule View", ViewType.SECURITY_RULE, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Sequence View", ViewType.SEQUENCE, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Settings View", ViewType.SETTINGS, OdooIcons.ODOO_ICON))
        addElement(ViewTypeItem("Cron Job View", ViewType.CRON_JOB, OdooIcons.ODOO_ICON))
    }

    private val viewTypeList = JBList(listModel)
    private var selectedType: ViewTypeItem? = null

    init {
        title = "New Odoo XML View File"
        init()
        viewTypeList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        viewTypeList.setCellRenderer(ViewTypeRenderer())
        viewTypeList.addListSelectionListener {
            selectedType = viewTypeList.selectedValue
        }
        viewTypeList.selectedIndex = 2 // Default to Basic View
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(400, 300)

        val namePanel = JPanel(BorderLayout())
        namePanel.add(JLabel("File Name:"), BorderLayout.WEST)
        namePanel.add(nameTextField, BorderLayout.CENTER)

        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(namePanel, BorderLayout.NORTH)
        contentPanel.add(JScrollPane(viewTypeList), BorderLayout.CENTER)

        panel.add(contentPanel, BorderLayout.CENTER)
        return panel
    }

    fun getFileName(): String = nameTextField.text.trim()
    fun getSelectedViewType(): ViewType = selectedType?.viewType ?: ViewType.BASIC

    private inner class ViewTypeRenderer : ColoredListCellRenderer<ViewTypeItem>() {
        override fun customizeCellRenderer(
            list: JList<out ViewTypeItem>,
            value: ViewTypeItem?,
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