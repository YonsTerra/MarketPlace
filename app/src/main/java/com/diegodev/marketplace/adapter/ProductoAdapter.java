package com.diegodev.marketplace.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.diegodev.marketplace.Detalle_Producto;
import com.diegodev.marketplace.EditarProductoActivity;
import com.diegodev.marketplace.R;
import com.diegodev.marketplace.model.Producto;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private final Context context;

    // LISTAS BIEN DEFINIDAS
    private final List<Producto> listaOriginal;
    private final List<Producto> listaVisible;

    private boolean modoMisAnuncios = false;

    public void setModoMisAnuncios(boolean activo) {
        this.modoMisAnuncios = activo;
        notifyDataSetChanged();
    }

    public ProductoAdapter(Context context, List<Producto> productos) {
        this.context = context;
        this.listaOriginal = new ArrayList<>();
        this.listaVisible = new ArrayList<>();

        listaOriginal.addAll(productos);
        listaVisible.addAll(productos);
    }

    //  FILTRAR (ERROR SOLUCIONADO)
    public void filtrar(String texto) {
        listaVisible.clear();

        if (texto == null || texto.trim().isEmpty()) {
            listaVisible.addAll(listaOriginal);
        } else {
            texto = texto.toLowerCase(Locale.getDefault());

            for (Producto p : listaOriginal) {
                if (p.getNombre() != null &&
                        p.getNombre().toLowerCase(Locale.getDefault()).contains(texto)) {
                    listaVisible.add(p);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaVisible.get(position);

        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText(
                String.format(Locale.getDefault(), "$%.2f", producto.getPrecio())
        );

        holder.ivImagenProducto.setImageResource(R.drawable.agregar_img);

        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(producto.getImageUrls().get(0))
                    .placeholder(R.drawable.agregar_img)
                    .centerCrop()
                    .into(holder.ivImagenProducto);
        }

        // IR AL DETALLE
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Detalle_Producto.class);
            intent.putExtra(Detalle_Producto.EXTRA_PRODUCTO_ID, producto.getId());
            context.startActivity(intent);
        });

        //  MIS ANUNCIOS
        if (modoMisAnuncios) {
            holder.layoutAcciones.setVisibility(View.VISIBLE);

            holder.btnEliminar.setOnClickListener(v ->
                    FirebaseDatabase.getInstance()
                            .getReference("productos")
                            .child(producto.getId())
                            .removeValue()
                            .addOnSuccessListener(a ->
                                    Toast.makeText(context,
                                            "Anuncio eliminado",
                                            Toast.LENGTH_SHORT).show()
                            )
            );

            holder.btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditarProductoActivity.class);
                intent.putExtra("producto_id", producto.getId());
                context.startActivity(intent);
            });

        } else {
            holder.layoutAcciones.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaVisible.size();
    }

    // ACTUALIZAR DATOS DESDE FIREBASE
    public void actualizarProductos(List<Producto> nuevosProductos) {
        listaOriginal.clear();
        listaVisible.clear();

        listaOriginal.addAll(nuevosProductos);
        listaVisible.addAll(nuevosProductos);

        notifyDataSetChanged();
    }

    // VIEW HOLDER
    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImagenProducto;
        TextView tvNombre, tvPrecio;
        LinearLayout layoutAcciones;
        Button btnEditar, btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImagenProducto = itemView.findViewById(R.id.iv_producto_imagen);
            tvNombre = itemView.findViewById(R.id.tv_producto_titulo);
            tvPrecio = itemView.findViewById(R.id.tv_producto_precio);

            layoutAcciones = itemView.findViewById(R.id.layout_acciones);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }
}
