package vt.cs.selab.androidhttpserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private MobileWebServer server;
    Button bServiceStart;
    Button bServiceStop;
    Button bRegisterStart;
    Button bSearch;
    ListView listView;
    Microservice serviceAgent;
    final String logTag = "server";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButtons();
        listView = (ListView) findViewById(R.id.listView);
        serviceAgent = new Microservice(this);
        serviceAgent.queryServiceByKeyword("");
    }



    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

    public void addLog(String logInfo){
        TextView log = (TextView)findViewById(R.id.textView);
        log.append(logInfo);
        Log.d(logTag, logInfo);
    }

    public void listViewShowMicroservices(String[] items){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item index
                int itemPosition     = position;
                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);
                // Show Alert
                Toast.makeText(getApplicationContext(), "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG).show();
            }
        });
    }


    public void addListenerOnButtons() {
        bServiceStart = (Button) findViewById(R.id.button_Start);
        bServiceStop = (Button) findViewById(R.id.button_Stop);
        bRegisterStart = (Button) findViewById(R.id.button_Reg);
        bSearch = (Button) findViewById(R.id.button_Search);

        bServiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                server = new MobileWebServer(getApplicationContext());
                try {
                    server.start();
                    bServiceStart.setEnabled(false);
                    bServiceStop.setEnabled(true);
                    bRegisterStart.setEnabled(true);
                } catch(IOException ioe) {
                    Log.w(logTag, "The server could not start.");
                }
                addLog( "Web server initialized.");
            }
        });

        bServiceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //server = new MobileWebServer(getApplicationContext());
                server.stop();
                bServiceStart.setEnabled(true);
                bServiceStop.setEnabled(false);
                bRegisterStart.setEnabled(false);
                addLog( "Web server stopped.");
            }
        });

        bRegisterStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addLog( "start to register all microservices.");
                //todo
                serviceAgent.registerServices();
            }
        });

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addLog("start to browse all microservices.");
                //todo
                EditText textInput = (EditText)findViewById(R.id.editText);
                String keyword = textInput.getText().toString();
                //addLog(keyword);
                serviceAgent.queryServiceByKeyword(keyword);
            }
        });

    }



}
