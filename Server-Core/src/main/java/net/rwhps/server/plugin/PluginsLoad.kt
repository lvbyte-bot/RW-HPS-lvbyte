/*
 * Copyright 2020-2024 Dr (dr@der.kim) and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin

import net.rwhps.server.data.bean.internal.BeanPluginInfo
import net.rwhps.server.data.global.Data
import net.rwhps.server.util.file.json.Json
import net.rwhps.server.dependent.LibraryManager
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.util.IsUtils
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.error
import net.rwhps.server.util.log.Log.warn
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader

/**
 * 在这里完成 插件的加载
 * @author Dr (dr@der.kim)
 */
class PluginsLoad {
    private fun loadPlugin(fileList: Seq<File>): Seq<PluginLoadData> {
        val data = Seq<PluginLoadData>()
        val dataName = Seq<String>()
        val dataImport = Seq<PluginImportData>()

        val scriptContext = JavaScriptPluginGlobalContext()

        for (file in fileList) {
            val zip = CompressionDecoderUtils.zip(file)
            try {
                val imp = zip.getZipNameInputStream("plugin.json")
                if (imp == null) {
                    error("Invalid Plugin file", file.name)
                    continue
                }
                // 读取 插件需要的 依赖
                // 进行注入进服务端
                val imports = zip.getZipNameInputStream("imports.json")
                if (imports != null) {
                    loadImports(Json(FileUtils.readFileString(imports)).getArraySeqData("imports"))
                }

                val pluginInfo = BeanPluginInfo::class.java.toGson(FileUtils.readFileString(imp))
                if (!GetVersion(Data.SERVER_CORE_VERSION).getIfVersion(pluginInfo.supportedVersions)) {
                    warn("Plugin版本不兼容 Plugin名字为: ", pluginInfo.name)
                    continue
                }
                if (IsUtils.isBlank(pluginInfo.import)) {
                    val mainPlugin: Plugin
                    if (pluginInfo.main.endsWith("js", true)) {
                        val mainJs = zip.getZipNameInputStream(pluginInfo.main)
                        if (mainJs == null) {
                            error("Invalid JavaScriptPlugin Main", pluginInfo.main)
                            continue
                        }
                        if (pluginInfo.internalName.isBlank() ||
                            pluginInfo.internalName.replace("^[a-z0-9A-Z]+\$".toRegex(), "").isNotBlank()
                            ) {
                            error("Invalid Internal Name Main", pluginInfo.main)
                            continue
                        }
                        scriptContext.addESMPlugin(pluginInfo, zip.getZipAllBytes())
                        continue
                    } else {
                        mainPlugin = loadClass(file, pluginInfo.main)
                    }

                    data.add(
                            PluginLoadData(
                                    pluginInfo.name, pluginInfo.internalName, pluginInfo.author, pluginInfo.description, pluginInfo.version, mainPlugin
                            )
                    )
                    dataName.add(pluginInfo.name)
                } else {
                    dataImport.add(PluginImportData(pluginInfo, file))
                }
            } catch (e: Exception) {
                error("Failed to load", e)
            } finally {
                zip.close()
            }
        }

        // 检查是否加载了依赖插件
        var i = 0
        val count = dataImport.size
        while (i < count) {
            dataImport.eachAll { e: PluginImportData ->
                if (dataName.contains(e.pluginData.import)) {
                    try {
                        val mainPlugin = loadClass(e.file, e.pluginData.main)
                        data.add(
                                PluginLoadData(
                                        e.pluginData.name, e.pluginData.internalName, e.pluginData.author, e.pluginData.description, e.pluginData.version, mainPlugin
                                )
                        )
                        dataName.add(e.pluginData.name)
                        dataImport.remove(e)
                    } catch (err: Exception) {
                        error("Failed to load", e)
                    }
                }
            }
            i++
        }

        scriptContext.loadESMPlugins().eachAll {
            data.add(it)
            dataName.add(it.name)
        }

        return data
    }


    private fun loadImports(importsJson: Seq<Json>) {
        val lib = LibraryManager()
        importsJson.eachAll {
            lib.implementation(it.getString("group"), it.getString("module"), it.getString("version"), it.getString("classifier"))
        }
        lib.loadToClassLoader()
    }

    /**
     * 根据包名加载实例
     * @param file Jar文件
     * @param main 主类的包名
     * @return Plugin实例
     * @throws InstantiationException 如果声明基础构造函数的类表示抽象类
     * @throws InvocationTargetException 如果基础构造函数引发异常
     * @throws ExceptionInInitializerError 如果此方法引发的初始化失败
     */
    @Throws(InstantiationException::class, InvocationTargetException::class, ExceptionInInitializerError::class)
    private fun loadClass(file: File, main: String): Plugin {
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()), ClassLoader.getSystemClassLoader())
        Log.info(file.name)
        val classMain = classLoader.loadClass(main)
        //mainPlugin.classLoader = classLoader
        return classMain.getDeclaredConstructor().newInstance() as Plugin
    }

    /**
     * Plugin导入的数据
     * @property pluginData Json示例
     * @property file Jar的文件
     * @constructor PluginImportData
     */
    private class PluginImportData(
        @JvmField
        val pluginData: BeanPluginInfo,
        @JvmField
        val file: File
    )

    companion object {
        /**
         * 返回文件夹内指定后辍的文件并包装为PluginLoadData
         * @param f FileUtil
         * @return Seq<PluginLoadData>
         */
        @JvmStatic
        internal fun resultPluginData(f: FileUtils): Seq<PluginLoadData> {
            val jarFileList = Seq<File>()
            val list = f.fileList
            for (file in list) {
                if (file.name.endsWith(".jar") || file.name.endsWith(".zip")) {
                    jarFileList.add(file)
                }
            }
            return PluginsLoad().loadPlugin(jarFileList)
        }

        @JvmStatic
        internal fun addPluginClass(
            name: String,
            internalName: String,
            author: String,
            description: String,
            version: String,
            main: Plugin,
            mkdir: Boolean,
            skip: Boolean,
            list: Seq<PluginLoadData>
        ) {
            list.add(PluginLoadData(name, internalName, author, description, version, main, mkdir, skip))
        }
    }
}