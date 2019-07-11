package developer.ujjwal.testapp1;

import android.content.Intent;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText name, phone;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.activityRegisterName);
        phone = findViewById(R.id.activityRegisterPhone);
        submit = findViewById(R.id.activityRegisterSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Secure Android ID :: https://medium.com/@ssaurel/how-to-retrieve-an-unique-id-to-identify-android-devices-6f99fd5369eb
                String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Toast.makeText(getApplicationContext(), android_id, Toast.LENGTH_SHORT).show();

                Intent in = new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(in);
                //finish();
            }
        });
    }
}
