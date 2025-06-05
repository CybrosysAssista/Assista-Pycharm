package utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.fasterxml.jackson.module.kotlin.*
import org.yaml.snakeyaml.Yaml

object ModuleCreator {
    fun createModuleTemplate(project: Project, parentDir: VirtualFile, moduleName: String,moduleType: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            try {
                val inputStream = javaClass.classLoader.getResourceAsStream("data/module_template.yaml")
                    ?: throw IllegalArgumentException("YAML file not found in classpath")

                val yaml = Yaml()
                val data = yaml.load<Map<String, Any>>(inputStream)
                val template = data[moduleType] as? Map<String, Any>
                    ?: throw IllegalArgumentException("${moduleType} not found or not a map")
                structureRender(template, parentDir, moduleName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createFile(dir: VirtualFile, name: String, content: String) {
        val file = dir.createChildData(this, name)
        file.setBinaryContent(content.toByteArray())
    }

    private fun structureRender(
        template: Map<String, Any> = emptyMap(),
        parentDir: VirtualFile,
        moduleName: String
    ) {
        val newDynamicValues = mapOf(
            "__moduleName__" to moduleName,
            "__formalModuleName__" to moduleName.replace("_", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
            "__modelName__" to "${moduleName}_sample",
            "__modelTechnicalName__" to "${moduleName}_sample".replace("_", "."),
            "__modelClassName__" to moduleName.split('_')
                .joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } } + "Sample"
        )

        for ((key, value) in template) {
            when (value) {
                is String -> {
                    val fileContent = replacePlaceholders(value,newDynamicValues)
                    val fileName = replacePlaceholders(key,newDynamicValues)
                    createFile(parentDir, fileName, fileContent.trimIndent())
                }
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val nestedTemplate = value as? Map<String, Any> ?: continue
                    val folderName = replacePlaceholders(key,newDynamicValues)
                    val childDir = parentDir.createChildDirectory(this, folderName)
                    structureRender(nestedTemplate, childDir, moduleName)
                }
                else -> {
                    println("Unsupported type at key '$key': ${value?.javaClass?.simpleName}")
                }
            }
        }
    }
    fun replacePlaceholders(input: String, replacements: Map<String, Any>): String {
        var result = input
        for ((key, value) in replacements) {
            result = result.replace(key, value.toString())
        }
        return result
    }
}
