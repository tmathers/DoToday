package com.taramathers.dotoday

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    val listItems: MutableMap<Int, String> = HashMap()
    var lastId = 0

    var percentDone = 0


    //val editText by lazy { TextView(this) }
    //val editText by lazy { TextView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)

        fab.setOnClickListener {

            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            //val rowView = inflater.inflate(R.layout.mainLayout, null)




            val editText = EditText(this)
            mainLayout.addView(editText)



            editText.apply {
                hint = "Enter something"
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.setMargins(8, 8, 8, 8)
                layoutParams = lp
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_light))
                imeOptions = EditorInfo.IME_ACTION_DONE

            }

            editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    createItem(editText.text.toString().trim())
                    mainLayout.removeView(editText)
                }
                false
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun Context.toast(context: Context = applicationContext, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun EditText.smartTextWatcher(on: (String) -> Unit, after: (String) -> Unit, before: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                after.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                before.invoke(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                on.invoke(s.toString())
            }
        })
    }

    fun createItem(itemText: String) {
        println("done_")

        lastId++
        listItems.put(lastId, itemText)
        print(listItems)

        var checkBox:CheckBox = CheckBox(this)
        checkBox.setText(itemText)


        //val linearLayout = findViewById(R.id.mainLayout) as LinearLayout
        itemLayout.addView(checkBox)


        checkBox.setOnClickListener( View.OnClickListener {
            updatePercentDone()
        })
    }

    fun updatePercentDone() {

        val linearLayout = findViewById(R.id.itemLayout) as LinearLayout

        var numChecked : Float = 0f
        val childCount = itemLayout.childCount


        println("CHILDREN " + childCount.toString())
        for (i in 0 until childCount) {
            val view = itemLayout.getChildAt(i)
            if (view is CheckBox) {
                val checkBox = view as CheckBox
                if (checkBox.isChecked) {
                    numChecked++
                }
            }
        }


        print( numChecked.toString() + " / " + listItems.size.toString())
        var percent : Float = numChecked / listItems.size.toFloat()
        percent *= 100
        var percentInt = percent.roundToInt()



println( " = " + percent.toString())

        totalText.text = percentInt.toString() + "% Complete"



    }
}
