import java.io.File

fun main() {
    val file = File("src/main/resources/eil101.tsp")
    if (file.exists()) {
        println("File found at ${file.absolutePath}")
    } else {
        println("File not found!")
    }

    val path = "eil101.tsp"

    //TODO set starting city, which is always at index 0
    val inputStream = TSP::class.java.classLoader.getResourceAsStream(path)
    if (inputStream == null) {
        System.err.println("File $path not found!")
        return
    }
    else {
        println("File found at ${file.absolutePath}")
        // read first line
        val line = inputStream.bufferedReader().readLine()
        println(line)
    }
}