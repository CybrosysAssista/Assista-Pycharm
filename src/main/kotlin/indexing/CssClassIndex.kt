package indexing

import com.intellij.util.indexing.*
import com.intellij.util.io.*
import com.intellij.openapi.project.Project
import java.io.DataInput
import java.io.DataOutput
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.openapi.vfs.VirtualFile

class CssClassIndex : FileBasedIndexExtension<String, Void>() {

    companion object {
        val NAME: ID<String, Void> = ID.create("odoo.css.classes")
    }

    override fun getName(): ID<String, Void> = NAME

    override fun getVersion(): Int = 1

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer { inputData ->
        val map = mutableMapOf<String, Void?>()
        val content = inputData.contentAsText.toString()

        // Regex to find class names like .btn-primary, .card-title
        val regex = Regex("""\.([a-zA-Z0-9_-]+)""")
        regex.findAll(content).forEach { match ->
            val className = match.groupValues[1]
            map[className] = null
        }
        map
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter { file: VirtualFile ->
        file.name.endsWith(".css", ignoreCase = true)
    }

    override fun dependsOnFileContent(): Boolean = true

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): DataExternalizer<Void> = object : DataExternalizer<Void> {
        override fun save(out: DataOutput, value: Void?) {}
        override fun read(input: DataInput): Void? = null
    }
}