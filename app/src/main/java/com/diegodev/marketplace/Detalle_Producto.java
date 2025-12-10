package com.diegodev.marketplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.diegodev.marketplace.adapter.ImagePagerAdapter;
import com.diegodev.marketplace.model.Producto;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Detalle_Producto extends AppCompatActivity {

    public static final String EXTRA_PRODUCTO_ID = "producto_id";

    private TextView tvTitulo, tvPrecio, tvDescripcion;
    private MaterialButton btnChat;
    private ViewPager2 viewPagerImagenes;

    private DatabaseReference productoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // ✅ IDs EXACTOS del XML
        tvTitulo = findViewById(R.id.tv_detalle_titulo);
        tvPrecio = findViewById(R.id.tv_detalle_precio);
        tvDescripcion = findViewById(R.id.tv_detalle_descripcion);
        btnChat = findViewById(R.id.btn_chat);
        viewPagerImagenes = findViewById(R.id.vp_detalle_imagenes);

        String productoId = getIntent().getStringExtra(EXTRA_PRODUCTO_ID);

        productoRef = FirebaseDatabase.getInstance()
                .getReference("productos")
                .child(productoId);

        productoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Producto producto = snapshot.getValue(Producto.class);

                if (producto != null) {
                    tvTitulo.setText(producto.getNombre());
                    tvPrecio.setText("$" + producto.getPrecio());
                    tvDescripcion.setText(producto.getDescripcion());

                    if (producto.getImageUrls() != null &&
                            !producto.getImageUrls().isEmpty()) {

                        ImagePagerAdapter adapter =
                                new ImagePagerAdapter(producto.getImageUrls());
                        viewPagerImagenes.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(Detalle_Producto.this, ChatActivity.class);
            intent.putExtra("producto_id", productoId);
            startActivity(intent);
        });
    }
}
