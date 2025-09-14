package net.mizukilab.pit.license

import lombok.SneakyThrows
import pku.yim.license.MagicLicense
import kotlin.concurrent.Volatile
import kotlin.system.exitProcess

object MagicLoader {
    private val lock = Object()
    private var exception: Exception? = null
    private var magicLicense: MagicLicense? = null

    @Volatile
    private var isLoaded = false

    @JvmStatic
    fun load() {
        Thread {
            try {
                synchronized(lock) {
                    isLoaded = true
                    lock.notifyAll()
                }
            }catch (e:Exception){
                exception = e
            }
        }.start()
    }


    @JvmStatic
    @SneakyThrows
    @Synchronized
    fun ensureIsLoaded() {
        if (!isLoaded) {
            synchronized(lock) {
                lock.wait()
            }
            if (exception != null) {
                exception!!.printStackTrace()
                exitProcess(0)
            }
        }
        magicLicense?.loadClass(System.getProperty("env"))?.getDeclaredMethod(System.getProperty("ent"))?.invoke(null)

    }
}
