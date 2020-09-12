package com.example.udplesson

/* 工具：使用tcp/upd test tool -> 選擇 tcp server and udp , upd : 設LocalPort = 8008 , TargetPort = 8009 ->按 connect進行連線
udp , 它不像tcp 要知道對方的網址, 它是可以不管對方IP的(已確認）, 只要兩手機做同網段就可以接，也就連同一個ssid
它們不用連線, 只做監聽的動作, 所以無關伺服器和客戶端
此測試程式是固定1s時間去送出"This is UDP Server" 結對方的 （目前只支援ascii)
目前只接收ascii 無法接收hex code
sendclose / mcloseBTn 呼叫都是同一個函數都是close 所以根本沒什麼差別

 senddata -> mUDPBroadCast.open(SEND_PORT, DEST_PORT)     //SEND_PORT = lOCAL_PORT 功能
 receive ->  mUDPBroadCaster.open(LOCAL_PORT, DEST_PORT)     //設定socket 用

 */
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.DatagramPacket


class MainActivity : AppCompatActivity(), View.OnClickListener {
    val TAG: String = "myTag"
    val LOCAL_PORT: Int = 8009 // 12348 //8009  （有用到, 對方要送資料給)
    val DEST_PORT: Int = 8008    //目的是對方 (有用到, 我送資料給對方）
    val SEND_PORT: Int = 8070    // 指的是自己的local  （好像沒用到）

    lateinit var mRecvBtn: Button
    lateinit var mSendBtn: Button
    lateinit var mCloseBtn: Button
    lateinit var mSendCloseBtn: Button
    lateinit var mScrollView: ScrollView
    lateinit var mLogTx: TextView
    var isClosed: Boolean = false
    lateinit var mUDPBroadCaster: UDPBroadcaster
    lateinit var mUDPBroadCast: UDPBroadcaster
    var sendBuffer: String = "This is UDP Server"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mUDPBroadCaster = UDPBroadcaster(this)
        mUDPBroadCast = UDPBroadcaster(this)

        initView()
        initEvent()
    }


    private fun initEvent() {
        mRecvBtn.setOnClickListener(this)
        mCloseBtn.setOnClickListener(this)
        mSendBtn.setOnClickListener(this)
        mSendCloseBtn.setOnClickListener(this)
    }

    private fun initView() {
        mRecvBtn = findViewById(R.id.btn_receive) as Button
        mCloseBtn = findViewById(R.id.btn_close) as Button                  //結束
        mSendBtn = findViewById(R.id.btn_send) as Button
        mSendCloseBtn = findViewById(R.id.btn_send_close) as Button         //傳送結束
        mScrollView = findViewById(R.id.scrollView) as ScrollView
        mLogTx = findViewById(R.id.log) as TextView

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_receive -> recvUDPBroadcast()
            R.id.btn_close -> cancelRecv()
            R.id.btn_send -> sendUDPBroadcast()
            R.id.btn_send_close -> cancelRecv()
        }
    }

    private fun closeUDPBroadcast() {
        isClosed = true
    }


    private fun cancelRecv() {
        isClosed = true
    }

    private fun recvUDPBroadcast() {
        isClosed = false
        mUDPBroadCaster.open(LOCAL_PORT, DEST_PORT)     //設定socket 用
        var buffer: ByteArray = kotlin.ByteArray(1024)  //定義一定抓取的容量大小
        val packet = DatagramPacket(buffer, buffer.size)     // 定義容器packet 資料抓到後放在buffer內
        println("packet = $packet")

        Thread(Runnable {
            while (!isClosed) {
                try {
                    Thread.sleep(500) //500ms延时
                } catch (e: Exception) {
                    println("slfdsflsdlfsdflsdfsdfdsfsdf")
                    e.printStackTrace()
                }
                val a = mUDPBroadCaster.recvPacket(buffer) //接收广播 (一直在等值出現呢?) 卡死在這
                val data: String = String(packet.data)
                println(data)
                addLog("$TAG data: $data")
                addLog("$TAG addr: ${packet.address}")
                addLog("$TAG port: ${packet.port}")
            }
            mUDPBroadCaster.close() //退出接收广播
        }).start()              //再啟動一次
    }

    //=====================================================
    private fun sendUDPBroadcast() {
        isClosed = false
        mUDPBroadCast.open(SEND_PORT, DEST_PORT) //打开广播
  //      val buffer: ByteArray = sendBuffer.toByteArray()            //資料放在buffer送
        val buffer = ByteArray(7)                       // 16進制傳送
        Thread(Runnable {
            while (!isClosed) {
                try {
                    Thread.sleep(500) //500ms 延时
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                /* 模擬16進制傳送 已確定成功了, 其實它跟 tcp 的傳法有點像*/
                buffer[0] = 0x55
                buffer[1] = 0x44
                buffer[2] = 0x00
                buffer[3] = 0x02
                buffer[4] = 0x0a
                buffer[5] = 0xfa-256
                buffer[6] = 0x90-256

                mUDPBroadCast.sendPacket(buffer) //发送广播包
                addLog("$TAG data: ${String(buffer)}")              //累積顯示
            }
            mUDPBroadCast.close() //关闭广播
        }).start()
    }


    //================================================
    private fun addLog(log: String) {
        // 底下這個其實是顯示換行的做法, 在最後字串找不到\n 就把它加上而已
        var mLog: String = log
        if (mLog.endsWith("\n").not()) {
            mLog += "\n"
        }
        //=========================
        mScrollView.post(Runnable {
            mLogTx.append(mLog)
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        })
    }
}