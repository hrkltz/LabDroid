package io.github.herrherklotz.chameleon.x.utils

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.herrherklotz.chameleon.MainService.Companion.context
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.stream.Collectors


object Storage {
    fun exists(path: String): Boolean {
        return Files.exists(Paths.get(context!!.filesDir.absolutePath, path))
    }


    fun readFile(path: String): Pair<Boolean, String> {
        return readFile(Paths.get(context!!.filesDir.absolutePath, path))
    }


    fun readFile(path: Path): Pair<Boolean, String> {
        val falseResult = Pair(false, "")

        if (!Files.exists(path))
            return falseResult

        if (!Files.isRegularFile(path))
            return falseResult

        try {
            return Pair(true, path.toFile().readText())
        } catch (ignored: Exception) {}

        return falseResult
    }


    fun readDirectory(path: String): Pair<Boolean, Any> {
        val map = HashMap<String, Any?>()

        try {
            val baseFile = Paths.get(context!!.filesDir.absolutePath, path).toFile()

            baseFile.walkTopDown().forEach {
                if (it.isDirectory) {
                    val pathSplitted = it.relativeTo(baseFile).path.toString().split("/")
                    
                    if (pathSplitted[0].isNotEmpty()) {// If == 1: This is the baseFile we don't need to add it.
                        var tmpMap = map

                        pathSplitted.forEach {
                            if (!tmpMap.containsKey(it))
                                tmpMap[it] = HashMap<String, Any>()

                            tmpMap = tmpMap[it] as HashMap<String, Any?>
                        }
                    }
                }
                else {
                    val pathSplitted = it.relativeTo(baseFile).path.toString().split("/").dropLast(1)
                    var tmpMap = map

                    pathSplitted.forEach {
                        tmpMap = tmpMap[it] as HashMap<String, Any?>
                    }

                    tmpMap[it.name] = null
                }
            }

            println(ObjectMapper().writeValueAsString(map))
        } catch (e: IOException) {
            e.printStackTrace()
            return Pair(false, "")
        }

        return Pair(true, map)
    }


    fun loadFile(path: String): Pair<Boolean, File> {
        return loadFile(Paths.get(context!!.filesDir.absolutePath, path))
    }


    private fun loadFile(path: Path): Pair<Boolean, File> {
        val falseResult = Pair(false, File(""))

        if (!Files.exists(path))
            return falseResult

        if (!Files.isRegularFile(path))
            return falseResult

        try {
            return Pair(true, path.toFile())
        } catch (ignored: Exception) {}

        return falseResult
    }



    /*fun <T> loadObject(path: String): Pair<Boolean, T?> {
        val falseResult = Pair(false, null)

        val result = load(path)

        if (!result.first)
            return falseResult

        try {
            return Pair(true, objectMapper.readValue(result.second, T))
        } catch (ignored: Exception) {}

        return falseResult
    }*/


    fun save(path: String, content: String, append: Boolean = false): Boolean {
        val absolutePath = Paths.get(context!!.filesDir.absolutePath, path)
        val parent = absolutePath.parent

        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }

        try {
            if (append)
                Files.write(absolutePath, content.toByteArray(), StandardOpenOption.APPEND)
            else
                Files.write(absolutePath, content.toByteArray())
        } catch (ignore: IOException) {
            return false
        }

        return true
    }


    /*private fun save(path: Path, content: String): Boolean {
        val absolutePath = Paths.get(context!!.filesDir.absolutePath, path.toString())
        val parent = absolutePath.parent

        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }

        try {
            Files.write(absolutePath, content.toByteArray())
        } catch (ignore: IOException) {
            return false
        }

        return true
    }*/


    fun saveFolder(path: String): Boolean {
        return saveFolder(Paths.get(path))
    }


    fun saveFolder(path: Path): Boolean {
        val absolutePath = Paths.get(context!!.filesDir.absolutePath, path.toString())

        if (!Files.exists(absolutePath)) {
            try {
                Files.createDirectories(absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }

        return true
    }


    /*fun listFiles(path: String) : List<String> {
        return try {
            Files.list(Paths.get(context!!.filesDir.absolutePath, path))
                    .filter { path: Path? -> Files.isRegularFile(path) }
                    .map { obj: Path -> obj.toFile().name }
                    .map { e: String -> e.replaceFirst(".json".toRegex(), "") } // TODO make this step optional
                    .collect(Collectors.toList())//.forEach(Consumer { eNodeName: String? -> lNodes.put(eNodeName, NodeInterface.load(this.mName, pGroupName, eNodeName)) })
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }*/


    fun listComponentsRecursive(projectName: String) : List<String> {
        return try {
            val basePath = Paths.get(context!!.filesDir.absolutePath, "Project",
                    projectName, "Component")

            basePath.toFile()
                    .walkTopDown()
                    .filter { it.isFile }
                    .map {
                        it.absolutePath
                                .replace("${basePath}/", "")
                                .replace("/", "_")
                                .replace(".json", "")
                    }
                    .toList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }


    fun listFolders(path: String) : List<String> {
        return try {
            Files.list(Paths.get(context!!.filesDir.absolutePath, path))
                    .filter { Files.isDirectory(it) }
                    .map { it.toFile().name }
                    .collect(Collectors.toList())//.forEach(Consumer { eNodeName: String? -> lNodes.put(eNodeName, NodeInterface.load(this.mName, pGroupName, eNodeName)) })
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }


    fun deleteFile(path: String) : Boolean {
        val file = Paths.get(context!!.filesDir.absolutePath, path).toFile()
        return file.delete()
    }


    fun deleteFolder(path: String) : Boolean {
        val file = Paths.get(context!!.filesDir.absolutePath, path).toFile()
        return file.deleteRecursively()
    }
}