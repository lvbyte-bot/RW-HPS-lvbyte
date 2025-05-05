package net.rwhps.server.dependent.redirections

import net.rwhps.asm.api.listener.RedirectionListener
import net.rwhps.asm.api.replace.RedirectionReplace
import net.rwhps.asm.data.MethodTypeInfoValue
import net.rwhps.asm.data.manager.ListenerRedirectionsDataManager
import net.rwhps.asm.data.manager.RemoveRedirectionsDataManager
import net.rwhps.asm.data.manager.ReplaceRedirectionsDataManager
import net.rwhps.asm.func.Find
import net.rwhps.asm.redirections.replace.def.BasicDataRedirections

/**
 * 用来实现各个 ASM Redirections 服务
 * 进行统一调用
 *
 * @author Dr (dr@der.kim)
 */
interface MainRedirections {
    // 强制实现
    fun register()

    fun addAllReplace(classPath: String) {
        ReplaceRedirectionsDataManager.addAllMethodReplace(classPath)
    }

    fun addAllReplace(classPath: String, removeSync: Boolean) {
        ReplaceRedirectionsDataManager.addAllMethodReplace(classPath, removeSync)
    }

    fun addAllReplace(classFind: Find<String, Boolean>) {
        ReplaceRedirectionsDataManager.addAllMethodReplace(classFind)
    }

    fun redirectR(methodTypeInfoValue: MethodTypeInfoValue) {
        ReplaceRedirectionsDataManager.addPartialMethodReplace(methodTypeInfoValue, BasicDataRedirections.NULL)
    }

    fun redirectR(methodTypeInfoValue: MethodTypeInfoValue, redirection: RedirectionReplace) {
        ReplaceRedirectionsDataManager.addPartialMethodReplace(methodTypeInfoValue, redirection)
    }

    fun redirectL(methodTypeInfoValue: MethodTypeInfoValue) {
        ListenerRedirectionsDataManager.addPartialMethodListener(methodTypeInfoValue, null)
    }

    fun redirectL(methodTypeInfoValue: MethodTypeInfoValue, redirection: RedirectionListener?) {
        ListenerRedirectionsDataManager.addPartialMethodListener(methodTypeInfoValue, redirection)
    }

    fun redirectRemove(methodTypeInfoValue: MethodTypeInfoValue) {
        RemoveRedirectionsDataManager.addPartialMethodRemove(methodTypeInfoValue)
    }
}
// 我修改了你的梦