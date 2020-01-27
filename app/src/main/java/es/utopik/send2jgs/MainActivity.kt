package es.utopik.send2jgs


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import es.utopik.jgsutils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.name
        const val EMAIL_SENDTO = "info@jaumegelabert.com"
        const val SMTP_SERVER = "mail.jaumegelabert.com"
        const val SMTP_USER = "test@utopik.es"
        const val SMTP_PWD = "w8NWXQQ7"
        const val SMTP_PORT = "587"
        const val CNT_VERSION = "27.01.2020"

//        const val EXTRA_SUBJECT = "subject"
    }

    val CHANNEL_ID = "Send2JGS"


    lateinit var progressDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()

        //================================ INTENT d'EnTRADA
        var subject = intent?.getStringExtra(Intent.EXTRA_SUBJECT) ?: "*DEBUG SUBJECT*"
/*
        if (subject==null){
            subject = "*DEBUG SUBJECT*"
        }
*/
//        subject = "Sent2JGS: " + subject

        var text = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: "*DEBUG BODY*"
        //================================ INTENT d'EnTRADA

        txtSUBJECT.setText(subject)
        txtBODY.setText(text)

        txtSUBJECT.requestFocus()

        //debug
        //DBG_getListOfIntentExtras()
    }

    // actionBar --> backButton
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * --..........................
     */
    fun initUI() {
        setSupportActionBar(toolbar)

        //show back button
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
//            setIcon(R.mipmap.ic_launcher_round)
        }

        //setTitle(getApplicationName(this) + " v$CNT_VERSION")


        lbl_emailSendTo.text = "SendTo: $EMAIL_SENDTO (v$CNT_VERSION)"

//        fabSend.setOnClickListener { view -> fabSend_onClick()}
//
//        fabShare.setOnClickListener { view -> fabShare_onClick()}

        btnSEND.setOnClickListener { fabSend_onClick() }
        btnItentSendTo.setOnClickListener { fabShare_onClick() }


        txtDebug.setOnLongClickListener {
            alertMsg(processarBody(txtBODY.text.toString()))
            true
        }


        img_clear_subject.setOnClickListener {
            txtSUBJECT.apply {
                setText("")
                requestFocus()
            }
        }
        img_clear_subject.setOnLongClickListener {
            txtSUBJECT.apply {
                setText("Sent2JGS ")
                requestFocus()
            }
            true
        }


        img_clear_body.setOnClickListener {
            txtBODY.apply {
                setText("")
                requestFocus()
            }
        }


        img_ThumbsUP.setOnClickListener {
            finish()
        }

        //23.01.2020
        // per poder enviar NOTIFICACIONS!
        createNotificationChannel()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menuItem_ENVIAR -> {
                fabSend_onClick()
                true
            }

            R.id.menuItem_COMPARTIR -> {
                fabShare_onClick()
                true
            }

            R.id.menuItem_TEST -> {
                var t = txtSUBJECT.text.toString()



                sendNotification("Send2JGS", "e-mail enviado: $t")
                true
            }

            //R.id.menuItem_SETTINGS -> true
            else -> {
                // pel cas del BackButton --> title = null
                item?.title?.apply {
                    alertMsg("FALTA onOptionsItemSelected `${this}`")
                }
//                if (item?.title != null) { // pel cas del BackButton
//                    alertMsg("FALTA onOptionsItemSelected `${item?.title}`")
//                }
                super.onOptionsItemSelected(item)
            }

        }


    }

    fun DBG_getListOfIntentExtras() {
        var intent = this.intent
        if (intent == null) {
            txtDebug.setText("itent = NULL")
        } else {
            var extras = intent.extras
            extras?.apply {
                var ss = ""
                for (key in this.keySet()) {
                    ss += "[$key]:\n${this.get(key)}\n\n"
                }
                txtDebug.setText(ss)
            }

        }
    }

    fun showProgressDialog(msg: String) {
        val builder = AlertDialog.Builder(this)

        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)

        val message = dialogView.findViewById<TextView>(R.id.lblProgressDialogMessage)
        message.text = msg
        builder.setView(dialogView)
        builder.setCancelable(false)
        this.progressDialog = builder.create()
        this.progressDialog.show()

    }

    fun hideProgressDialog() {
        this.progressDialog?.dismiss()
    }

    fun fabShare_onClick() {
        var uriText = "mailto:$EMAIL_SENDTO" +
                "?subject=" + Uri.encode(txtSUBJECT.text.toString()) +
                "&body=" + Uri.encode(txtBODY.text.toString());
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriText))


//            var uri = Uri.fromParts("mailto","info@jaumegelabert.com",null)

