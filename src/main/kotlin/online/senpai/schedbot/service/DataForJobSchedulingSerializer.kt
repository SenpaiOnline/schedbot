package online.senpai.schedbot.service

import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer
import java.io.Serializable
import java.time.OffsetDateTime

class DataForJobSchedulingSerializer : Serializer<DataForJobScheduling>, Serializable {
    override fun serialize(out: DataOutput2, value: DataForJobScheduling) {
        out.apply {
            writeLong(value.messageChannel)
            writeLong(value.author)
            writeUTF(value.rawMessage)
            writeUTF(value.delayUntil.toString())
        }
    }

    override fun deserialize(input: DataInput2, available: Int): DataForJobScheduling {
        return DataForJobScheduling(
            messageChannel = input.readLong(),
            author = input.readLong(),
            rawMessage = input.readUTF(),
            delayUntil = OffsetDateTime.parse(input.readUTF())
        )
    }
}
