package com.example.tableahead;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.search.SelectTypeScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public class accountScreen extends AppCompatActivity implements View.OnClickListener {
    public final ArrayList<String> dataTitles = new ArrayList<>();
    public final ArrayList<String> dataFields = new ArrayList<>();

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    /** @noinspection FieldMayBeFinal, FieldCanBeLocal */
    private final String userID = mAuth.getUid();
    private String subtitle = "null";
    final accountDataRVAdapter adapter = new accountDataRVAdapter(dataTitles, dataFields);

    //Simply used for async task of connecting to database backend
    public static class Invoker implements Executor {
        @Override
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    //Tight coupling of this and accountFragment, passes it the userID to edit on backend
    public String getUserID() {
        return userID;
    }

    //Passes email to accountFragment
    public String getEmail() {
        return dataFields.get(2);
    }

    //Passes password to accountFragment
    public String getPass() {
        return dataFields.get(3);
    }

    //Passes email to accountFragment
    public String getSubtitle() {
        return subtitle;
    }

    //Sets subtitle field to newStr
    public void setSubtitle(String newStr) {
        subtitle = newStr;
    }

    //Updates the accountDataRVAdapter of a data update from start to amount
    public void updateData(int start, int amount) {
        adapter.notifyItemRangeChanged(start, amount);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_screen);
        //Necessary for async database connection
        AtomicReference<FirebaseFirestore> db = new AtomicReference<>();
        AtomicReference<DocumentReference> user = new AtomicReference<>();

        ProgressBar prgsBar = findViewById(R.id.progressBar);
        TextView loadText = findViewById(R.id.loadText);

        //Toolbar
        Button signOutBtn = findViewById(R.id.signOutButton);
        signOutBtn.setOnClickListener(c -> {
            mAuth.signOut();
            Intent intent2 = new Intent(this, Login.class);
            // Kill activity stack
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);
            finish();
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        ImageButton homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(c -> {
            Intent intent2 = new Intent(this, SelectTypeScreen.class);
            // Kill activity stack
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
            finish();
        });
        ImageButton helpBtn = findViewById(R.id.helpBtn);
        helpBtn.setOnClickListener(c -> startActivity(new Intent(this, helpScreen.class)));
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(c -> finish());

        Button addPhoneBtn  = findViewById(R.id.addPhoneBtn);
        addPhoneBtn.setVisibility(View.INVISIBLE);
        addPhoneBtn.setOnClickListener(x -> {
            if(addPhoneBtn.getText().toString().equals(getString(R.string.addPhoneNum))) {
                //add phone number to data
                final String[] phoneNum = {"null"};
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final EditText newNum = new EditText(this);
                newNum.setInputType(InputType.TYPE_CLASS_PHONE);
                alert.setMessage(getString(R.string.enterPhoneNum));
                alert.setTitle(getString(R.string.phoneNumTitle));
                alert.setView(newNum);
                alert.setPositiveButton(getString(R.string.confirm), (dialog, whichButton) -> {
                    phoneNum[0] = newNum.getText().toString();
                    dataFields.add(phoneNum[0]);
                    dataTitles.add(getString(R.string.phoneNumberColon));
                    user.get().update("phoneNumber", phoneNum[0]);
                    adapter.notifyDataSetChanged();
                    addPhoneBtn.setText(getString(R.string.alreadyPhone));
                });
                alert.setNegativeButton(getString(R.string.cancel), (dialog, whichButton) -> {

                });
                alert.show();
            } else { //remove phone number from data
                user.get().update("phoneNumber", FieldValue.delete());
                dataFields.remove(4);
                dataTitles.remove(4);
                adapter.notifyItemRemoved(4);
                addPhoneBtn.setText(getString(R.string.addPhoneNum));
            }
        });
        dataTitles.add(getString(R.string.firstNameColon));
        dataTitles.add(getString(R.string.lastNameColon));
        dataTitles.add(getString(R.string.emailAddressColon));
        dataTitles.add(getString(R.string.passwordColon));
        Executor exe = new Invoker();

        //Async database connection
        exe.execute( () -> {
            db.set(FirebaseFirestore.getInstance());
            user.set(db.get().collection("userData").document(userID));
            user.get().get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast.makeText(accountScreen.this, getString(R.string.gotUserData), Toast.LENGTH_SHORT).show();
                        dataFields.add(document.get("firstName", String.class));
                        dataFields.add(document.get("lastName", String.class));
                        dataFields.add(document.get("email", String.class));
                        dataFields.add(document.get("password", String.class));
                        if(document.get("phoneNumber", String.class) != null) {
                            dataTitles.add(getString(R.string.phoneNumberColon));
                            dataFields.add(document.get("phoneNumber", String.class));
                            addPhoneBtn.setText(R.string.alreadyPhone);
                        }
                        runOnUiThread(() -> { //Necessary for updating UI elements
                            RecyclerView recyclerView = findViewById(R.id.accountRV);
                            recyclerView.setLayoutManager(new LinearLayoutManager(accountScreen.this));
                            recyclerView.setAdapter(adapter);
                            loadText.setVisibility(View.INVISIBLE);
                            prgsBar.setVisibility(View.INVISIBLE);
                            addPhoneBtn.setVisibility(View.VISIBLE);
                        });
                    } else {
                        Toast.makeText(accountScreen.this, getString(R.string.gotUserDataFail), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(accountScreen.this, getString(R.string.gotUserDataFail), Toast.LENGTH_SHORT).show();
                }
            });
        });
        prgsBar.setIndeterminateTintList(ColorStateList.valueOf(Color.BLUE));
        View frag = findViewById(R.id.accountFragment);
        frag.setVisibility(View.INVISIBLE);
    }

    //Sets subtitle local var to whatever is being edited in the fragment
    @Override
    public void onClick(View view) {
        TextView thing = (TextView)view;
        String text = thing.getText().toString();
        View frag = findViewById(R.id.accountFragment);
        TextView subTitle = findViewById(R.id.accountFragmentSubtitle);
        if (dataFields.contains(text)) {setSubtitle(dataTitles.get(dataFields.indexOf(text)).replace(":", ""));}
        else {setSubtitle(text.replace(":", ""));}
        subTitle.setText(getSubtitle());
        frag.setVisibility(View.VISIBLE);
    }


    //RecyclerView (inner-class)
    public class accountDataRVAdapter extends RecyclerView.Adapter<com.example.tableahead.accountScreen.accountDataRVAdapter.ViewHolder> {
        private final List<String> titles;
        private final List<String> fields;
        public accountDataRVAdapter(List<String> titles, List<String> fields){
            this.titles = titles;
            this.fields = fields;
        }

        //Creating an item in the RV
        @NonNull
        @Override
        public accountScreen.accountDataRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_info_row, parent, false);
            return new com.example.tableahead.accountScreen.accountDataRVAdapter.ViewHolder(rowItem);
        }

        //Sets values of the RV textviews
        @Override
        public void onBindViewHolder(@NonNull com.example.tableahead.accountScreen.accountDataRVAdapter.ViewHolder holder, int position) {
            if(!Objects.equals(this.titles.get(position), getString(R.string.passwordColon))) {
                holder.dataField.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            }
            holder.dataTitle.setText(this.titles.get(position));
            holder.dataField.setText(this.fields.get(position));
            holder.dataTitle.setOnClickListener(accountScreen.this);
            holder.dataField.setOnClickListener(accountScreen.this);
        }

        @Override
        public int getItemCount() {
            return this.titles.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView dataTitle;
            private final TextView dataField;

            public ViewHolder(View view) {
                super(view);
                this.dataTitle = view.findViewById(R.id.accountDataTitle);
                this.dataField = view.findViewById(R.id.accountDataField);
            }
        }
    }
}