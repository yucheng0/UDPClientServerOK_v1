package com.example.udplesson

import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import android.util.Log
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.*

class UDPBroadcaster(var mContext: Context) {
    private val TAG:String = UDPBroadcaster::class.java.simpleName
    private var mDestPort = 0
    private var mSocket: DatagramSocket? = null
    private val ROOT_PATH:String = android.os.Environment.getExternalStorageDirectory().path
    /**
     * 打开
     */
    fun open(localPort: Int, destPort: Int): Boolean {
        mDestPort = destPort
        try {
            mSocket = DatagramSocket(localPort)
            mSocket?.broadcast = true
            mSocket?.reuseAddress = true
            return true
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 关闭
     */
    fun close(): Boolean {
        if (mSocket != null && mSocket?.isClosed?.not() as Boolean) {
            mSocket?.close()
        }
        return true
    }

    /**
     * 发送广播包
     */
    fun sendPacket(buffer: ByteArray): Boolean {
        try {
            val addr = getBroadcastAddress(mContext)
            Log.d("$TAG addr",addr.toString())
            val packet = DatagramPacket(buffer, buffer.size)
            packet.address = addr
            packet.port = mDestPort
            mSocket?.send(packet)
            return true
        } catch (e1: UnknownHostException) {
            e1.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 接收广播
     */
    fun recvPacket(buffer: ByteArray): Boolean {
        val packet = DatagramPacket(buffer, buffer.size)
        try {
             mSocket?.receive(packet)       //卡在這裡有東西才會出去
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    companion object {
        /**
         * 获取广播地址
         */
        fun getBroadcastAddress(context: Context): InetAddress {
            if (isWifiApEnabled(context)) { //判断wifi热点是否打开
                return InetAddress.getByName("192.168.43.255")  //直接返回
            }
            val wifi: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcp: DhcpInfo = wifi.dhcpInfo ?: return InetAddress.getByName("255.255.255.255")
            val broadcast = (dhcp.ipAddress and dhcp.netmask) or dhcp.netmask.inv()
            val quads = ByteArray(4)
            for (k in 0..3) {
                quads[k] = ((broadcast shr k * 8) and 0xFF).toByte()
            }
            return InetAddress.getByAddress(quads)
        }

        /**
         * check whether the wifiAp is Enable
         */
        private fun isWifiApEnabled(context: Context): Boolean {
            try {
                val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val method = manager.javaClass.getMethod("isWifiApEnabled")
                return method.invoke(manager) as Boolean
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            return false
        }
    }
}