//            val intent = Intent(Intent.ACTION_SENDTO, uri)
//            intent.setType("text/plain")
//            intent.putExtra(Intent.EXTRA_EMAIL,"hola@hola.com")
//            intent.putExtra(Intent.EXTRA_SUBJECT,"*subject*")
//            intent.putExtra(Intent.EXTRA_TEXT,txtText.text)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            //finish()
        } else {
            snackbar("Can't handle this: ACTION_SENDTO", {})
//            val parentLayout: View = findViewById(android.R.id.content)
//            parentLayout.snackbar2("Can't handle this: ACTION_SENDTO")
//                Log.d(ImplicitIntentsActivity.TAG_LOG,"Can't handle this")
        }
    }


    /**
     * 14.01.2020 --> SendEmail directly
     * https://codingshiksha.com/kotlin/kotlin-android-send-smtp-email-with-awesome-maildroid-library-new-coding-shiksha/
     */
    fun fabSend_onClick() {

        if (!hayConexionAInternet()) {
            alertMsg("No hay conexión a Internet", "SIN INTERNET!!")
            return
        }


        var body = txtBODY.text.toString()
/*
        body.apply {
            if (this.startsWith("http",true)){
                this = "<a href=\"$this\">$this</a>"
            }
        }
*/
        body = processarBody(body)
        var subject = processarSubject(txtSUBJECT.text.toString(), body)


        subject = "Sent2JGS: " + subject
/*
       if (body.startsWith("http",true)){
            body = "<a href=\"$body\">$body</a>"
        }
*/
//        alertMsg(body,"BODY")
//        return


        this.showProgressDialog("Enviando e-mail...")

        try {
            sendEmail(EMAIL_SENDTO, EMAIL_SENDTO, subject, body)
        } catch (e: Exception) {
            alertMsg(e.message.toString())
        }

    }

    fun processarSubject(subject: String, body: String): String {
        var ret: String = subject


        if (body.contains("deezer.com")) {
            ret = "DEEZER>> $ret"
        } else if (body.contains("bandcamp.com")) {
            ret = "BANDCAMP>> $ret"
        }

        return ret

    }

    fun processarBody(body: String): String {
        var lines = body.lines()
        var ret: String = ""
        var s: String

        for (lin in lines) {
            if (lin.startsWith("http", true)) {
                s = "<a href=\"$lin\">$lin</a>"
            } else {
                s = lin
            }
            ret += "$s\n"
        }

/*
        lines.forEach(var s : String -> {
        })
            var s : String=""
            s = it
            if (it.startsWith("http:",true)){
                s = "<a href=\"$s\">$s</a>"
            }
            ret += "$ret\n"
        }
*/
        return ret

    }


    /**
     * 23.01.2020
     */
    fun sendEmail(sTo: String, sFrom: String, sSubject: String, sBody: String) {


        MaildroidX.Builder()
            .smtp(SMTP_SERVER)
            .smtpUsername(SMTP_USER)
            .smtpPassword(SMTP_PWD)
            .smtpAuthentication(true)
            .port(SMTP_PORT)
            .type(MaildroidXType.HTML)
            .to(sTo)
            .from(sFrom)
            .subject(sSubject)
            .body(sBody)
            //.attachment("")
            //or
            //.attachments() //List<String>

            .onCompleteCallback(object : MaildroidX.onCompleteCallback {
                override val timeout: Long = 3000
                override fun onSuccess() {
//                    alertMsg("MaildroidX",  "SUCCESS")
                    Log.d("$TAG - MaildroidX", "SUCCESS")

                    hideProgressDialog()

//                    snackbar(
//                        "Se ha enviado el e-mail :)",
//                        action = { finish() },
//                        duration = Snackbar.LENGTH_INDEFINITE
//                    )

                    var t: String = txtSUBJECT.text.toString()
                    sendNotification("Send2JGS", "e-mail enviado: `$t`")
//                    img_ThumbsUP.visibility = View.VISIBLE

                    //-------------------- img THUMBS UP
                    //RANDOM!!
                    var idx = (1..3).shuffled().first()
//                    var idx = ThreadLocalRandom.current().nextInt(1, 3)
                    var context = img_ThumbsUP.context
                    var idResIcon = img_ThumbsUP.getIdResourceByString("ic_thumbs_up" + idx)
//                    var idIcon = context.resources.getIdentifier(
//                        "ic_thumbs_up" + idx,
//                        "drawable",
//                        context.packageName
//                    )

                    img_ThumbsUP.setImageResource(idResIcon)
//                    img_ThumbsUP.setImageResource(R.drawable.ic_thumbs_up1)
                    img_ThumbsUP.isVisible(true)
                    //-------------------- img THUMBS UP

                    //------------ ocultam botons
                    btnSEND.isVisible(false)
//                    btnItentSendTo.isVisible(false)
                    btnItentSendTo.isVisible2 = false
                    if (btnItentSendTo.isVisible2) {
                        //....
                    }

                    txtSUBJECT.isEnabled =false
                    txtBODY.isEnabled =false


                    //finish()
                }

                override fun onFail(errorMessage: String) {
                    hideProgressDialog()
                    alertMsg(errorMessage, "MaildroidX ERROR")
                    Log.d("$TAG - MaildroidX", "FAIL: " + errorMessage)

                }
            })
            .mail()

    }


    /**
     * 23.01.2020
     * @source https://developer.android.com/training/notify-user/build-notification
     */
    fun sendNotification(title: String, msg: String) { //Get an instance of NotificationManager//
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_send)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            var notificationId: Int = 1102921

            notify(notificationId, builder.build())
        }


    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun hayConexionAInternet(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        return isConnected

    }


}
