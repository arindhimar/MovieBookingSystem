package com.example.cardview



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var datalist:ArrayList<datalist>
    private lateinit var imagelist:Array<Int>
    private lateinit var desc:Array<String>
    private  lateinit var name:Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         recyclerView= findViewById(R.id.recyclerView)

        recyclerView.layoutManager=LinearLayoutManager(this)


      imagelist=  arrayOf(
          1,2,3,4
      /*  R.drawable.img1,
          R.drawable.img2,
          R.drawable.img1,
          R.drawable.img2,*/
      )
        desc= arrayOf("aaaa","bbb","ccc","ddd")

        name= arrayOf("PK","POWER","AVANGERS","SUN")
        datalist= arrayListOf()
        getdata()




    }
   private  fun getdata()
    {
        for(i in imagelist.indices)
        {
            val dataclass=datalist(imagelist[i],name[i], desc[i])
            datalist.add(dataclass)
        }
       // recyclerView.adapter=dataAdapter(datalist)
        var adpter = dataAdapter(datalist)
        recyclerView.adapter = adpter
    }
}