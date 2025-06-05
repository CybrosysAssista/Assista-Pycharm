package icons

import com.intellij.openapi.util.IconLoader

object PluginIcons {
    val MODULE_TEMPLATES = IconLoader.getIcon("/icons/module_templates.svg", PluginIcons::class.java)
    val BASIC_MODULE = IconLoader.getIcon("/icons/basic_module.svg", PluginIcons::class.java)
    val ADVANCE_MODULE = IconLoader.getIcon("/icons/theme_module.svg", PluginIcons::class.java)
    val THEME_MODULE = IconLoader.getIcon("/icons/website_theme.svg", PluginIcons::class.java)
    val OWL_BASIC_MODULE = IconLoader.getIcon("/icons/basic_owl.svg", PluginIcons::class.java)
    val OWL_ADVANCE_MODULE = IconLoader.getIcon("/icons/advance_owl.svg", PluginIcons::class.java)
    val SYSTRAY_ICON_MODULE = IconLoader.getIcon("/icons/systray_icon.svg", PluginIcons::class.java)
}