package com.example.lifelink;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder> {

    private Context context;
    private List<ConnectionModel> connectionsList;
    private OnConnectionClickListener listener;

    public ConnectionAdapter(Context context) {
        this.context = context;
        this.connectionsList = new ArrayList<>();
    }

    public void setOnConnectionClickListener(OnConnectionClickListener listener) {
        this.listener = listener;
    }

    public void setConnectionsList(List<ConnectionModel> connectionsList) {
        this.connectionsList = connectionsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_connection, parent, false);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder holder, int position) {
        ConnectionModel connection = connectionsList.get(position);
        
        holder.tvConnectionName.setText(connection.getName());
        holder.tvDistance.setText(connection.getDistance() + " km away");
        holder.tvBloodType.setText(connection.getBloodType());
        
        // Set connect button status based on connection status
        if (connection.isConnected()) {
            holder.btnConnect.setText("Connected");
            holder.btnConnect.setEnabled(false);
        } else {
            holder.btnConnect.setText("Connect");
            holder.btnConnect.setEnabled(true);
        }
        
        // Set click listeners
        holder.btnConnect.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConnectClick(connection);
                // Update UI immediately for better UX
                holder.btnConnect.setText("Connected");
                holder.btnConnect.setEnabled(false);
                connection.setConnected(true);
            }
        });
        
        holder.btnMessage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(connection);
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConnectionClick(connection);
            }
        });
    }

    @Override
    public int getItemCount() {
        return connectionsList.size();
    }

    public static class ConnectionViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivContactImage;
        TextView tvConnectionName, tvDistance, tvBloodType;
        Button btnConnect, btnMessage;

        public ConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivContactImage = itemView.findViewById(R.id.ivContactImage);
            tvConnectionName = itemView.findViewById(R.id.tvConnectionName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvBloodType = itemView.findViewById(R.id.tvBloodType);
            btnConnect = itemView.findViewById(R.id.btnConnect);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }
    
    public interface OnConnectionClickListener {
        void onConnectionClick(ConnectionModel connection);
        void onConnectClick(ConnectionModel connection);
        void onMessageClick(ConnectionModel connection);
    }
} 