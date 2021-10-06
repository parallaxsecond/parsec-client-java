import groovy.io.FileType

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

import static java.nio.charset.StandardCharsets.UTF_8
import static java.nio.file.StandardOpenOption.CREATE
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING


def src_dir = new File(protobuf_src_dir).getCanonicalFile()
def dest_dir = new File(protobuf_dest_dir)
dest_dir.mkdirs()
src_dir.eachFileRecurse (FileType.FILES) { src ->
    def source = src.toPath()
    def relativeProtoFilePath = src.getAbsolutePath().replaceAll(src_dir.getAbsolutePath(), "")
    def dest = new File(dest_dir, relativeProtoFilePath).getCanonicalFile().toPath()
    def sourceTs = Files.getLastModifiedTime(source)
    if (!Files.exists(dest) || sourceTs.toMillis() > Files.getLastModifiedTime(dest).toMillis()) {
        def content = new String(Files.readAllBytes(source), UTF_8)
                .replaceAll('(\n|^)package ([^;]+)([;]?(?:\n|$))',
                        "\$1package \$2;\noption java_package = \"${protobuf_java_package}.\$2\"\$3")
        Files.write(dest, content.getBytes(UTF_8), CREATE, TRUNCATE_EXISTING)
        Files.setLastModifiedTime(dest, sourceTs)
    }
}