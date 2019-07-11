package developer.ujjwal.testapp1;

import android.content.Intent;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText name, phone;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        try {
            Toast.makeText(getApplicationContext(), mUser.getPhoneNumber(), Toast.LENGTH_SHORT).show();
            if (mUser.getPhoneNumber() != null) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Please Submit Details", Toast.LENGTH_SHORT).show();
        }

        name = findViewById(R.id.activityRegisterName);
        phone = findViewById(R.id.activityRegisterPhone);
        submit = findViewById(R.id.activityRegisterSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = name.getText().toString().trim();
                if (n.isEmpty() || name.length() < 3) {
                    showError(1);
                    return;
                }

                String phoneNumber = phone.getText().toString().trim();
                Pattern p = Pattern.compile("^[6-9][0-9]{9}$");
                Matcher m = p.matcher(phoneNumber);
                if (phoneNumber.isEmpty() || !m.find()) {
                    showError(2);
                    return;
                }
                phoneNumber = "+91" + phoneNumber;


                //Secure Android ID :: https://medium.com/@ssaurel/how-to-retrieve-an-unique-id-to-identify-android-devices-6f99fd5369eb
                String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Toast.makeText(getApplicationContext(), "Secure Android ID: " + android_id, Toast.LENGTH_SHORT).show();

                Intent in = new Intent(getApplicationContext(), OtpActivity.class);
                in.putExtra("phoneNumber", phoneNumber);
                startActivity(in);
                //finish();
            }
        });
    }

    private void showError(int i) {
        switch (i) {
            case 1:
                name.requestFocus();
                name.setError("Enter Name");
                break;
            case 2:
                phone.requestFocus();
                phone.setError("Invalid Phone");
                break;
        }
    }
}
