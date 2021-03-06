package com.nullsoft.art.kuponchikru;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.Bind;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loginBtnListener();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });


        tryAutologin();
    }

    public void loginBtnListener() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed("Ошибка входа");
            return;
        }

        _loginButton.setEnabled(false);

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        login(email, password);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Введите корректный e-mail");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Пароль должен быть длиной от 5 до 10 символов");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void login(String email, final String password)
    {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Авторизация...");
        progressDialog.show();

        Call<ServerApi.ServerResponse> call = ServerApi.getUserService().login(email,password);
        call.enqueue(new Callback<ServerApi.ServerResponse>() {

            @Override
            public void onFailure(Call<ServerApi.ServerResponse> call, Throwable t) {
                Log.d("login() error:",t.getMessage());
                onLoginFailed("Ошибка входа");
                progressDialog.dismiss();

            }

            @Override
            public void onResponse(Call<ServerApi.ServerResponse> call, Response<ServerApi.ServerResponse> response) {
                if(response.raw().code()!=200)
                {
                    onLoginFailed("Ошибка входа");
                }

                if(response.raw().code()==200 && !response.body().success)
                {
                    if(response.body().errorText!=null)
                    {
                        onLoginFailed(response.body().errorText);
                    }
                }

                if(response.raw().code()==200 && response.body().success)
                {
                    response.body().user.PASSWORD=password;
                    UserController.getController().insert(response.body().user);
                    onLoginSuccess();
                }
                progressDialog.dismiss();
            }
        });
    }

    public void tryAutologin()
    {
        User user=UserController.getController().get();
        if(user!=null)
        {
            _emailText.setText(user.LOGIN);
            _passwordText.setText(user.PASSWORD);
            login(user.LOGIN, user.PASSWORD);
        }
    }

}