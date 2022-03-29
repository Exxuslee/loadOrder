package com.exxus.miimes

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import jcifs.smb.SmbFileOutputStream
import org.json.JSONException
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity(), OnItemClickListener, OnLongClickListener {
    private lateinit var adapterRecyclerAdapter: RecyclerAdapter
    private lateinit var spinnerKB: Spinner
    private lateinit var spinnerKN: Spinner
    private lateinit var usersList: RecyclerView
    private var myHandler = Handler()

    private lateinit var currentBake: TextView
    private lateinit var currentReglament: TextView
    private lateinit var currentPech: TextView
    private lateinit var curBunker1: ImageView
    private lateinit var curBunker2: ImageView
    private lateinit var curBunker3: ImageView
    private lateinit var curBunker1niz: ImageView
    private lateinit var curBunker2niz: ImageView
    private lateinit var curBunker3niz: ImageView
    private lateinit var btnUpload: Button
    private lateinit var btnPechPP: Button
    private lateinit var btnPechP: Button
    private lateinit var btnPechMM: Button
    private lateinit var btnPechM: Button
    private lateinit var btnUpdReg: Button
    private lateinit var gradientBunker1: GradientDrawable
    private lateinit var gradientBunker2: GradientDrawable
    private lateinit var gradientBunker3: GradientDrawable


    private var addr: String = BuildConfig.addr
    private var login: String = BuildConfig.login
    private var passw: String = BuildConfig.passw

    private var uploadState : Boolean = true
    private var curPech : Int = 0
    private var curRegl: String = "0"
    private var currentBatarrey : Int = 0
    private lateinit var obj: JSONObject
    private lateinit var vibrator: Vibrator
    val users = mutableListOf<User>()

    private lateinit var valueAnimator1 : ValueAnimator
    private lateinit var valueAnimator2 : ValueAnimator
    private lateinit var valueAnimator3 : ValueAnimator
    private lateinit var valueAnimator4 : ValueAnimator
    private lateinit var valueAnimator5 : ValueAnimator
    private lateinit var valueAnimator6 : ValueAnimator

    private lateinit var machineOnLine: Array<out String>

    var filename = ""
    private val filepath = ""
    internal var myExternalFile: File?=null

    @SuppressLint("MissingPermission")
    fun refresh(){
        curRegl = obj.get(curPech.toString()).toString()

        currentPech.text = curPech.toString()
        currentBake.text = curRegl
        currentReglament.text = when (curRegl){
            "3" -> "3 - 1 - 2"
            "2.75" -> "3 (н/г) - 1 - 2"
            "2.5" -> "3 (н/г) - 1 - 2"
            "2" -> "2 - 1"
            "0" -> "печь в ремонте"
            else -> "неучтённый регламент"
        }

        btnUpdReg.text = getString(
            R.string.updRegl,
            obj.get("date")
        )
        btnUpload.text = getString(
            R.string.uploadIn,
            spinnerKN.selectedItem.toString(),
            "набираю\nбункера",
            curPech
        )

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(50)
        }

        users.forEach { data ->
            data.color = when (data.username) {
                curPech -> Color.GRAY
                else -> Color.BLACK
            }
        }


        if (curRegl == "0") {
            gradientBunker1.colors = intArrayOf (Color.GREEN, Color.RED)
            gradientBunker2.colors = intArrayOf (Color.GREEN, Color.RED)
            gradientBunker3.colors = intArrayOf (Color.GREEN, Color.RED)
            gradientBunker1.setGradientCenter(0f, 1f)
            gradientBunker2.setGradientCenter(0f, 1f)
            gradientBunker3.setGradientCenter(0f, 1f)
        }
        else {
            gradientBunker1.colors = intArrayOf (Color.GREEN, Color.LTGRAY)
            gradientBunker2.colors = intArrayOf (Color.GREEN, Color.LTGRAY)
            gradientBunker1.setGradientCenter(0f, 1f)
            gradientBunker2.setGradientCenter(0f, 1f)
            gradientBunker3.colors = intArrayOf(Color.LTGRAY, Color.GREEN, Color.RED)
            gradientBunker3.setGradientCenter(0.0F, 3 - curRegl.toFloat())
        }

        adapterRecyclerAdapter.notifyDataSetChanged()
        btnUpload.setBackgroundColor(Color.LTGRAY)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(findViewById(R.id.toolbar))

        vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        btnUpload = findViewById(R.id.btnUpload)
        btnPechPP = findViewById(R.id.btnxUx)
        btnPechMM = findViewById(R.id.btnxDx)
        btnPechP = findViewById(R.id.btnxxU)
        btnPechM = findViewById(R.id.btnxxD)
        btnUpdReg = findViewById(R.id.btnxUpdReg)

        spinnerKB = findViewById(R.id.spinnerKB)
//        spinnerKM = findViewById(R.id.spinnerKM)
        spinnerKN = findViewById(R.id.spinnerKN)

        currentBake = findViewById(R.id.CurrentBake)
        currentReglament = findViewById(R.id.currentReglament)
        currentPech = findViewById(R.id.currentPech)

        curBunker1 = findViewById(R.id.curBunker1)
        curBunker2 = findViewById(R.id.curBunker2)
        curBunker3 = findViewById(R.id.curBunker3)
        curBunker1niz = findViewById(R.id.imageView)
        curBunker2niz = findViewById(R.id.imageView2)
        curBunker3niz = findViewById(R.id.imageView3)

        gradientBunker1 = GradientDrawable()
        gradientBunker2 = GradientDrawable()
        gradientBunker3 = GradientDrawable()
//        valueAnimator1 = ValueAnimator()
//        valueAnimator2 = ValueAnimator()
//        valueAnimator3 = ValueAnimator()
        valueAnimator1 = ValueAnimator.ofFloat(1f, 0f)
        valueAnimator2 = ValueAnimator.ofFloat(1f, 0f)
        valueAnimator3 = ValueAnimator.ofFloat(1f, 0f)
        valueAnimator4 = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator5 = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator6 = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator1.interpolator = LinearInterpolator()
        valueAnimator1.duration = 3000
        valueAnimator1.startDelay = 0
        valueAnimator2.interpolator = LinearInterpolator()
        valueAnimator2.duration = 3000
        valueAnimator2.startDelay = 0
        valueAnimator3.interpolator = LinearInterpolator()
        valueAnimator3.duration = 3000
        valueAnimator3.startDelay = 0
        valueAnimator4.interpolator = LinearInterpolator()
        valueAnimator4.duration = 3000
        valueAnimator4.startDelay = 3000
        valueAnimator5.interpolator = LinearInterpolator()
        valueAnimator5.duration = 3000
        valueAnimator5.startDelay = 6000
        valueAnimator6.interpolator = LinearInterpolator()
        valueAnimator6.duration = 3000
        valueAnimator6.startDelay = 0

        gradientBunker1.apply {
            gradientType = GradientDrawable.SWEEP_GRADIENT
            shape = GradientDrawable.RECTANGLE
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            setStroke(2, Color.parseColor("#4B5320"))
            if (curRegl == "0") {
                colors = intArrayOf(
                    Color.GREEN,
                    Color.RED
                )
                setGradientCenter(0.0F, 1.0F)
                //               valueAnimator1 = ValueAnimator.ofFloat(1f, 1f)
            }
            else {
                colors = intArrayOf(
                    Color.GREEN,
                    Color.LTGRAY
                )
                setGradientCenter(0.0F, 1.0F)
                //valueAnimator1 = ValueAnimator.ofFloat(1f, 0f)
            }
            valueAnimator1.addUpdateListener {
                val value = it.animatedValue as Float
                setGradientCenter(0.0F, value)
            }
            valueAnimator4.addUpdateListener {
                val value = it.animatedValue as Float
                setGradientCenter(0.0F, value)
            }
        }
        gradientBunker2.apply {
            gradientType = GradientDrawable.SWEEP_GRADIENT
            shape = GradientDrawable.RECTANGLE
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            setStroke(2, Color.parseColor("#4B5320"))
            if (curRegl == "0") {
                colors = intArrayOf(
                    Color.GREEN,
                    Color.RED
                )
                setGradientCenter(0.0F, 1.0F)
  //              valueAnimator2 = ValueAnimator.ofFloat(1f, 1f)
            }
            else {
                colors = intArrayOf(
                    Color.GREEN,
                    Color.LTGRAY
                )
                setGradientCenter(0.0F, 1.0F)
//                valueAnimator2 = ValueAnimator.ofFloat(1f, 0f)
            }
            valueAnimator2.addUpdateListener {
                val value = it.animatedValue as Float
                setGradientCenter(0.0F, value)
            }
            valueAnimator5.addUpdateListener {
                val value = it.animatedValue as Float
                setGradientCenter(0.0F, value)
            }
        }
        gradientBunker3.apply {
            if (curRegl == "0") {
                colors = intArrayOf(
                    Color.GREEN,
                    Color.RED
                )
                setGradientCenter(0.0F, 1.0F)
            }
            else {
                colors = intArrayOf(
                    Color.LTGRAY,
                    Color.GREEN,
                    Color.RED
                )
                setGradientCenter(0.0F, 1.0F)
            }
            gradientType = GradientDrawable.SWEEP_GRADIENT
            shape = GradientDrawable.RECTANGLE
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            setStroke(2, Color.parseColor("#4B5320"))
            setGradientCenter(0.0F, 3 - curRegl.toFloat())
 //           valueAnimator3 = ValueAnimator.ofFloat(3 - curRegl.toFloat(), 1f)
            valueAnimator3.addUpdateListener {
                colors = intArrayOf(
                    Color.GREEN,
                    Color.LTGRAY
                )
                val value = it.animatedValue as Float
                setGradientCenter(0.0F, value)
            }
        }
        curBunker3.setImageDrawable(gradientBunker3)
        curBunker2.setImageDrawable(gradientBunker2)
        curBunker1.setImageDrawable(gradientBunker1)

        usersList = findViewById<RecyclerView>(R.id.recyclerView)
        usersList.layoutManager = LinearLayoutManager(this)
        adapterRecyclerAdapter = RecyclerAdapter(users, this, this)
        usersList.adapter = adapterRecyclerAdapter

        fun readReglament() {
            myExternalFile = File(getExternalFilesDir(filepath), filename)
            if(filename.trim()!=""){
                val fileInputStream =FileInputStream(myExternalFile)
                val inputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val stringBuilder: StringBuilder = StringBuilder()
                var text: String? = null
                while ({ text = bufferedReader.readLine(); text }() != null) {
                    stringBuilder.append(text)
                }
                fileInputStream.close()

                try{
                    obj = JSONObject(stringBuilder.toString())
                    Toast.makeText(
                        applicationContext,
                        "Регламент $filename считан успешно!",
                        Toast.LENGTH_SHORT
                    ).show()
                    refresh()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                    try{
                        val jsonString = application.assets.open(filename).bufferedReader().use{
                            it.readText()
                        }
                        obj = JSONObject(jsonString)
                        Toast.makeText(
                            applicationContext,
                            "Загружен регламент $filename по умолчанию!",
                            Toast.LENGTH_LONG
                        ).show()
                        refresh()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        spinnerKB.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                machineOnLine = when (position) {
                    0 -> resources.getStringArray(R.array.KN01)
                    1 -> resources.getStringArray(R.array.KN02)
                    2 -> resources.getStringArray(R.array.KN03)
                    3 -> resources.getStringArray(R.array.KN05)
                    4 -> resources.getStringArray(R.array.KN06)
                    5 -> resources.getStringArray(R.array.KN07)
                    6 -> resources.getStringArray(R.array.KN08)
                    7 -> resources.getStringArray(R.array.KN09)
                    else -> resources.getStringArray(R.array.KN00)
                }
                filename = when (position){
                    0 -> "КБ-1.txt"
                    1 -> "КБ-2.txt"
                    2 -> "КБ-3.txt"
                    3 -> "КБ-5.txt"
                    4 -> "КБ-6.txt"
                    5 -> "КБ-7.txt"
                    6 -> "КБ-8.txt"
                    7 -> "КБ-9.txt"
                    else -> "_КБ-0.json"
                }
                curPech = when (position){
                    0 -> 101
                    1 -> 201
                    2 -> 301
                    3 -> 501
                    4 -> 604
                    5 -> 701
                    6 -> 801
                    7 -> 901
                    else -> 101
                }

                currentBatarrey = when (position){
                    0 -> 1
                    1 -> 2
                    2 -> 3
                    3 -> 5
                    4 -> 6
                    5 -> 7
                    6 -> 8
                    7 -> 9
                    else -> 1
                }
                users.clear()
                adapterRecyclerAdapter.notifyDataSetChanged()

                readReglament()
                val adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item, machineOnLine
                )
                spinnerKN.adapter = adapter
                spinnerKN.performClick()
            }


        }

        spinnerKN.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                btnUpload.text = getString(
                    R.string.uploadIn,
                    spinnerKN.selectedItem.toString(),
                    "набираю\nбункера",
                    curPech
                )
            }
        }

        btnUpdReg.setOnClickListener {
            readReglament()
//            refresh()
        }

        btnUpdReg.setOnLongClickListener{
            val buffer = ByteArray(1024)

            fun read2Samba(kb: String): Boolean {
                try {
                    val url = "smb://${addr}/db/reglament/${kb}.json"
                    Log.d(TAG, url)

                    val auth = NtlmPasswordAuthentication(
                        null,
                        login,
                        passw
                    )
                    val file = SmbFile(url, auth)
                    val input: SmbFileInputStream
                    input = SmbFileInputStream(file)
                    var bytesRead = 0;
                    do {
                        bytesRead = input.read(buffer)
                        // here you have "bytesRead" in buffer array
                    } while (bytesRead > 0);

                    obj = JSONObject(String(buffer))


                    Log.d(TAG, obj.toString())
                    return true;
                } catch (e: Exception) {
                    e.printStackTrace();
                    return false;
                }
            }

            val toastYes: Toast = Toast.makeText(
                this,
                "Регламент $filename обновлён c сервера",
                Toast.LENGTH_SHORT
            )
            val toastNo: Toast =
                Toast.makeText(this, "Сервер не доступен", Toast.LENGTH_SHORT)


            object : Thread() {
                override fun run() {
                    if (read2Samba(spinnerKB.selectedItem.toString())) {

                        myExternalFile = File(
                            getExternalFilesDir(filepath),
                            spinnerKB.selectedItem.toString() + ".json"
                        )
                        val fileOutputStream: FileOutputStream
                        try {
                            fileOutputStream = FileOutputStream(myExternalFile)
                            fileOutputStream.write(buffer)
                            fileOutputStream.close()
                            toastYes.show()
                            myHandler.post( Runnable {
                                refresh()
                            })
                        } catch (e: FileNotFoundException){
                            Log.d(TAG, e.toString())
                        }catch (e: NumberFormatException){
                            Log.d(TAG, e.toString())
                        }catch (e: IOException){
                            Log.d(TAG, e.toString())
                        }catch (e: Exception){
                            Log.d(TAG, e.toString())
                        }
                    }
                    else toastNo.show()
                }
            }.start()
            true
        }

        btnUpload.setOnClickListener {
            val toastOn: Toast = Toast.makeText(this,"Печь $curPech загрузка", Toast.LENGTH_SHORT)
            val toastOff: Toast = Toast.makeText(this,"Печь $curPech выгрузка", Toast.LENGTH_SHORT)
            val toastNo: Toast = Toast.makeText(this, "Сервер не доступен", Toast.LENGTH_SHORT)
            fun save2Samba(text: String, selectedUZV: String): Boolean {
                try {
                    val url = "smb://${addr}/db/$selectedUZV/uzv.txt"
                    Log.d(TAG, url)
                    val auth = NtlmPasswordAuthentication(
                        null,
                        login,
                        passw
                    )
                    Log.d(TAG, auth.toString())
                    val file = SmbFile(url, auth);
                    val out =  SmbFileOutputStream(file);
                    out.write(text.toByteArray())
                    out.flush()
                    out.close()
  //                  Log.d(TAG, spinnerKB.selectedItem.toString())
                    return true;
                } catch (e: Exception) {
                    e.printStackTrace();
                    return false;
                }
            }


            if (uploadState) {
                btnUpload.setBackgroundColor(Color.YELLOW)
                btnPechM.isEnabled = false
                btnPechMM.isEnabled = false
                btnPechP.isEnabled = false
                btnPechPP.isEnabled = false
                btnUpdReg.isEnabled = false
                spinnerKB.isEnabled = false
                spinnerKN.isEnabled = false
                btnUpload.text = getString(
                    R.string.uploadIn,
                    spinnerKN.selectedItem.toString(),
                    "загружаю в печь",
                    curPech
                )

                // Анимация загрузки
                when (curRegl) {
                    "0" -> {
                        Toast.makeText(this, "Обновите регламент!", Toast.LENGTH_SHORT).show()
                    }
                    "2" -> {
                        valueAnimator1.start()
                        valueAnimator2.start()
                    }
                    else -> {
                        valueAnimator1.start()
                        valueAnimator2.start()


                        valueAnimator3 = ValueAnimator.ofFloat(1f, 3 - curRegl.toFloat())
                        valueAnimator3.interpolator = LinearInterpolator()
                        val tempTime = (curRegl.toFloat()*1000-2000)*3
                        valueAnimator3.duration = tempTime.toLong()
                        valueAnimator3.startDelay = 0
                        valueAnimator3.addUpdateListener {
                            gradientBunker3.colors = intArrayOf(
                                Color.GREEN,
                                Color.LTGRAY
                            )
                            val value = it.animatedValue as Float
                            gradientBunker3.setGradientCenter(0.0F, value)
                        }
                        valueAnimator3.start()

                    }

                }

                object : Thread() {
                    override fun run() {
                        if (save2Samba(
                                curPech.toString(),
                                spinnerKN.selectedItem.toString()
                            ))
                            toastOn.show()
                        else  toastNo.show()
                    }
                }.start()

                btnUpload.isEnabled = false
                object : CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        btnUpload.text = getString(
                            R.string.uploadIn,
                            spinnerKN.selectedItem.toString(),
                            "загружаю\n",
                            millisUntilFinished / 1000
                        )
                    }
                    override fun onFinish() {
                        btnUpload.isEnabled = true
                        btnUpload.text = getString(
                            R.string.uploadIn,
                            spinnerKN.selectedItem.toString(),
                            "загружаю в печь",
                            curPech
                        )
                    }
                }.start()
            } else {
                var updateUser: Boolean = true
                val unixTime: Long = System.currentTimeMillis()/1000L

                var position = 0
                var addPosition = true
                users.forEach { user ->
                    if (user.username == curPech) {
                        user.timestamp = unixTime
                        user.color = Color.GREEN
                        updateUser = false
                        addPosition = false
                    } else if (addPosition) position++
                }
                if (updateUser) users.add(User(curPech, unixTime, Color.GREEN))
                users.sortBy { it.timestamp }
                adapterRecyclerAdapter.notifyDataSetChanged()
                btnUpload.setBackgroundColor(Color.GREEN)
                btnUpload.text = getString(
                    R.string.uploadIn,
                    spinnerKN.selectedItem.toString(),
                    "загрузил в печь",
                    curPech
                )

                btnUpload.isEnabled = false
                val millisInFuture = when (curRegl) {
                    "0" -> 1000
                    "2" -> 6300
                    else -> 9300
                }
                object : CountDownTimer(millisInFuture.toLong(), 3000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                        btnPechM.isEnabled = true
                        btnPechMM.isEnabled = true
                        btnPechP.isEnabled = true
                        btnPechPP.isEnabled = true
                        btnUpdReg.isEnabled = true
                        spinnerKB.isEnabled = true
                        spinnerKN.isEnabled = true
                        btnUpload.isEnabled = true

                        if (position == users.size - 1) {
                            curPech = users[0].username
                            users[0].color = Color.LTGRAY
                        } else {
                            curPech = users[position].username
                            users[position].color = Color.LTGRAY
                        }
                        refresh()
                    }
                }.start()



                object : Thread() {
                    override fun run() {
                        val startBake = (curPech/100)*100
                        if (save2Samba(
                                startBake.toString(),
                                spinnerKN.selectedItem.toString()
                            ))
                            toastOff.show()
                        else  toastNo.show()
                    }
                }.start()

                // Анимация выгрузки
                when (curRegl) {
                    "0" -> {
                        Toast.makeText(this, "Обновите регламент!", Toast.LENGTH_SHORT).show()
                    }
                    "2" -> {
                        valueAnimator4.startDelay=3000
                        valueAnimator4.start()
                        valueAnimator5.startDelay=0
                        valueAnimator5.start()

                    }
                    else -> {
                        valueAnimator4.startDelay = 3000
                        valueAnimator4.start()
                        valueAnimator5.startDelay = 6000
                        valueAnimator5.start()
                        valueAnimator6 = ValueAnimator.ofFloat(3 - curRegl.toFloat(), 1f)
                        valueAnimator6.interpolator = LinearInterpolator()
                        val tempTime = (curRegl.toFloat()*1000-2000)*3
                        valueAnimator6.duration = tempTime.toLong()
                        valueAnimator6.startDelay = 0
                        valueAnimator6.addUpdateListener {
                            gradientBunker3.colors = intArrayOf(
                                Color.GREEN,
                                Color.LTGRAY
                            )
                            val value = it.animatedValue as Float
                            gradientBunker3.setGradientCenter(0.0F, value)
                        }
                        valueAnimator6.start()
                    }

                }

            }
            uploadState = !uploadState

            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(50)
            }
   //         refresh()
        }

        btnPechPP.setOnClickListener {
            curPech += 10
            if (currentBatarrey > 4 && curPech - (curPech/100)*100 > 72) curPech = currentBatarrey*100 + curPech%10
            if (currentBatarrey < 4 && curPech - (curPech/100)*100 > 85) curPech = currentBatarrey*100 + curPech%10
            refresh()
        }

        btnPechMM.setOnClickListener {
            if (curPech%100 > 10) curPech -= 10
            else {
                if (currentBatarrey > 4 ) curPech = currentBatarrey*100 + 72
                if (currentBatarrey < 4 ) curPech = currentBatarrey*100 + 85
            }
            refresh()
        }

        btnPechP.setOnClickListener {
            ++curPech
            if (curPech%10 == 0) ++curPech
            if (currentBatarrey > 4 && curPech - (curPech/100)*100 > 72) curPech = currentBatarrey*100 + 1
            if (currentBatarrey < 4 && curPech - (curPech/100)*100 > 85) curPech = currentBatarrey*100 + 1
            refresh()
        }

        btnPechM.setOnClickListener {
            --curPech
            if (curPech % 10 == 0) --curPech
            if (currentBatarrey > 4 && curPech - (curPech / 100) * 100 > 72) curPech =
                currentBatarrey * 100 + 72
            if (currentBatarrey < 4 && curPech - (curPech / 100) * 100 > 85) curPech =
                currentBatarrey * 100 + 85
            refresh()
        }

    }

    companion object {
        private const val TAG = "ExxActivity"
    }

    override fun onItemClicked(user: User, position: Int) {
        if (!uploadState) return
        curPech = user.username
        refresh()
    }

    override fun onItemLongClicked(user: User, position: Int) {
        if (!uploadState) return
        Toast.makeText(
            applicationContext,
            "Печь №${user.username} удалена из списка.",
            Toast.LENGTH_SHORT
        ).show()
        users.removeAt(position)
        refresh()
    }
}







