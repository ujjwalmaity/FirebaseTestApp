package developer.ujjwal.firebasetestapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private String verificationId;
    private FirebaseAuth mAuth;

    private EditText editText;
    private ProgressBar progressBar;
    String name, phone, mac, userIdPhone;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String NAME_KEY = "Name";
    private static final String PHONE_KEY = "Phone";
    private static final String MAC_KEY = "Mac";
    private static final String ACTIVE_KEY = "Active";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();

        editText = findViewById(R.id.activityOtpCode);
        progressBar = findViewById(R.id.activityOtpProgressBar);

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        mac = getIntent().getStringExtra("mac");

        sendVerificationCode(phone);

        findViewById(R.id.activityOtpSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editText.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    editText.setError("Enter Code");
                    editText.requestFocus();
                    return;
                }
                try {
                    verifyCode(code);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacksPhoneAuthActivity.java
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }
    };

    public void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            try {
                                userIdPhone = mUser.getPhoneNumber();
                            } catch (Exception e) {
                            }

                            /*Query query = db.collection("users")
                                    .orderBy("Phone", Direction.ASCENDING)
                                    .whereEqualTo("Phone", phone);*/

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
                                                    Intent in2 = new Intent(getApplicationContext(), MainActivity.class);
                                                    in2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(in2);
                                                }
                                                //Inactive User
                                                else {
                                                    Toast.makeText(getApplicationContext(), "You Are Not Allow", Toast.LENGTH_SHORT).show();
                                                    FirebaseAuth.getInstance().signOut();
                                                    finish();
                                                }
                                            }

                                            //First Time User(Upload Data)
                                            if (m == null && a == null) {
                                                Map<String, Object> user = new HashMap<>();
                                                user.put(NAME_KEY, name);
                                                user.put(PHONE_KEY, phone);
                                                user.put(MAC_KEY, mac);
                                                user.put(ACTIVE_KEY, true);
                                                db.collection("users")
                                                        .document(userIdPhone)
                                                        .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getApplicationContext(), "Data Saved", Toast.LENGTH_SHORT).show();
                                                        Intent in2 = new Intent(getApplicationContext(), MainActivity.class);
                                                        in2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(in2);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getApplicationContext(), "Data Not Saved", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
