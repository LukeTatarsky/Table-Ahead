package com.example.tableahead;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class accountFragment extends Fragment {
    FirebaseAuth mAuth;

    public accountFragment() {
        super(R.layout.account_fragment);
    }

    /** @noinspection deprecation*/
    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        accountScreen accScreen = (accountScreen)getActivity();
        Button confirmEdit = requireActivity().findViewById(R.id.accountFragmentConfirm);
        TextView editedText = requireActivity().findViewById(R.id.accountFragmentEditor);
        confirmEdit.setOnClickListener(x -> {
            assert accScreen != null;
            String toChange = editedText.getText().toString();
            if(toChange.isEmpty()) {
                //User didn't type anything
                Toast.makeText(getContext(), R.string.emptyText, Toast.LENGTH_SHORT).show();
            } else {
                String field;
                String checker = accScreen.getSubtitle();
                //Manually checking each possibility for what is being edited
                //Have a couple specific cases to be handled
                //In general, get new text, update DB, hide fragment and tell user successful
                //In specific cases, reauthorize user login, then try to update
                if (checker.equals(getString(R.string.firstNameColon).replace(":", ""))) {
                    field = "firstName";
                    db.collection("userData").document(accScreen.getUserID()).update(field, toChange);
                    int index = accScreen.dataTitles.indexOf(accScreen.getSubtitle().concat(":"));
                    accScreen.dataFields.set(index, toChange);
                    accScreen.updateData(index, 1);
                    accScreen.findViewById(R.id.accountFragment).setVisibility(View.INVISIBLE);
                    editedText.setText("");
                    Snackbar.make(requireView(), R.string.updateAccountSuccess, Snackbar.LENGTH_SHORT).show();
                }
                else if (checker.equals(getString(R.string.lastNameColon).replace(":", ""))) {
                    field = "lastName";
                    db.collection("userData").document(accScreen.getUserID()).update(field, toChange);
                    int index = accScreen.dataTitles.indexOf(accScreen.getSubtitle().concat(":"));
                    accScreen.dataFields.set(index, toChange);
                    accScreen.updateData(index, 1);
                    accScreen.findViewById(R.id.accountFragment).setVisibility(View.INVISIBLE);
                    editedText.setText("");
                    Snackbar.make(requireView(), R.string.updateAccountSuccess, Snackbar.LENGTH_SHORT).show();
                }
                else if (checker.equals(getString(R.string.emailAddressColon).replace(":", ""))) {
                    field = "email";
                    AuthCredential credential = EmailAuthProvider.getCredential(accScreen.getEmail(), accScreen.getPass());
                    Objects.requireNonNull(mAuth.getCurrentUser()).reauthenticate(credential).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            mAuth.getCurrentUser().updateEmail(toChange).addOnCompleteListener(task1 -> {
                                if(!task1.isSuccessful()){
                                    Snackbar.make(requireView(), R.string.updateAccountFail, Snackbar.LENGTH_SHORT).show();
                                    Log.w("Account Update", task1.getException());
                                    Snackbar.make(requireView(), R.string.updateAccountFailInfo, Snackbar.LENGTH_LONG).show();
                                } else {
                                    db.collection("userData").document(accScreen.getUserID()).update(field, toChange);
                                    int index = accScreen.dataTitles.indexOf(accScreen.getSubtitle().concat(":"));
                                    accScreen.dataFields.set(index, toChange);
                                    accScreen.updateData(index, 1);
                                    accScreen.findViewById(R.id.accountFragment).setVisibility(View.INVISIBLE);
                                    editedText.setText("");
                                    Snackbar.make(requireView(), R.string.updateAccountSuccess, Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            Snackbar.make(requireView(), R.string.updateAccountFail, Snackbar.LENGTH_SHORT).show();
                            Log.w("Account Update", task.getException());
                            Snackbar.make(requireView(), R.string.updateAccountFailInfo, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                else if (checker.equals(getString(R.string.passwordColon).replace(":", ""))) {
                    field = "password";
                    AuthCredential credential = EmailAuthProvider.getCredential(accScreen.getEmail(), accScreen.getPass());
                    Objects.requireNonNull(mAuth.getCurrentUser()).reauthenticate(credential).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            mAuth.getCurrentUser().updatePassword(toChange).addOnCompleteListener(task12 -> {
                                if(!task12.isSuccessful()){
                                    Snackbar.make(requireView(), R.string.updateAccountFail, Snackbar.LENGTH_SHORT).show();
                                    Log.w("Account Update", task12.getException());
                                    Snackbar.make(requireView(), R.string.updateAccountFailInfo, Snackbar.LENGTH_LONG).show();
                                } else {
                                    db.collection("userData").document(accScreen.getUserID()).update(field, toChange);
                                    int index = accScreen.dataTitles.indexOf(accScreen.getSubtitle().concat(":"));
                                    accScreen.dataFields.set(index, toChange);
                                    accScreen.updateData(index, 1);
                                    accScreen.findViewById(R.id.accountFragment).setVisibility(View.INVISIBLE);
                                    editedText.setText("");
                                    Snackbar.make(requireView(), R.string.updateAccountSuccess, Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            Snackbar.make(requireView(), R.string.updateAccountFail, Snackbar.LENGTH_SHORT).show();
                            Log.w("Account Update", task.getException());
                            Snackbar.make(requireView(), R.string.updateAccountFailInfo, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                else if (checker.equals(getString(R.string.phoneNumberColon).replace(":", ""))) {
                    field = "phoneNumber";
                    db.collection("userData").document(accScreen.getUserID()).update(field, toChange);
                    int index = accScreen.dataTitles.indexOf(accScreen.getSubtitle().concat(":"));
                    accScreen.dataFields.set(index, toChange);
                    accScreen.updateData(index, 1);
                    accScreen.findViewById(R.id.accountFragment).setVisibility(View.INVISIBLE);
                    editedText.setText("");
                    Snackbar.make(requireView(), R.string.updateAccountSuccess, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}