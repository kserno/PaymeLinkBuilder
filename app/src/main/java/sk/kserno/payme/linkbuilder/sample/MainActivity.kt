package sk.kserno.payme.linkbuilder.sample

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sk.kserno.payme.PayMeLinkBuilder
import sk.kserno.payme.linkbuilder.R

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_create.setOnClickListener {
            try {
                val builder = PayMeLinkBuilder(
                        edit_iban.text.toString(),
                        edit_amount.text.toString(),
                        edit_currency_code.text.toString(),
                        edit_version.text.toString().toIntOrNull() ?: 1,
                        cb_validate.isChecked
                )
                val message = edit_message.text.toString()
                builder.setMessage(message)
                val pi = edit_pi.text.toString()
                builder.setPaymentIdentification(pi)

                val link = builder.build()
                val text = "<a href=$link>$link</a>"
                txt_result.text = Html.fromHtml(text)
            } catch (e: Exception) {
                AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(e.message)
                        .show()
            }


        }

    }
}