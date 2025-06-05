package indexing

import com.intellij.util.io.DataExternalizer
import java.io.DataInput
import java.io.DataOutput

object ListOfStringDataExternalizer : DataExternalizer<List<String>> {
    override fun save(out: DataOutput, value: List<String>) {
        out.writeInt(value.size)
        for (item in value) {
            out.writeUTF(item)
        }
    }

    override fun read(input: DataInput): List<String> {
        val size = input.readInt()
        val result = mutableListOf<String>()
        repeat(size) {
            result.add(input.readUTF())
        }
        return result
    }
}
