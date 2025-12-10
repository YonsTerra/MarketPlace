package com.diegodev.marketplace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.diegodev.marketplace.adapter.MensajeAdapter;
import com.diegodev.marketplace.adapter.ConversationAdapter;
import com.diegodev.marketplace.model.Mensaje;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText etMensaje;
    private ImageButton btnEnviarMensaje;
    private ImageButton btnEnviarImagen;
    private TextView tvNombreContacto;
    private TextView tvEstadoContacto;
    private ImageView imgProfile;
    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> listaMensajes;
    private DatabaseReference mensajesRef;
    private String currentUserId;
    private String contactoId;
    private ConversationAdapter conversationAdapter;
    private List<ConversationAdapter.Item> listaConversaciones;
    private DatabaseReference conversacionesRef;
    private android.widget.LinearLayout layoutInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        inicializarVistas();
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user != null ? user.getUid() : null;
        contactoId = getIntent().getStringExtra("contacto_id");
        configurarCabecera();
        if (contactoId != null && !contactoId.isEmpty()) {
            layoutInput.setVisibility(View.VISIBLE);
            configurarRecyclerView();
            configurarListeners();
            configurarFirebase(); //  listener  al final
        } else {
            configurarListaConversaciones();
        }

    }
    private void inicializarVistas() {
        tvNombreContacto = findViewById(R.id.tvContactName);
        tvEstadoContacto = findViewById(R.id.tvContactStatus);
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMensaje = findViewById(R.id.editTextMensaje);
        btnEnviarMensaje = findViewById(R.id.btnEnviar);
        btnEnviarImagen = findViewById(R.id.btnAttachImage);
        layoutInput = findViewById(R.id.layout_input);
        imgProfile = findViewById(R.id.imgProfile);
    }
    private void configurarCabecera() {
        if (contactoId == null || contactoId.isEmpty()) {
            tvNombreContacto.setText("Mis Chats");
            tvEstadoContacto.setText("");
        } else {
            tvNombreContacto.setText("Contacto");
            tvEstadoContacto.setText("online");
            FirebaseDatabase.getInstance().getReference("users").child(contactoId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String nombre = snapshot.child("nombre").getValue(String.class);
                            if (nombre != null && !nombre.isEmpty()) tvNombreContacto.setText(nombre);
                            String avatar = snapshot.child("urlImagenPerfil").getValue(String.class);
                            if (imgProfile != null) {
                                Glide.with(ChatActivity.this)
                                        .load(avatar)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .error(R.drawable.ic_profile_placeholder)
                                        .circleCrop()
                                        .into(imgProfile);
                            }
                        }
                        @Override public void onCancelled(DatabaseError error) { }
                    });
        }
    }
    private void configurarRecyclerView() {
        listaMensajes = new ArrayList<>();
        mensajeAdapter = new MensajeAdapter(this, listaMensajes, currentUserId != null ? currentUserId : "");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mensajeAdapter);
    }
    private void configurarListaConversaciones() {
        listaConversaciones = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(this, listaConversaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(conversationAdapter);
        layoutInput.setVisibility(View.GONE);
        if (currentUserId == null) {
            Toast.makeText(this, "Inicie sesión para ver sus chats.", Toast.LENGTH_LONG).show();
            return;
        }
        conversacionesRef = FirebaseDatabase.getInstance().getReference("user_chats").child(currentUserId);
        conversationAdapter.setOnConversationClickListener(contactId -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("contacto_id", contactId);
            startActivity(i);
        });
        conversacionesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaConversaciones.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String contactId = child.child("contactId").getValue(String.class);
                    String name = child.child("contactName").getValue(String.class);
                    String lastMsg = child.child("lastMessage").getValue(String.class);
                    Long ts = child.child("lastTimestamp").getValue(Long.class);
                    String avatar = child.child("contactAvatarUrl").getValue(String.class);
                    if (contactId != null) {
                        listaConversaciones.add(new ConversationAdapter.Item(contactId, name, lastMsg, ts != null ? ts : 0, avatar));
                    }
                }
                java.util.Collections.sort(listaConversaciones, (a, b) -> Long.compare(b.lastTimestamp, a.lastTimestamp));
                conversationAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }
    private void configurarFirebase() {
        if (currentUserId == null || contactoId == null) return;

        String chatId = generarChatId(currentUserId, contactoId);
        mensajesRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatId)
                .child("mensajes");

        mensajesRef.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        listaMensajes.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Mensaje m = child.getValue(Mensaje.class);
                            if (m != null) listaMensajes.add(m);
                        }
                        mensajeAdapter.notifyDataSetChanged();
                        if (!listaMensajes.isEmpty()) {
                            recyclerView.scrollToPosition(listaMensajes.size() - 1);
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }

    private void configurarListeners() {
        btnEnviarMensaje.setOnClickListener(v -> enviarMensaje());
        btnEnviarImagen.setOnClickListener(v -> Toast.makeText(this, "Adjuntar imagen próximamente", Toast.LENGTH_SHORT).show());
    }
    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (texto.isEmpty() || mensajesRef == null || currentUserId == null) {
            Toast.makeText(this, "Escriba un mensaje.", Toast.LENGTH_SHORT).show();
            return;
        }
        String key = mensajesRef.push().getKey();
        if (key == null) return;
        Mensaje m = new Mensaje(key, currentUserId, texto, System.currentTimeMillis());
        mensajesRef.child(key).setValue(m).addOnSuccessListener(a -> {
            etMensaje.setText("");
            actualizarIndiceChat(currentUserId, contactoId, texto, m.getTimestamp());
            actualizarIndiceChat(contactoId, currentUserId, texto, m.getTimestamp());
        });
    }
    private String generarChatId(String a, String b) {
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }
    private void actualizarIndiceChat(String ownerId, String otherId, String lastMessage, long ts) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_chats").child(ownerId).child(generarChatId(ownerId, otherId));
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(otherId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String nombre = snapshot.child("nombre").getValue(String.class);
                String avatar = snapshot.child("urlImagenPerfil").getValue(String.class);
                java.util.Map<String,Object> data = new java.util.HashMap<>();
                data.put("contactId", otherId);
                data.put("contactName", nombre);
                data.put("lastMessage", lastMessage);
                data.put("lastTimestamp", ts);
                if (avatar != null) data.put("contactAvatarUrl", avatar);
                ref.updateChildren(data);
            }
            @Override public void onCancelled(DatabaseError error) { }
        });
    }
}
