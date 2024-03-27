package com.smpt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ProfilFragment extends Fragment {

    private FirebaseAuth auth;
    private TextView user_email;
    private TextView punkty;
    private FirebaseUser user;

    private ImageView logoutButton;
    private ImageView editProfileDescriptionButton, confirmProfileDescriptionButton, btn_logout, back;
    private TextView profileDescriptionText;
    private EditText profileDescriptionEdit;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri selectedImageUri;
    ImageView profilePic;

    public ProfilFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        // Inicjalizacja FirebaseAuth i pobranie aktualnie zalogowanego użytkownika
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        user_email = view.findViewById(R.id.user_email);

        // Ustawienie adresu email użytkownika w TextView, jeśli jest zalogowany
        if (user != null) {
            user_email.setText(user.getEmail());
        } else {
            // Obsługa sytuacji, gdy użytkownik nie jest zalogowany
            user_email.setText("Użytkownik niezalogowany");
        }


        // Inicjalizacja widoków
        editProfileDescriptionButton = view.findViewById(R.id.editProfileDescriptionButton);
        confirmProfileDescriptionButton = view.findViewById(R.id.confirmProfileDescriptionButton);
        profileDescriptionText = view.findViewById(R.id.opis);
        punkty = view.findViewById(R.id.punkty);
        profileDescriptionEdit = view.findViewById(R.id.profileDescriptionEdit);
        profilePic = view.findViewById(R.id.profile_pic);

        back=view.findViewById(R.id.btn_profil_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sprawdzamy, czy jesteśmy dołączeni do aktywności
                if (isAdded() && getActivity() != null) {
                    // Używamy FragmentManagera do wykonania operacji na fragmentach
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .remove(ProfilFragment.this) // Usuwamy bieżący fragment
                            .commit();
                }
            }
        });


        btn_logout=view.findViewById(R.id.logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Logika dla przycisku edycji opisu profilu
        editProfileDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDescriptionText.setVisibility(View.GONE);
                profileDescriptionEdit.setText(profileDescriptionText.getText().toString());
                profileDescriptionEdit.setVisibility(View.VISIBLE);
                confirmProfileDescriptionButton.setVisibility(View.VISIBLE);
                profileDescriptionEdit.requestFocus();
            }
        });

        // Logika dla przycisku potwierdzenia edycji opisu profilu
        confirmProfileDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedDescription = profileDescriptionEdit.getText().toString();
                profileDescriptionText.setText(updatedDescription);
                profileDescriptionEdit.setVisibility(View.GONE);
                profileDescriptionText.setVisibility(View.VISIBLE);
                // Ukrywanie przycisku potwierdzenia po zapisaniu zmian
                confirmProfileDescriptionButton.setVisibility(View.GONE);

                if (user != null) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference userRef = database.getReference("users").child(user.getUid()).child("profileDescription");

                    userRef.setValue(updatedDescription).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Opis zaktualizowany pomyślnie.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Błąd przy aktualizacji opisu.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Użytkownik nie jest zalogowany.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Pobranie i ustawienie opisu profilu z Firebase przy otwieraniu fragmentu
        if (user != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users").child(user.getUid()).child("profileDescription");

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String description = task.getResult().getValue(String.class);
                    if (description != null && !description.isEmpty()) {
                        profileDescriptionText.setText(description);
                    } else {
                        profileDescriptionText.setText("(brak opisu)");
                    }
                } else {
                    profileDescriptionText.setText("(brak opisu)");
                    Log.e("Firebase", "Error fetching profile description", task.getException());
                }
            });




            DatabaseReference pointsRef = database.getReference("users").child(user.getUid()).child("points");
            // Pobieranie ilości punktów z bazy danych
            pointsRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Używamy typu prostego int z wartością domyślną 0
                    int points = 0;
                    if (task.getResult().getValue() != null) {
                        // Konwersja wyniku na typ prosty int
                        points = task.getResult().getValue(Integer.class);
                    }
                    // Ustawianie tekstu na podstawie pobranej ilości punktów
                    punkty.setText("Punkty: " + points);
                } else {
                    // W przypadku błędu również ustawiamy domyślnie 0
                    punkty.setText("Punkty: 0");
                    Log.e("Firebase", "Error fetching points", task.getException());
                }
            });
        } else {
            // Ustawiamy domyślnie 0, jeśli użytkownik nie jest zalogowany
            punkty.setText("Punkty: 0");




        }




        return view;
    }






}