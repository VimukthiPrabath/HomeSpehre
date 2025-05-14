package lk.javainstitute.homesphre.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.Order;

public class OrdersParentAdapter extends RecyclerView.Adapter<OrdersParentAdapter.OrderParentViewHolder> {

    private final List<Order> orders;

    public OrdersParentAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_parent, parent, false);
        return new OrderParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderParentViewHolder holder, int position) {
        Order order = orders.get(position);

        // Order header
        holder.orderId.setText("Order #" + order.getOrderId().substring(0, 6));
        holder.orderTotal.setText(String.format("Total: Rs%.2f", order.getTotalAmount()));
        holder.orderStatus.setText(order.getStatus());

        // Status color
        int color = order.getStatus().equals("DELIVERED") ?
                R.color.green_dark : R.color.Red;
        holder.orderStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), color));

        // Products list
        OrderAdapter adapter = new OrderAdapter(order.getItems());
        holder.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.itemsRecyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class OrderParentViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderTotal;
        RecyclerView itemsRecyclerView;

        public OrderParentViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            orderStatus = itemView.findViewById(R.id.order_status);
            orderTotal = itemView.findViewById(R.id.order_total);
            itemsRecyclerView = itemView.findViewById(R.id.items_recycler_view);
        }
    }
}