package io.github.herrherklotz.chameleon.x.utils

import io.github.herrherklotz.chameleon.MainService.Companion.context
import android.content.res.AssetManager
import io.github.herrherklotz.chameleon.MainService
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths


object Assets {
    private val mRegex = Regex("^(examples|templates)/(.[^/]*)")


    fun load(pPath: String): String {
        return context!!.assets.open(pPath).bufferedReader().use {
            it.readText()
        }
    }


    fun save(path: String, content: String): String {
        return context!!.assets.open(path).bufferedReader().use {
            it.readText()
        }
    }


    fun copyAssetsFile(sourceFolder: String, sourceFileName: String, targetFolder: String) {
        copyAssetsFile(sourceFolder, sourceFileName, targetFolder)
    }


    fun copyAssetsFile(sourceFolder: String, sourceFileName: String, targetFolder: String,
                       targetFileName: String = "") {
        val lAssetManager: AssetManager = MainService.context!!.assets

        val source = "$sourceFolder/$sourceFileName"
        var target = "$targetFolder/"

        target += if (targetFileName.isEmpty())
            sourceFileName
        else
            targetFileName

        val stream: InputStream? = lAssetManager.open(source)
        val output: OutputStream? = BufferedOutputStream(FileOutputStream(Paths.get(MainService
            .context!!.filesDir.absolutePath, target).toFile()))

        val data = ByteArray(1024)
        var count: Int = stream!!.read(data)

        while (count != -1) {
            output!!.write(data, 0, count)
            count = stream.read(data)
        }

        output!!.flush()
        output.close()
        stream.close()
    }


    fun copyProjectFromAssets(pProjectName: String, pAssetFolder: String) {
        if (context!!.assets.list(pAssetFolder)!!.isNotEmpty()) {
            // pAssetFolder is a directory
            Files.createDirectories(Paths.get(context!!.filesDir.absolutePath, "Project",
                pProjectName, pAssetFolder.replace(mRegex, "")))
        }

        for (fileName in context!!.assets.list(pAssetFolder)!!) {
            val lAssetFolder = "$pAssetFolder/$fileName"

            if (context!!.assets.list(lAssetFolder)!!.isNotEmpty()) {
                // lAssetFolder is a directory
                copyProjectFromAssets(pProjectName, lAssetFolder)
            } else {
                // lAssetFolder is a file
                val stream: InputStream = context!!.assets.open(lAssetFolder)
                val output: OutputStream = BufferedOutputStream(FileOutputStream(Paths.get(
                    context!!.filesDir.absolutePath, "Project", pProjectName, lAssetFolder
                    .replace(mRegex, "")).toFile()))

                val data = ByteArray(1024)
                var count: Int = stream.read(data)

                while (count != -1) {
                    output.write(data, 0, count)
                    count = stream.read(data)
                }

                output.flush()
                output.close()
                stream.close()
            }
        }
    }


    fun listFolders(path: String) : List<String> {
        return try {
            context!!.assets.list(path)!!.toList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}