package com.diegodev.marketplace;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.diegodev.marketplace.model.Producto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditarProductoActivity extends AppCompatActivity {

    private EditText etNombre, etPrecio, etDescripcion;
    private Button btnGuardar;

    private DatabaseReference productoRef;
    private String productoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_producto);

        productoId = getIntent().getStringExtra("producto_id");
        if (productoId == null) {
            finish();
            return;
        }

        etNombre = findViewById(R.id.et_nombre);
        etPrecio = findViewById(R.id.et_precio);
        etDescripcion = findViewById(R.id.et_descripcion);
        btnGuardar = findViewById(R.id.btn_guardar);

        productoRef = FirebaseDatabase.getInstance()
                .getReference("productos")
                .child(productoId);

        cargarProducto();

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void cargarProducto() {
        productoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Producto producto = snapshot.getValue(Producto.class);
                if (producto != null) {
                    etNombre.setText(producto.getNombre());
                    etPrecio.setText(String.valueOf(producto.getPrecio()));
                    etDescripcion.setText(producto.getDescripcion());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditarProductoActivity.this,
                        "Error al cargar producto",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarCambios() {
        productoRef.child("nombre").setValue(etNombre.getText().toString());
        productoRef.child("precio").setValue(
                Double.parseDouble(etPrecio.getText().toString())
        );
        productoRef.child("descripcion").setValue(
                etDescripcion.getText().toString()
        );

        Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
        finish();
    }
}
