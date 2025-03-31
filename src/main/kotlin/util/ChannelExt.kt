package util

import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun ByteReadChannel.asLineFlow(): Flow<String> = flow {
    while (!isClosedForRead) {
        val line = readUTF8Line()
        if (line != null) emit(line)
    }
}
