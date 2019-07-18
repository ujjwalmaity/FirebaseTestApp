package developer.ujjwal.firebasetestapp;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText nameEditText, phoneEditText;
    Button submitButton;
    String name, phone, mac, userIdPhone;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String MAC_KEY = "Mac";
    private static final String ACTIVE_KEY = "Active";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mac = getBeautifulMac(getApplicationContext());
/*
        //Secure Android ID :: https://medium.com/@ssaurel/how-to-retrieve-an-unique-id-to-identify-android-devices-6f99fd5369eb
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Toast.makeText(getApplicationContext(), "Secure Android ID: " + android_id, Toast.LENGTH_SHORT).show();
*/

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        try {
            userIdPhone = mUser.getPhoneNumber();

            if (userIdPhone != null) {
                db.collection("users")
                        .document(userIdPhone)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String m = documentSnapshot.getString(MAC_KEY);
                                Boolean a = documentSnapshot.getBoolean(ACTIVE_KEY);

                                //Existing User(Check Data)
                                if (m != null && a != null) {
                                    //Active User
                                    if (m.equals(mac) && a.toString().equals("true")) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    //Inactive User
                                    else {
                                        Toast.makeText(getApplicationContext(), "You Are Not Allow", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }
                        });
            }
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Please Submit Details", Toast.LENGTH_SHORT).show();
        }

        nameEditText = findViewById(R.id.activityRegisterName);
        phoneEditText = findViewById(R.id.activityRegisterPhone);
        submitButton = findViewById(R.id.activityRegisterSubmit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameEditText.getText().toString().trim();
                if (name.isEmpty() || name.length() < 3) {
                    showError(1);
                    return;
                }

                phone = phoneEditText.getText().toString().trim();
                Pattern p = Pattern.compile("^[6-9][0-9]{9}$");
                Matcher m = p.matcher(phone);
                if (phone.isEmpty() || !m.find()) {
                    showError(2);
                    return;
                }
                phone = "+91" + phone;

                Intent in = new Intent(getApplicationContext(), OtpActivity.class);
                in.putExtra("name", name);
                in.putExtra("phone", phone);
                in.putExtra("mac", mac);
                startActivity(in);
                //finish();
            }
        });
    }

    private void showError(int i) {
        switch (i) {
            case 1:
                nameEditText.requestFocus();
                nameEditText.setError("Enter Name");
                break;
            case 2:
                phoneEditText.requestFocus();
                phoneEditText.setError("Invalid Phone");
                break;
        }
    }

    public static String getBeautifulMac(Context context) {
        String macAddr = getMacAddress(context);
        return macAddr.replaceAll(":", "");
    }

    public static String getMacAddress(Context context) {
        String macAddr = "";
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().toLowerCase().startsWith("wlan")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString().toLowerCase();
            }
        } catch (Exception ex) {
        }
        return "";

        //return macAddr;
    }
}
