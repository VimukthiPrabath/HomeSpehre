package lk.javainstitute.homesphre.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.Order;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderViewHolder> {

    private final List<Order> orderList;

    public OrderListAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_admin, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getOrderId());
        holder.tvCustomerName.setText(order.getUserName());
        holder.tvCustomerAddress.setText(order.getUserAddress());
        holder.tvTotalAmount.setText(String.format("Rs%.2f", order.getTotalAmount()));
        holder.tvStatus.setText(order.getStatus());
        holder.tvOrderDate.setText("Ordered on: " + order.getOrderDate().toString());

        // Setup nested RecyclerView for order items
        OrderAdapter itemsAdapter = new OrderAdapter(order.getItems());
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvOrderItems.setAdapter(itemsAdapter);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvCustomerAddress, tvTotalAmount, tvStatus, tvOrderDate;
        RecyclerView rvOrderItems;

        OrderViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
        }
    }
}