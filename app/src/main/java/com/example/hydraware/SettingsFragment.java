package com.example.hydraware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.hydraware.R;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("settings", 0);
            // Switch de notificaciones
            Switch switchNotifications = view.findViewById(R.id.switchNotifications);
            if (switchNotifications != null) {
                boolean enabled = prefs.getBoolean("ph_notifications", true);
                switchNotifications.setChecked(enabled);
                switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    prefs.edit().putBoolean("ph_notifications", isChecked).apply();
                });
            }
            // Selector de tema
            Spinner spinnerTheme = view.findViewById(R.id.spinnerTheme);
            if (spinnerTheme != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.theme_options, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTheme.setAdapter(adapter);
                int themeIndex = prefs.getInt("theme_mode", 0);
                spinnerTheme.setSelection(themeIndex);
                spinnerTheme.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                        prefs.edit().putInt("theme_mode", position).apply();
                        switch (position) {
                            case 0:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                break;
                            case 1:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                break;
                            case 2:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                break;
                        }
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            }
            // Soporte y reporte
            EditText editTextSupport = view.findViewById(R.id.editTextSupport);
            Button buttonSendSupport = view.findViewById(R.id.buttonSendSupport);
            if (editTextSupport != null && buttonSendSupport != null) {
                buttonSendSupport.setOnClickListener(v2 -> {
                    String mensaje = editTextSupport.getText().toString().trim();
                    if (mensaje.isEmpty()) {
                        Toast.makeText(requireContext(), "Por favor describe el problema o sugerencia.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822");
                        emailIntent.setPackage("com.google.android.gm");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hydraware61@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de error/sugerencia Hydraware");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, mensaje);
                        startActivity(Intent.createChooser(emailIntent, "Enviar reporte vía Gmail..."));
                        Toast.makeText(requireContext(), "¡Gracias por mandar tu reporte o sugerencia!", Toast.LENGTH_LONG).show();
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(requireContext(), "No hay aplicaciones de Gmail instaladas.", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(requireContext(), "Ocurrió un error al intentar enviar el correo.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Ocurrió un error en los ajustes: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return view;
    }
} 